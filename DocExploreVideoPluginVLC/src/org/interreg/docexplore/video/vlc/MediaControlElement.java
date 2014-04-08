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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.roi.OverlayElement;
import org.interreg.docexplore.reader.gfx.Texture;

import uk.co.caprica.vlcj.binding.internal.libvlc_state_t;

public class MediaControlElement implements OverlayElement
{
	ReaderApp app;
	MediaOverlayElement media;
	Texture tex;
	BufferedImage buffer;
	public static int controlHeight = 40;
	
	public MediaControlElement(ReaderApp app, int width, final MediaOverlayElement media)
	{
		this.app = app;
		this.media = media;
		this.tex = new Texture(width, controlHeight, false, false);
		this.buffer = new BufferedImage(tex.width(), tex.height(), BufferedImage.TYPE_3BYTE_BGR);
		
		update();
		new Thread() {public void run()
		{
			while (!media.disposed)
			{
				try {Thread.sleep(300);}
				catch (Exception e) {}
				update();
			}
		}}.start();
	}
	
	void update()
	{
		synchronized (media)
		{
			if (media.disposed)
				return;
			
			Graphics2D g = buffer.createGraphics();
			float pos = 0;
			long length = 0;
			boolean playing = false;
			if (media.media.player != null)
			{
				playing = media.media.player.getMediaPlayerState() == libvlc_state_t.libvlc_Playing;
				if (ReaderApp.local)
				{
					pos = media.media.player.getPosition();
					length = media.media.player.getLength();
				}
				else
				{
					pos = media.media.pos;
					if (media.media.length > 0 && media.media.player.getMediaPlayerState() == libvlc_state_t.libvlc_Playing)
						pos += (System.currentTimeMillis()-media.media.lastPos)*1f/media.media.length;
					length = media.media.length;
				}
			}
			MediaPanel.drawControls(g, pos, length, playing, 0, 0, buffer.getWidth(), buffer.getHeight());
			tex.setup(buffer);
			app.submitRenderTask(new Runnable() {public void run() {tex.update();}});
		}
	}

	public int getWidth(int maxWidth) {return maxWidth;}
	public int getHeight(int width) {return width*tex.height()/tex.width();}

	public boolean bind() {tex.bind(); return true;}

	public void dispose() {tex.dispose();}

	public boolean clicked(float x, float y)
	{
		int mx = (int)(x*buffer.getWidth()), my = (int)(y*buffer.getHeight());
		float seek = MediaPanel.getSeekPosition(mx, my, 0, 0, buffer.getWidth(), buffer.getHeight());
		if (seek >= 0)
			media.media.seek(seek);
		else if (MediaPanel.getPlaybackToggle(mx, my, 0, 0, buffer.getWidth(), buffer.getHeight()))
		{
			media.media.overlay = null;
			media.media.play();
		}
		return true;
	}
	
	public float marginFactor() {return 1;}
}
