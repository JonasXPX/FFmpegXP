package me.jonasxpx.ffmpegxp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFmpegXP {

	public static File file;
	public static ArrayList<File> files;
	public static String[] remove = {"720p", "1080p", "www","doublado", "Dual", "TORRENTDOSFILMES", "COM", "WEB-DL", "The", "vampires", "diaries"};
	
	
	public static void main(String[] args) throws IOException {
		file = new File(args[0]);
		files = new ArrayList<File>();
		String fileOut = null;
		String temp_fileOut = "";
		File directoryOut = null;
		
		if(!file.exists()){
			System.err.println("Local inválido");
		}
		if(args.length >= 2){
			fileOut = args[args.length-1];
			System.out.println(fileOut);
		} else if(args.length == 1){
			return;
		}
		if(file.isDirectory()){
			file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if(f.getName().endsWith(".mp4") || f.getName().endsWith(".mkv") || f.getName().endsWith(".avi")){
						try {
							if(Files.size(f.toPath()) > 10485760)
							files.add(f);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			});
		} else {
			files.add(file);
		}
		
		System.out.println("Files: " + files.size());
		if(files.size() > 1){
			directoryOut = new File(fileOut.substring(0, fileOut.length() - 4));
			System.out.println(directoryOut.mkdir());
		}
		
		int x = 0;
		for(File f : files){
			file = f;
			MediaInfo m = getProbe(file.getAbsolutePath());
			File temp = new File(fileOut);
			temp_fileOut = x + "_" + formatFileName(f.getName()) + "_" + temp.getName().substring(temp.getName().length() - 4, temp.getName().length());
			System.out.println("File out: " + temp_fileOut);
			boolean copyCodec = true;
			List<String> cmd = new ArrayList<>();
			cmd.add("ffmpeg"); cmd.add("-y"); cmd.add("-i"); cmd.add(file.getAbsolutePath());
			if(m.getStreamLength() > 2){
				cmd.add("-map");
				cmd.add("v:0");
				cmd.add("-map");
				cmd.add("a:0");
			}
			if(m.getStreamAt(0).getBitrate() > 180000 || m.getBitrate() > 160000){
				cmd.add("-b:v");
				cmd.add("1.4M");
				cmd.add("-bufsize");
				cmd.add("1.4M");
				cmd.add("-maxrate");
				cmd.add("1.6M");
				copyCodec = false;
			}
			if(!m.getStreamAt(0).getDisplayRatio().equalsIgnoreCase("16:9") && !m.getStreamAt(0).getDisplayRatio().equalsIgnoreCase("-1")){
				cmd.add("-aspect");
				cmd.add("16:9");
				copyCodec = false;
			}
			if(m.getStreamAt(0).getWidth() >= 1900){
				cmd.add("-vf");
				cmd.add("scale=hd720");
				copyCodec = false;
			}
			if(m.getStreamAt(1).getChannels() > 2){
				cmd.add("-ac");
				cmd.add("2");
			}
			if(copyCodec){
				cmd.add("-vcodec");
				cmd.add("copy");
			}
				
			cmd.add((directoryOut == null ? fileOut : directoryOut.getAbsolutePath() + File.separator + temp_fileOut));
			buildProcess(true, cmd);
			x++;
			
		}
		if(files.size() > 1){
			System.out.println("Running: " + Arrays.asList("dropbox", "upload", (directoryOut == null ? "" : directoryOut.getAbsolutePath()), "Filmes").toString());
			buildProcess(true, Arrays.asList("dropbox", "upload", (directoryOut == null ? "" : directoryOut.getAbsolutePath()), "Filmes"));
		} else {
			System.out.println("Running: " + Arrays.asList("dropbox","upload", new File(fileOut).getAbsolutePath(), "Filmes").toString());
			buildProcess(true, Arrays.asList("dropbox",	"upload", new File(fileOut).getAbsolutePath(), "Filmes"));
		} 
	}
	
	
	public static MediaInfo getProbe(String file){
		String json = buildProcess(false, "ffprobe", "-show_format", "-show_streams"
				,"-print_format", "json"
				,"-v", "fatal"
				, file);
		return new MediaInfo(json);
	}
	
	public static String buildProcess(boolean realTime, List<String> args){
		StringBuffer sb = new StringBuffer();
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		Process p;
		BufferedReader br = null;
		try{
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String start = null;
			String s = "";
			while((s = br.readLine()) != null){
				if(realTime){
					if(start != null && s.startsWith(start)){
						System.out.print(s + "\r");
					}else{
						System.out.println(s);
					}
					start = s.substring(0, 3);
				}else
					sb.append(s);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	public static String buildProcess(boolean realTime, String ... args){
		StringBuffer sb = new StringBuffer();
		ProcessBuilder pb = new ProcessBuilder(args);
		try{
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = "";
			while((s = br.readLine()) != null){
				if(realTime)
					System.out.print(s + "\r");
				else
					sb.append(s);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String formatFileName(String s){
		String finalFile = s.toLowerCase();
		for(String string : remove){
			finalFile = finalFile.replaceAll(string.toLowerCase(), "");
		}
		return finalFile;
	}
}
