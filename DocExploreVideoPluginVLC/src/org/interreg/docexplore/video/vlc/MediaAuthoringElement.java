package org.interreg.docexplore.video.vlc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;

@SuppressWarnings("serial")
public class MediaAuthoringElement extends InfoElement
{
	MediaPanel panel;
	
	public MediaAuthoringElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException
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
