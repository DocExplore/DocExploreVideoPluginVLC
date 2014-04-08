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
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.caprica.vlcj.binding.internal.libvlc_state_t;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

import com.sun.jna.Memory;

public class MediaReader implements RenderCallback
{
	public static int defaultWidth = 400, defaultHeight = 300;
	
	public int width;
	public int height;
//	public int sourceWidth = -1;
//	public int sourceHeight = -1;
	
	private DirectMediaPlayer mediaPlayer;

	public MediaReader() throws InterruptedException, InvocationTargetException {this(defaultWidth, defaultHeight);}
	public MediaReader(int width, int height)
	{
		super();
		this.width = width;
		this.height = height;
		
		mediaPlayer = invoke(ccreate(width, height));
		if (mediaPlayer == null)
			throw new NullPointerException();
	}

	boolean released = false;
	public void dispose()
	{
		synchronized (listeners) {listeners.clear();}
		stop();
		release();
	}
	
	synchronized <T> T invoke(Callable<T> callable)
	{
		if (released)
			return null;
		try {return service.submit(callable).get();} 
		catch (Exception e) {e.printStackTrace();} 
		return null;
	}
	
	Callable<DirectMediaPlayer> ccreate(final int width, final int height) 
		{return new Callable<DirectMediaPlayer>() {public DirectMediaPlayer call() throws Exception 
			{return new MediaPlayerFactory(new String [] {}).newDirectMediaPlayer(
				new BufferFormatCallback() {public BufferFormat getBufferFormat(int arg0, int arg1)
				{
					return new BufferFormat("RV24", width, height, new int [] {3*width}, new int [] {height});
				}}, 
				MediaReader.this
			);}};}
	
	synchronized public libvlc_state_t getMediaState() {return invoke(cgetMediaState());}
	Callable<libvlc_state_t> cgetMediaState() {return new Callable<libvlc_state_t>() {public libvlc_state_t call() throws Exception {return mediaPlayer.getMediaState();}};}
	
	synchronized public libvlc_state_t getMediaPlayerState() {return invoke(cgetMediaPlayerState());}
	Callable<libvlc_state_t> cgetMediaPlayerState() {return new Callable<libvlc_state_t>() {public libvlc_state_t call() throws Exception {return mediaPlayer.getMediaPlayerState();}};}
	
	synchronized public void stop() {invoke(cstop());}
	Callable<Void> cstop() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.stop(); return null;}};}
	
	synchronized public void start() {invoke(cstart());}
	Callable<Void> cstart() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.start(); return null;}};}
	
	synchronized public void play() {invoke(cplay());}
	Callable<Void> cplay() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.play(); return null;}};}
	
	synchronized public void pause() {invoke(cpause());}
	Callable<Void> cpause() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.pause(); return null;}};}
	
	synchronized public void release() {invoke(crelease()); released = true;}
	Callable<Void> crelease() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.release(); return null;}};}
	
	synchronized public boolean startMedia(String s) {return invoke(cstartMedia(s));}
	Callable<Boolean> cstartMedia(final String s) {return new Callable<Boolean>() {public Boolean call() throws Exception {return mediaPlayer.startMedia(s);}};}
	
	synchronized public boolean playMedia(String s) {return invoke(cplayMedia(s));}
	Callable<Boolean> cplayMedia(final String s) {return new Callable<Boolean>() {public Boolean call() throws Exception {return mediaPlayer.playMedia(s);}};}
	
	synchronized public void setPosition(float p) {invoke(csetPosition(p));}
	Callable<Void> csetPosition(final float p) {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.setPosition(p); return null;}};}
	
	synchronized public float getPosition() {return invoke(cgetPosition());}
	Callable<Float> cgetPosition() {return new Callable<Float>() {public Float call() throws Exception {return mediaPlayer.getPosition();}};}
	
	synchronized public long getLength() {return invoke(cgetLength());}
	Callable<Long> cgetLength() {return new Callable<Long>() {public Long call() throws Exception {return mediaPlayer.getLength();}};}
	
	synchronized public Dimension getVideoDimension() {return invoke(cgetVideoDimension());}
	Callable<Dimension> cgetVideoDimension() {return new Callable<Dimension>() {public Dimension call() throws Exception {return mediaPlayer.getVideoDimension();}};}

	public static interface MediaListener
	{
		public void mediaProduced(Memory buffer);
	}
	private List<MediaListener> listeners = new LinkedList<MediaReader.MediaListener>();
	public void addMediaListener(MediaListener mediaListener) {synchronized(listeners) {listeners.add(mediaListener);}}
	public void removeMediaListener(MediaListener mediaListener) {synchronized(listeners) {listeners.remove(mediaListener);}}
	public void notifyMediaProduced(Memory buffer) {synchronized(listeners) {for (MediaListener listener : listeners) listener.mediaProduced(buffer);}}

	public void display(DirectMediaPlayer player, Memory [] memory, BufferFormat format)
	{
		//System.out.println("Memory produced: "+memory.length+" "+format.getWidth()+"x"+format.getHeight()+" "+format.getChroma());
		display(memory[0]);
	}
	public void display(Memory nativeBuffer) {notifyMediaProduced(nativeBuffer);}
	
	static ExecutorService service = Executors.newFixedThreadPool(1);
}