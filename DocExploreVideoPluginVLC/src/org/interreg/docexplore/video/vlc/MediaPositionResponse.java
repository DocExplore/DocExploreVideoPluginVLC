package org.interreg.docexplore.video.vlc;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.Response;
import org.interreg.docexplore.reader.net.StreamedResource;

public class MediaPositionResponse implements Response
{
	private static final long serialVersionUID = -8477790491986787857L;
	
	String uri;
	long length;
	float pos;
	
	public MediaPositionResponse(String uri, float pos, long length)
	{
		this.uri = uri;
		this.pos = pos;
		this.length = length;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource res = client.getResource(uri);
		if (res == null)
			return;
		StreamedMedia media = (StreamedMedia)res;
		media.pos = pos;
		media.length = length;
		media.lastPos = System.currentTimeMillis();
	}

}
