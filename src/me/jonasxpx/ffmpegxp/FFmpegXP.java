package me.jonasxpx.ffmpegxp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FFmpegXP {

	public static File file;
	public static ArrayList<File> files;
	public static void main(String[] args) throws IOException {
		file = new File(args[0]);
		files = new ArrayList<File>();
		String fileOut = null;
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
		int x = 0;
		System.out.println("Files: " + files.size());
		File directoryOut = null;
		if(files.size() > 1){
			directoryOut = new File(fileOut.substring(0, fileOut.length() - 4));
			System.out.println(directoryOut.mkdir());
		}
		for(File f : files){
			file = f;
			MediaInfo m = getProbe(file.getAbsolutePath());
			String temp_fileOut = x + "_" + fileOut;
			
			/*String[] maps = {"-map","v:0","-map","a:0"};
			String[] codecs = {"-b:v","1.5M","-bufsize","1.5M","-maxrate","1.8M"};
			String[] copyCodecs = {"-vcodec", "copy"};
			String[] channel = {"-ac 2"};*/
			boolean copyCodec = true;
			List<String> cmd = new ArrayList<>();
			cmd.add("ffmpeg"); cmd.add("-y"); cmd.add("-i"); cmd.add(file.getAbsolutePath());
			if(m.getStreamLength() > 2){
				cmd.add("-map");
				cmd.add("v:0");
				cmd.add("-map");
				cmd.add("a:0");
			}
			if(m.getStreamAt(0).getBitrate() > 180000){
				cmd.add("-b:v");
				cmd.add("1.5M");
				cmd.add("-bufsize");
				cmd.add("1.5M");
				cmd.add("-maxrate");
				cmd.add("1.8M");
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
				
			cmd.add((directoryOut == null ? "" : directoryOut.getAbsolutePath() + File.separator) + temp_fileOut);
			buildProcess(true, cmd);
			x++;
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
	
}
