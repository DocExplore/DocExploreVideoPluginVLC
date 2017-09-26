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
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.caprica.vlcj.player.MediaDetails;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

public class MediaDetailsReader
{
	private HeadlessMediaPlayer mediaPlayer;

	public MediaDetailsReader() throws InterruptedException, InvocationTargetException
	{
		mediaPlayer = invoke(ccreate());
		if (mediaPlayer == null)
			throw new NullPointerException();
	}

	boolean released = false;
	public void dispose()
	{
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
	
	Callable<HeadlessMediaPlayer> ccreate() 
		{return new Callable<HeadlessMediaPlayer>() {public HeadlessMediaPlayer call() throws Exception 
			{return new MediaPlayerFactory(new String [] {}).newHeadlessMediaPlayer();}};}
	
	synchronized public MediaDetails getMediaDetails(File f) {return getMediaDetails(f.getAbsolutePath());}
	synchronized public MediaDetails getMediaDetails(String s)
	{
		startMedia(s);
		MediaDetails details = getMediaDetails();
		System.out.println(details);
		stop();
		return details;
	}
	
	synchronized public void release() {invoke(crelease()); released = true;}
	Callable<Void> crelease() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.release(); return null;}};}
	
	synchronized public void parseMedia() {invoke(cparseMedia());}
	Callable<Void> cparseMedia() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.parseMedia(); return null;}};}
	
	synchronized public boolean startMedia(String s) {return invoke(cstartMedia(s));}
	Callable<Boolean> cstartMedia(final String s) {return new Callable<Boolean>() {public Boolean call() throws Exception {return mediaPlayer.startMedia(s);}};}
	
	synchronized public void stop() {invoke(cstop());}
	Callable<Void> cstop() {return new Callable<Void>() {public Void call() throws Exception {mediaPlayer.stop(); return null;}};}
	
	synchronized public MediaDetails getMediaDetails() {return invoke(cgetMediaDetails());}
	Callable<MediaDetails> cgetMediaDetails() {return new Callable<MediaDetails>() {public MediaDetails call() throws Exception {return mediaPlayer.getMediaDetails();}};}
	
	synchronized public MediaMeta getMediaMeta() {return invoke(cgetMediaMeta());}
	Callable<MediaMeta> cgetMediaMeta() {return new Callable<MediaMeta>() {public MediaMeta call() throws Exception {return mediaPlayer.getMediaMeta();}};}
	
	synchronized public long getLength() {return invoke(cgetLength());}
	Callable<Long> cgetLength() {return new Callable<Long>() {public Long call() throws Exception {return mediaPlayer.getLength();}};}
	
	synchronized public Dimension getVideoDimension() {return invoke(cgetVideoDimension());}
	Callable<Dimension> cgetVideoDimension() {return new Callable<Dimension>() {public Dimension call() throws Exception {return mediaPlayer.getVideoDimension();}};}

	static ExecutorService service = Executors.newFixedThreadPool(1);
}