package org.interreg.docexplore.video.vlc;

import java.awt.Dimension;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.net.ResourceRequest;
import org.interreg.docexplore.reader.net.ServerTask;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class MediaRequest extends ResourceRequest
{
	private static final long serialVersionUID = -7699800215470712351L;

	ServerTask task;
	String address;
	String uri;
	int port;
	boolean streaming = false;
	
	HeadlessMediaPlayer mediaPlayer = null;
	Object monitor = null;
	
	public MediaRequest(String uri, String address)
	{
		super(uri);
		this.uri = uri;
		this.address = address;
	}

	static Map<String, MediaRequest> streams = new TreeMap<String, MediaRequest>();
	public void run(ServerTask task) throws Exception
	{
		if (ReaderApp.local)
		{
			task.submitResponse(new MediaFileResponse(uri, new File(task.server.baseDir, uri)));
			return;
		}
		
		this.port = 5555;
		this.task = task;
		this.monitor = new Object();
		
		streams.put(uri, this);
		streaming = true;
		if (preparePlayer(0))
		{
			while (!cancel)
			{
				try {Thread.sleep(1000);}
				catch (Exception e) {}
				synchronized (monitor) {task.submitResponse(new MediaPositionResponse(uri, mediaPlayer.getPosition(), mediaPlayer.getLength()));}
			}
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		else System.out.println("Couldn't start media stream: "+uri);
		
		streaming = false;
		streams.remove(uri);
	}
	
	boolean preparePlayer(float pos)
	{
		synchronized (monitor)
		{
			float start = 0;
			if (mediaPlayer == null)
				mediaPlayer = new MediaPlayerFactory().newHeadlessMediaPlayer();
			else
			{
				start = mediaPlayer.getLength()*pos/1000;
				mediaPlayer.stop();
			}
			if (!mediaPlayer.startMedia(new File(task.server.baseDir, uri).getAbsolutePath(),
				":sout=#rtp{sdp=rtsp://@"+address+":"+port+"/docex}",
				":no-sout-rtp-sap", 
				":no-sout-standard-sap", 
				":sout-all", 
				":sout-keep",
				":start-time="+start))
					return false;
			Dimension dim = mediaPlayer.getVideoDimension();
			task.submitResponse(new MediaResponse(uri, port, dim == null ? 320 : dim.width, dim == null ? 240 : dim.height, mediaPlayer.getLength()));
			return true;
		}
	}
}
