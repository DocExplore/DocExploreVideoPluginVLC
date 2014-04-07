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
