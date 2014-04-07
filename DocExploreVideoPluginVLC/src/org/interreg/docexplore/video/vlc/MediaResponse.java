package org.interreg.docexplore.video.vlc;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.Response;
import org.interreg.docexplore.reader.net.StreamedResource;

import uk.co.caprica.vlcj.mrl.RtspMrl;

public class MediaResponse implements Response
{
	private static final long serialVersionUID = 4676632433906971439L;

	String uri;
	int port;
	long length;
	int width, height;
	
	public MediaResponse(String uri, int port, int width, int height, long length)
	{
		this.uri = uri;
		this.port = port;
		this.length = length;
		this.width = width;
		this.height = height;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource res = client.getResource(uri);
		if (res == null)
			return;
		StreamedMedia media = (StreamedMedia)res;
		
		media.mrl = new RtspMrl().host(client.serverAddress).port(port).path("/docex");
		media.length = length;
		int width = media.width < 0 ? this.width : media.width;
		int height = media.height < 0 ? this.height : media.height;
		if (media.player != null)
			media.player.dispose();
		media.player = new MediaReader(width, height);
		media.image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		media.player.addMediaListener(media);
		if (!media.player.playMedia(media.mrl.value()))
			throw new Exception("Couldn't get stream: "+uri+"!");
		media.player.pause();
	}
}
