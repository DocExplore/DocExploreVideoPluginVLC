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
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.AnnotationPanel;
import org.interreg.docexplore.manuscript.MetaData;

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
	
	public VideoEditor(VideoPlugin plugin, AnnotationPanel panel, final MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		this.plugin = plugin;
		this.value = null;//DocExploreDataLink.getOrExtractMetaDataFile(annotation);
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
				XMLResourceBundle.getString("management-lrb", "annotateBrowseLabel"));
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
