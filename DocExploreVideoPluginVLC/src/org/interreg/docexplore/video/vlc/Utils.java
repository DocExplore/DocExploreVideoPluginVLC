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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.interreg.docexplore.util.ByteUtils;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.logger.Logger.Level;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Utils
{
	public static boolean inited = false;
	public static synchronized void init(File jarFile, File dependencies) throws IOException
	{
		if (inited)
			return;
		
		File vlcLibs = new File(dependencies, "libs");
		if (!vlcLibs.exists())
		{
			vlcLibs.mkdir();
			
			JarFile vlcJar = new JarFile(jarFile);
			Enumeration<JarEntry> entries = vlcJar.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (!entry.getName().startsWith("libs-") || entry.getName().endsWith("/"))
					continue;
				add(vlcJar, entry, vlcLibs);
			}
			vlcJar.close();
		}
		
		System.out.println("VLC native library dir: "+vlcLibs.getCanonicalPath()+File.separator+"lib");
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibs.getCanonicalPath()+File.separator+"lib");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		LibXUtil.initialise();
		Logger.setLevel(Level.FATAL);
		inited = true;
	}
	
	static void add(JarFile jarFile, JarEntry entry, File libs) throws IOException
	{
		String [] path = entry.getName().split("/");
		File cur = libs;
		for (int i=1;i<path.length-1;i++)
			cur = new File(cur, path[i]);
		cur.mkdirs();
		
		byte [] bytes = ByteUtils.readStream(jarFile.getInputStream(entry));
		if (bytes == null)
			throw new IOException("Can't extract "+entry.getName());
		
		FileOutputStream output = new FileOutputStream(new File(cur, path[path.length-1]));
		output.write(bytes);
		output.close();
	}
}
