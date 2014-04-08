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
