package me.jonasxpx.ffmpegxp;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

public class MediaInfo {
	
	private final int streams;
	private final long bitrate;
	private final long size;
	private final double duration;
	private final String format_name;
	private final JSONObject index;
	
	public MediaInfo(String json) {
		this.index = new JSONObject(json);
		JSONObject format = index.getJSONObject("format");
		this.streams = format.getInt("nb_streams");
		this.bitrate = format.has("bit_rate") ? format.getLong("bit_rate") : -1;
		this.size = format.getLong("size");
		this.duration = format.getDouble("duration");
		this.format_name = format.getString("format_name");
		
	}
	
	public Stream getStreamAt(int a){
		JSONArray ar = index.getJSONArray("streams");
		if(a > ar.length()){
			throw new ArrayIndexOutOfBoundsException("Possição inválida " + a + ", Stream não encontrada");
		}
		return new Stream(ar.getJSONObject(a));
	}

	public int getStreamLength() {
		return streams;
	}

	public long getBitrate() {
		return bitrate;
	}

	public long getSize() {
		return size;
	}

	public double getDuration() {
		return duration;
	}

	public String getFormat_name() {
		return format_name;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	public static void main(String[] args) {
		File f = new File("C:\\Users\\JonasXPX\\Desktop\\VIVO\\FORMULARIO.pdf");
		System.out.println(f.getName());
	}
}
