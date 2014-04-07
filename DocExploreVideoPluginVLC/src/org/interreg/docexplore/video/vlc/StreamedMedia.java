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
