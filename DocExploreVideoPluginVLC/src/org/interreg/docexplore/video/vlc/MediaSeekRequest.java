package org.interreg.docexplore.video.vlc;

import org.interreg.docexplore.reader.net.Request;
import org.interreg.docexplore.reader.net.ServerTask;

public class MediaSeekRequest implements Request
{
	private static final long serialVersionUID = 8739463377157353324L;
	
	String uri;
	float pos;
	
	public MediaSeekRequest(String uri, float pos)
	{
		this.uri = uri;
		this.pos = pos;
	}

	public void run(ServerTask task) throws Exception
	{
		MediaRequest request = MediaRequest.streams.get(uri);
		if (request != null)
			request.preparePlayer(pos);
	}
}
