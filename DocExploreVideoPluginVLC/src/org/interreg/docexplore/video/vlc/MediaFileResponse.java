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

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.Response;
import org.interreg.docexplore.reader.net.StreamedResource;

public class MediaFileResponse implements Response
{
	private static final long serialVersionUID = 4676632433906971439L;

	String uri;
	File file;
	
	public MediaFileResponse(String uri, File file)
	{
		this.uri = uri;
		this.file = file;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource res = client.getResource(uri);
		if (res == null)
			return;
		StreamedMedia media = (StreamedMedia)res;
		
		int width = media.width < 0 ? 320 : media.width;
		int height = media.height < 0 ? 240 : media.height;
		if (media.player != null)
			media.player.dispose();
		media.player = new MediaReader(width, height);
		media.image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		media.player.addMediaListener(media);
		if (!media.player.startMedia(file.getAbsolutePath()))
			throw new Exception("Couldn't get stream: "+uri+"!");
		media.player.pause();
	}
}
