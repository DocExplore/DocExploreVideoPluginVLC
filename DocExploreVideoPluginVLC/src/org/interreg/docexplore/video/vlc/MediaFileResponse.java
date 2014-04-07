package org.interreg.docexplore.video.vlc;

import java.awt.image.BufferedImage;
import java.io.File;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.Response;
import org.interreg.docexplore.reader.net.StreamedResource;

public class MediaFileResponse implements Response
{
	private static final long serialVersionUID = 4676632433906971439L;

	String uri;
	File file;
	
	public MediaFileResponse(String uri, File file)
	{
		this.uri = uri;
		this.file = file;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource res = client.getResource(uri);
		if (res == null)
			return;
		StreamedMedia media = (StreamedMedia)res;
		
		int width = media.width < 0 ? 320 : media.width;
		int height = media.height < 0 ? 240 : media.height;
		if (media.player != null)
			media.player.dispose();
		media.player = new MediaReader(width, height);
		media.image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		media.player.addMediaListener(media);
		if (!media.player.startMedia(file.getAbsolutePath()))
			throw new Exception("Couldn't get stream: "+uri+"!");
		media.player.pause();
	}
}
