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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.authoring.ExportDialogOld;
import org.interreg.docexplore.authoring.ExportOptions;
import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.authoring.rois.RegionSidePanel;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.FileDialogs;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.MMTAnnotationPanel;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.ImageUtils;

public class VideoPlugin implements MetaDataPlugin
{
	FileDialogs.Category mediaCategory;
	
	public String getName() {return "DocExplore VLC Video Plugin";}

	public void setHost(File jarFile, File dependencies)
	{
		try {Utils.init(jarFile, dependencies);}
		catch (Exception e) {throw new RuntimeException(e);}
		
		try
		{
			mediaCategory = DocExploreTool.getFileDialogs().getOrCreateCategory("Media", suffixes);
			JarFile vlcJar = new JarFile(jarFile);
			mediaIcon = new ImageIcon(ByteUtils.readStream(vlcJar.getInputStream(vlcJar.getEntry("org/interreg/docexplore/video/vlc/media-file-48x48.png"))));
			vlcJar.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}

	public String getType() {return "vid";}

	public AnnotationEditor createEditor(MMTAnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		return new VideoEditor(this, panel, annotation);
	}

	public JLabel createLabel(String keyName, MetaData annotation)
	{
		return new JLabel("<html><b>"+keyName+"</b></html>", ImageUtils.getIcon("video-32x32.png"), SwingConstants.HORIZONTAL);
	}

	String lastFileSelected = null;
	public Object createDefaultValue()
	{
		File selected = DocExploreTool.getFileDialogs().openFile(mediaCategory);
		if (selected != null)
		{
			lastFileSelected = selected.getName();
			return selected;
		}
		return null;
	}
	public Collection<File> openFiles(boolean multiple)
	{
		File [] files = {null};
		if (!multiple)
			files[0] = DocExploreTool.getFileDialogs().openFile(mediaCategory);
		else files = DocExploreTool.getFileDialogs().openFiles(mediaCategory);
		if (files != null && files.length > 0 && files[0] != null)
			try {return Arrays.asList(files);}
			catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	public InfoElement createInfoElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException
	{
		return new MediaAuthoringElementOld(editor, md, width);
	}
	public org.interreg.docexplore.authoring.rois.InfoElement createInfoElement(RegionSidePanel editor, MetaData md, int width) throws DataLinkException
	{
		return new MediaAuthoringElement(editor, md, width);
	}
	
	static Set<String> suffixes = new TreeSet<String>();
	{
		suffixes.add("mpg");
		suffixes.add("mp4");
		suffixes.add("mov");
		suffixes.add("3gp");
		suffixes.add("avi");
		suffixes.add("flv");
		suffixes.add("mp3");
		suffixes.add("wav");
		suffixes.add("aif");
		suffixes.add("asf");
		suffixes.add("wmv");
		suffixes.add("wma");
		suffixes.add("ogg");
		suffixes.add("ogm");
		suffixes.add("mkv");
		suffixes.add("mpeg");
		suffixes.add("flac");
	}
	public boolean canPreview(Object object)
	{
		if (object instanceof File)
		{
			File file = (File)object;
			int ind = file.getName().lastIndexOf('.');
			if (ind < 0)
				return false;
			String suffix = file.getName().substring(ind+1);
			if (suffix.length() > 4)
				return false;
			return suffixes.contains(suffix);
		}
		else if (object instanceof MetaData)
			return ((MetaData)object).getType().equals(getType());
		
		return false;
	}
	
	public PreviewPanel getPreview(Object object, int mx, int my)
	{
		MediaPreview preview = new MediaPreview();
		preview.setPreview(object, mx, my);
		preview.setVisible(true);
		return preview;
	}
	
	Icon mediaIcon = null;
	public Icon createIcon(Object object) {return mediaIcon;}
	
	public String getFileType() {return "Media";}
	
	public final static String optionKey = "DocExploreVideoPluginVLC";
	public void exportMetaData(MetaData md, StringBuffer xml, File bookDir, int id, ExportOptions options, int exportType)
	{
		try
		{
			InputStream in = md.getValue();
			File dest = null;
			if (((VideoExportOptions)options.getPluginPanel(optionKey)).shouldConvert())
				dest = Utils.webConversion(in, bookDir, "media"+id);
			if (dest == null || !dest.exists())
			{
				String ext = null;
				List<MetaData> mds = md.getMetaDataListForKey(md.getLink().getOrCreateKey("source-uri"));
				if (mds.size() > 0)
				{
					String uri = mds.get(0).getString();
					int dot = uri.lastIndexOf('.');
					if (dot >= 0)
					{
						String end = uri.substring(dot+1).toLowerCase();
						if (suffixes.contains(end))
							ext = end;
					}
				}
				dest = new File(bookDir, "media"+id+(ext != null ? "."+ext : ""));
				FileOutputStream output = new FileOutputStream(dest);
				ByteUtils.writeStream(md.getValue(), output);
				output.close();
			}
			
//			Map<String, String> props = Utils.readProperties(dest);
//			int width = props.get("width") == null ? 400 : Integer.parseInt(props.get("width"));
//			int height = props.get("height") == null ? 400 : Integer.parseInt(props.get("height"));
			int width = 400, height = 400;
			
			xml.append("\t\t\t<Info type=\"media\" src=\"").append(dest.getName())
				.append("\" width=\"").append(width).append("\" height=\"").append(height).append("\" />\n");
		}
		catch (Exception e) {e.printStackTrace();}
	}

	@Override public void setupExportOptions(ExportOptions options, int exportType)
	{
		if (options.getPluginPanel(optionKey) == null)
			options.addPluginPanel(optionKey, new VideoExportOptions());
		VideoExportOptions voptions = (VideoExportOptions)options.getPluginPanel(optionKey);
		voptions.convertBox.setSelected(exportType != ExportDialogOld.ReaderExport);
	}
}
