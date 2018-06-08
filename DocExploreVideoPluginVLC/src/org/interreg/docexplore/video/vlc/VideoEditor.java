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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.MMTAnnotationPanel;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class VideoEditor extends AnnotationEditor
{
	public static interface StreamSource
	{
		public boolean stream();
		public Object getFile();
		public String getUri();
	}
	
	VideoPlugin plugin;
	File value;
	
	public VideoEditor(VideoPlugin plugin, MMTAnnotationPanel panel, final MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		this.plugin = plugin;
		this.value = null;//DocExploreDataLink.getOrExtractMetaDataFile(annotation);
		keyLabel.setIcon(ImageUtils.getIcon("video-64x64.png"));
	}
	
	MediaPanel videoPanel = null;
	protected void fillExpandedState()
	{
		super.fillExpandedState();
		changed = false;
		
		try
		{
			value = DocExploreDataLink.getOrExtractMetaDataFile(annotation);
			
			JPanel imagePanel = new JPanel(new BorderLayout(5, 10));
			imagePanel.setOpaque(false);
			
			imagePanel.add(buildKeyPanel(), BorderLayout.NORTH);
			
			final JTextField imageField = new JTextField(30);
			imageField.setEditable(false);
			imageField.setText(annotation.getCanonicalUri());
			imagePanel.add(imageField, BorderLayout.CENTER);
			JButton browseButton = new JButton(
				Lang.s("annotateBrowseLabel"));
			if (videoPanel == null)
				videoPanel = new MediaPanel(16);
			
			if (value.length() > 0)
				setupVideo();
			else System.out.println("Empty video!");
			
			browseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					File selected = DocExploreTool.getFileDialogs().openFile(plugin.mediaCategory);
					if (selected != null)
					{
						changed = true;
						value = selected;
						try {imageField.setText(value.getCanonicalPath());}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
						setupVideo();
						
						validate();
						repaint();
					}
				}
			});
			
			JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			browsePanel.setOpaque(false);
			browsePanel.add(browseButton);
			imagePanel.add(browsePanel, BorderLayout.EAST);
			
			videoPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			previewPanel.setOpaque(false);
			previewPanel.add(videoPanel);
			imagePanel.add(previewPanel, BorderLayout.SOUTH);
			imagePanel.setBorder(BorderFactory.createTitledBorder("Media"));
			
			add(imagePanel, BorderLayout.NORTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void disposeExpandedState()
	{
		if (videoPanel.player != null)
		{
			videoPanel.player.dispose();
			videoPanel = null;
		}
		System.out.println("video disposed");
	}
	
	protected void fillContractedState()
	{
		super.fillContractedState();
	}
	
	void setupVideo()
	{
		if (videoPanel == null)
			return;
		try
		{
			videoPanel.set(new MediaReader());
			videoPanel.player.startMedia(value.getCanonicalPath());
			videoPanel.player.pause();
		}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}

	public void writeObject(MetaData object) throws DataLinkException
	{
		annotation.setKey(annotation.getLink().getOrCreateKey(keyName));
		
		try
		{
			File toWrite = DocExploreDataLink.getOrExtractMetaDataFile(annotation);
			if (toWrite.getAbsolutePath().equals(value.getAbsolutePath()))
				{System.out.println("nothing here!"); return;}
			
			annotation.setValue("vid", new FileInputStream(value));
			value = DocExploreDataLink.getOrExtractMetaDataFile(annotation);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		/*GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			ConvertVideo conv = null;
			public void run()
			{
				File input = value;
				File tempDir = new File("temp");
				tempDir.mkdirs();
				File output = new File(tempDir, "transcoded.flv");
				
				conv = new ConvertVideo(input, output, 360);
				conv.run();
				
				try
				{
					annotation.setValue("vid", new FileInputStream(output));
					value = DocExploreDataLink.getOrExtractMetaDataFile(annotation);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				
				//output.delete();
			}
			public float getProgress()
			{
				if (conv == null)
					return 0;
				return (float)conv.progress;
			}
		}, this, "Conversion...");*/
	}

}
