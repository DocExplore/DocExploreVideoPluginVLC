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
