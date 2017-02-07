package me.jonasxpx.ffmpegxp;

import org.json.JSONObject;

public class Stream {

	private final int index;
	private final String codec_name;
	private final boolean isVideo;
	private int width = -1;
	private final long bitrate;
	private int channels = -1;
	private final String displayRatio;
	
	public Stream(JSONObject js) {
		this.index = js.getInt("index");
		this.codec_name = js.getString("codec_name");
		this.isVideo = (js.getString("codec_type") == "video" ? true : false);
		setWidth(js.has("width") ? js.getInt("width") : -1);
		setChannels(js.has("channels") ? js.getInt("channels") : -1);
		this.displayRatio = js.has("display_aspect_ratio") ? js.getString("display_aspect_ratio") : "-1";
		this.bitrate = js.has("bit_rate") ? js.getLong("bit_rate") : 0;
	}

	public int getWidth() {
		return width;
	}

	private void setWidth(int width) {
		this.width = width;
	}

	
	/**
	 * @return Return -1 if there has no video.
	 */
	public int getIndex() {
		return index;
	}

	public String getCodecName() {
		return codec_name;
	}

	public boolean isVideo() {
		return isVideo;
	}

	public long getBitrate() {
		return bitrate;
	}

	/**
	 * 
	 * @return Return -1 if there has no audio.
	 */
	public int getChannels() {
		return channels;
	}

	private void setChannels(int channels) {
		this.channels = channels;
	}

	public String getDisplayRatio() {
		return displayRatio;
	}
	
}
