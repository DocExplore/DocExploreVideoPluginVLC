package org.interreg.docexplore.video.vlc;

import java.awt.Dimension;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.roi.OverlayElement;
import org.interreg.docexplore.video.vlc.VideoClientPlugin.MediaInfoElement;

public class MediaOverlayElement implements OverlayElement
{
	StreamedMedia media;
	boolean disposed;
	
	public MediaOverlayElement(ReaderApp app, MediaInfoElement infoElement)
	{
		this.media = app.client.getResource(StreamedMedia.class, infoElement.uri);
		media.setSize(infoElement.width, infoElement.height);
		if (media == null)
			throw new NullPointerException();
		media.overlay = VideoClientPlugin.helpOverlay;
	}
	
	public int getWidth(int maxWidth)
	{
		if (media == null)
			return 0;
		return maxWidth;
//		if (media.texture == null)
//			return maxWidth;
//		if (media.texture.width() > maxWidth)
//			return maxWidth;
//		int minDim = maxWidth/3;
//		if (media.texture.width() < minDim && media.texture.height() < minDim)
//			return minDim*media.texture.width()/Math.max(media.texture.width(), media.texture.height());
//		return media.texture.width();
	}
	public int getHeight(int width)
	{
		if (media == null)
			return 0;
		if (media.texture == null)
			return width/4;
		Dimension dim = media.player.getVideoDimension();
		if (dim == null)
			return media.texture.height()*width/media.texture.width();
		else return dim.height*width/dim.width;
	}

	public boolean bind()
	{
		if (media == null)
			return false;
		media.bind();
		return true;
	}

	public synchronized void dispose()
	{
		disposed = true;
		media.release();
	}
	
	public boolean clicked(float x, float y)
	{
		media.overlay = null;
		media.play();
		return true;
	}
	
	public float marginFactor() {return 0;}
}
