package org.interreg.docexplore.video.vlc;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;

import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class MediaPreview extends PreviewPanel
{
	MediaPanel panel;
	
	public MediaPreview()
	{
		this.panel = new MediaPanel(16);
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		panel.setPreferredSize(new Dimension(400, 400));
		add(panel);
	}
	
	public void dispose() {if (panel.player != null) panel.player.dispose();}
	
	public void set(final Object object)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try
				{
					File file = null;
					if (object instanceof File)
						file = (File)object;
					else if (object instanceof MetaData)
						file = DocExploreDataLink.getOrExtractMetaDataFile((MetaData)object);
					else return;
					MediaReader reader = new MediaReader(getPreferredSize().width, getPreferredSize().height);
					panel.set(reader);
					reader.startMedia(file.getAbsolutePath());
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
		}, null);
	}
}
