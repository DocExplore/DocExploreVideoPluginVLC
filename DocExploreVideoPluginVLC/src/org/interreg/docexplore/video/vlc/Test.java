package org.interreg.docexplore.video.vlc;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.logger.Logger.Level;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Test
{
	public static void main(String [] args) throws Exception
	{
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "libs/lib");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		//Logger.setLevel(Level.DEBUG);
		Logger.setLevel(Level.FATAL);
		
		
		String media = "C:\\sci\\Serenity - HD DVD Trailer.mp4";
		
		MediaReader player = new MediaReader(1280, 720);
		
		JFrame frame = new JFrame("VLCJ Test");
        MediaPanel imagePane = new MediaPanel(16);
        imagePane.set(player);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(imagePane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //frame.setResizable(false);
        frame.setVisible(true);
		
        //new FileUDPStream(new File("Serenity - HD DVD Trailer.mp4"));
        //Thread.sleep(1000);
        
        //player.mediaPlayer.playMedia(new UdpMrl().groupAddress("127.0.0.1").port(1234).value());
        player.playMedia(media);
	}
}
