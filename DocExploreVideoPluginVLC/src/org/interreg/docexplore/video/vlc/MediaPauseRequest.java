package org.interreg.docexplore.video.vlc;

import org.interreg.docexplore.reader.net.Request;
import org.interreg.docexplore.reader.net.ServerTask;

public class MediaPauseRequest implements Request
{
	private static final long serialVersionUID = -6963521611470973126L;

	String uri;
	
	public MediaPauseRequest(String uri)
	{
		this.uri = uri;
	}
	
	public void run(ServerTask task) throws Exception
	{
		MediaRequest request = MediaRequest.streams.get(uri);
		if (request != null)
			request.mediaPlayer.pause();
	}

}
