package org.interreg.docexplore.video.vlc;

import java.io.File;

import org.interreg.docexplore.reader.net.ReaderServer;
import org.interreg.docexplore.reader.plugin.ServerPlugin;

public class VideoServerPlugin implements ServerPlugin
{

	@Override public void setHost(ReaderServer server, File jarFile, File dependencies)
	{
		try {Utils.init(jarFile, dependencies);}
		catch (Exception e) {throw new RuntimeException(e);}
	}
}
