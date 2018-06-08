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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import org.interreg.docexplore.authoring.rois.InfoElement;
import org.interreg.docexplore.authoring.rois.RegionSidePanel;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;

@SuppressWarnings("serial")
public class MediaAuthoringElement extends InfoElement
{
	MediaPanel panel;
	
	public MediaAuthoringElement(RegionSidePanel editor, MetaData md, int width) throws DataLinkException
	{
		super(editor, md);
		
		File file = DocExploreDataLink.getOrExtractMetaDataFile(md);
		if (file == null)
			throw new DataLinkException(md.getLink().getLink(), "Couldn't get file for "+md.getType()+" "+md.getCanonicalUri());
		
		MediaReader reader = null;
		try {reader = new MediaReader(width, width);}
		catch (Exception e) {throw new DataLinkException(md.getLink().getLink(), e);}
		
		this.panel = new MediaPanel(16);
		panel.set(reader);
		try {panel.player.startMedia(file.getCanonicalPath());}
		catch (Exception e) {throw new DataLinkException(md.getLink().getLink(), e);}
		panel.player.pause();
		
		add(panel);
	}

	public void dispose()
	{
		if (panel != null && panel.player != null)
			panel.player.dispose();
	}

	public BufferedImage getPreview(int width, Color back)
	{
		BufferedImage res;
		if (panel.image == null)
			res = new BufferedImage(width, 9*width/16, BufferedImage.TYPE_3BYTE_BGR);
		else
		{
			int height = (int)(width/panel.ratio);
			res = new BufferedImage(width, height+MediaControlElement.controlHeight, BufferedImage.TYPE_3BYTE_BGR);
			res.createGraphics().drawImage(panel.image, 0, 0, width, height, 0, 0, panel.image.getWidth(), 
				(panel.image.getHeight()-MediaControlElement.controlHeight), null);
			MediaPanel.drawControls(res.createGraphics(), 0, panel.player.getLength(), false, 0, height, width, MediaControlElement.controlHeight);
		}
		return res;
	}
}
