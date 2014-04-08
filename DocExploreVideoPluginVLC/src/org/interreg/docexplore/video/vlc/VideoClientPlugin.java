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
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.book.ROISpecification.InfoElement;
import org.interreg.docexplore.reader.book.roi.OverlayElement;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.StreamedResource;
import org.interreg.docexplore.reader.net.StreamedResource.Allocator;
import org.interreg.docexplore.reader.plugin.ClientPlugin;
import org.interreg.docexplore.util.ImageUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class VideoClientPlugin implements ClientPlugin
{
	static BufferedImage largeMediaImage = null, helpOverlay = null;
	
	public void setHost(final ReaderClient client, File jarFile, File dependencies)
	{
		try {Utils.init(jarFile, dependencies);}
		catch (Exception e) {throw new RuntimeException(e);}
		
		client.registerStreamType(StreamedMedia.class, allocator);
		
//		new Thread()
//		{
//			public void run()
//			{
//				while (client.socket == null)
//					try {Thread.sleep(1000);}
//					catch (Exception e) {}
//				client.getResource(StreamedMedia.class, "transcoded.mp4");
//				//client.getResource(StreamedMedia.class, "ambient.mp3");
//			}
//		}.start();
		if (largeMediaImage == null)
			try
			{
				JarFile vlcJar = new JarFile(jarFile);
				largeMediaImage = ImageUtils.read(vlcJar.getInputStream(vlcJar.getEntry("org/interreg/docexplore/video/vlc/media-256x256.png")));
				helpOverlay = ImageUtils.read(vlcJar.getInputStream(vlcJar.getEntry("org/interreg/docexplore/video/vlc/help.png")));
				vlcJar.close();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	static Set<String> handledTypes = new TreeSet<String>();
	static
	{
		handledTypes.add("media");
		handledTypes.add("video");
		handledTypes.add("sound");
	}
	public Set<String> getHandledTypes() {return handledTypes;}
	
	public static class MediaInfoElement implements InfoElement
	{
		String uri;
		int width, height;
		
		public MediaInfoElement(String uri, int width, int height)
		{
			this.uri = uri;
			this.width = width;
			this.height = height;
		}
	}
	public ROISpecification.InfoElement buildInfoElement(Node element, NamedNodeMap atts, String baseUrl) throws Exception
	{
		int width = -1, height = -1;
		Node widthNode = atts.getNamedItem("width");
		if (widthNode != null)
			width = Integer.parseInt(widthNode.getNodeValue());
		Node heightNode = atts.getNamedItem("height");
		if (heightNode != null)
			height = Integer.parseInt(heightNode.getNodeValue());
		Node srcNode = atts.getNamedItem("src");
		if (srcNode == null)
			throw new Exception("Missing src attribute in media info tag!");
		return new MediaInfoElement(baseUrl+srcNode.getNodeValue(), width, height);
	}
	
	public boolean buildOverlayElement(ReaderApp app, List<OverlayElement> elements, InfoElement infoElement, int width)
	{
		if (!(infoElement instanceof MediaInfoElement))
			return false;
		MediaOverlayElement media = new MediaOverlayElement(app, (MediaInfoElement)infoElement);
		elements.add(media);
		elements.add(new MediaControlElement(app, width, media));
		return true;
	}

	static Allocator<StreamedMedia> allocator = new Allocator<StreamedMedia>()
	{
		public StreamedMedia allocate(ReaderClient client, String uri, File file) {return new StreamedMedia(client, uri, file);}
		public StreamedMedia cast(StreamedResource stream) {return (StreamedMedia)stream;}
	};
}
