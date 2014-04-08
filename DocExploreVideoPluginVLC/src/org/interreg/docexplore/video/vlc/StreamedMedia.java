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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.InputStream;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.reader.gfx.Filler;
import org.interreg.docexplore.reader.gfx.StreamedTexture;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.ResourceRequest;
import org.interreg.docexplore.video.vlc.MediaReader.MediaListener;

import uk.co.caprica.vlcj.binding.internal.libvlc_state_t;
import uk.co.caprica.vlcj.mrl.Mrl;

import com.sun.jna.Memory;

public class StreamedMedia extends StreamedTexture implements MediaListener
{
	Mrl mrl = null;
	MediaReader player = null;
	int width = -1, height = -1;
	long length = 0;
	float pos = 0;
	long lastPos = 0;
	
	protected StreamedMedia(ReaderClient client, String uri, File file)
	{
		super(client, uri, file);
		this.width = -1;
		this.height = -1;
	}
	
	static Texture filler = null;
	static Bindable getFiller()
	{
		if (filler == null)
			try {return filler = new Texture(VideoClientPlugin.largeMediaImage, false);}
			catch (Exception e) {e.printStackTrace();}
		if (filler == null)
			return Filler.getBindable();
		return filler;
	}
	public void bind()
	{
		if (texture != null && isComplete())
			texture.bind();
		else getFiller().bind();
	}
	
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void handle(InputStream stream) throws Exception
	{
		if (file == null)
			return;
		
		int width = this.width < 0 ? 320 : this.width;
		int height = this.height < 0 ? 240 : this.height;
		if (player != null)
			player.dispose();
		player = new MediaReader(width, height);
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		player.addMediaListener(this);
		if (!player.startMedia(file.getAbsolutePath()))
			throw new Exception("Couldn't get stream: "+uri+"!");
		player.pause();
	}

	public void release()
	{
		while (player == null)
			try {Thread.sleep(100);}
			catch (Exception e) {}
		player.dispose();
		super.release();
	}

	public ResourceRequest request()
	{
		return new MediaRequest(uri, client.serverAddress);
	}
	
	BufferedImage overlay = null;
	void doOverlay()
	{
		BufferedImage overlay = this.overlay;
		if (overlay == null)
			return;
		image.createGraphics().drawImage(overlay, 0, image.getHeight()-overlay.getHeight(), null);
	}

	boolean initSent = false;
	public void mediaProduced(Memory nativeBuffer)
	{
		if (image == null)
			return;
		byte [] buffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		nativeBuffer.read(0, buffer, 0, buffer.length);
		doOverlay();
		if (!initSent)
			{initSent = true; client.app.submitRenderTask(new InitTextureTask(this));}
		else if (texture != null)
		{
			texture.setup(image);
			client.app.submitRenderTask(new UpdateTextureTask(this));
		}
	}
	
	public void seek(float pos)
	{
		if (ReaderApp.local)
		{
			if (player.getMediaState() == libvlc_state_t.libvlc_Ended)
			{
				player.stop();
				player.start();
				player.pause();
			}
			player.setPosition(pos);
		}
		else
		{
			player.stop();
			client.submitRequest(new MediaSeekRequest(uri, pos));
		}
	}
	
	public void play()
	{
		if (ReaderApp.local)
		{
			if (player.getMediaState() == libvlc_state_t.libvlc_Ended)
			{
				player.stop();
				player.start();
				player.pause();
			}
			if (player.getMediaState() == libvlc_state_t.libvlc_Playing) player.pause();
			else player.start();
		}
		else
		{
			if (player.getMediaPlayerState() != libvlc_state_t.libvlc_Playing)
				client.submitRequest(new MediaSeekRequest(uri, pos));
			else
			{
				client.submitRequest(new MediaPauseRequest(uri));
				player.play();
			}
		}
	}
}
