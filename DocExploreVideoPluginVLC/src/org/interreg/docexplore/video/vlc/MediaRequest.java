/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
