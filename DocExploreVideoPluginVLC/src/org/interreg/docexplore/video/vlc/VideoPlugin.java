package org.interreg.docexplore.video.vlc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.FileDialogs;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.AnnotationPanel;
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

	public AnnotationEditor createEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		return new VideoEditor(this, panel, annotation);
	}

	public JLabel createLabel(String keyName, MetaData annotation)
	{
		return new JLabel("<html><b>"+keyName+"</b></html>", ImageUtils.getIcon("video-32x32.png"), SwingConstants.HORIZONTAL);
	}

	public InputStream createDefaultValue()
	{
		File selected = DocExploreTool.getFileDialogs().openFile(mediaCategory);
		if (selected != null)
			try {return new FileInputStream(selected);}
			catch (Exception e) {e.printStackTrace();}
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
	
	public void exportMetaData(MetaData md, StringBuffer xml, File bookDir, int id)
	{
		try
		{
			File dest = new File(bookDir, "media"+id);
			FileOutputStream output = new FileOutputStream(dest);
			ByteUtils.writeStream(md.getValue(), output);
			output.close();
			
//			Map<String, String> props = Utils.readProperties(dest);
//			int width = props.get("width") == null ? 400 : Integer.parseInt(props.get("width"));
//			int height = props.get("height") == null ? 400 : Integer.parseInt(props.get("height"));
			int width = 400, height = 400;
			
			xml.append("\t\t\t<Info type=\"media\" src=\"").append("media").append(id)
				.append("\" width=\"").append(width).append("\" height=\"").append(height).append("\" />\n");
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
