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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JOptionPane;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ProcessDialog;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.GuiUtils;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.logger.Logger.Level;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Utils
{
	public static boolean inited = false;
	public static File handBrake = null;
	public static synchronized void init(File jarFile, File dependencies) throws IOException
	{
		if (inited)
			return;
		
		File vlcLibs = new File(dependencies, "libs");
		
		boolean firstInit = false;
		if (!vlcLibs.exists())
		{
			vlcLibs.mkdir();
			
			JarFile vlcJar = new JarFile(jarFile);
			Enumeration<JarEntry> entries = vlcJar.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (!entry.getName().startsWith("libs-") || entry.getName().endsWith("/"))
					continue;
				add(vlcJar, entry, vlcLibs);
			}
			vlcJar.close();
			firstInit = true;
		}
		
		System.out.println("VLC native library dir: "+vlcLibs.getCanonicalPath()+File.separator+"lib");
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibs.getCanonicalPath()+File.separator+"lib");
		try
		{
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
			LibXUtil.initialise();
		}
		catch (Throwable ex)
		{
			if (firstInit && System.getProperty("os.name").toLowerCase().contains("linux"))
			{
				JOptionPane.showMessageDialog(null, "You must restart DocExplore to enable the video plugin");
				System.exit(0);
			}
		}
		Logger.setLevel(Level.FATAL);
		inited = true;
		
		File [] files = new File(vlcLibs, "lib").listFiles();
		if (files != null)
			for (int i=0;i<files.length;i++)
				if (files[i].getName().startsWith("HandBrake"))
					{handBrake = files[i]; break;}
		if (handBrake != null && System.getProperty("os.name").toLowerCase().contains("mac"))
			try {Runtime.getRuntime().exec(new String [] {"chmod", "+x", handBrake.getAbsolutePath()});}
			catch (Exception e) {e.printStackTrace();}
		//System.out.println(handBrake.getAbsolutePath());
	}
	
	static void add(JarFile jarFile, JarEntry entry, File libs) throws IOException
	{
		String [] path = entry.getName().split("/");
		File cur = libs;
		for (int i=1;i<path.length-1;i++)
			cur = new File(cur, path[i]);
		cur.mkdirs();
		
		byte [] bytes = ByteUtils.readStream(jarFile.getInputStream(entry));
		if (bytes == null)
			throw new IOException("Can't extract "+entry.getName());
		
		FileOutputStream output = new FileOutputStream(new File(cur, path[path.length-1]));
		output.write(bytes);
		output.close();
	}
	
	static File webConversion(InputStream in, File outDir, String outName)
	{
		//on linux check if handbrake is on the path or prompt user to install it
		String os = System.getProperty("os.name");
		boolean linux = os.toLowerCase().contains("linux");
		if (linux)
		{
			while (!isHandbrakeOnLinuxPath() &&
				JOptionPane.showConfirmDialog(null, "<html><div style=\"width: 480px\">"
					+ (Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
						"<b>HandBrakeCLI ext introuvable.</b><br/><br/>"
						+ "Afin de permettre les conversions vidéos, HandBrakeCLI doit être installé sur votre ordinateur. "
						+ "Rendez-vous sur cette page pour obtenir les instructions de téléchargement :<br/> "
						+ "<a href=\"https://handbrake.fr/downloads2.php\">https://handbrake.fr/downloads2.php</a><br/>"
						+ "<b>Vérifié en avril 2017 : n'utilisez pas le dépôt standard Ubunutu, <u>cette version ne fonctionne pas</u>. "
						+ "Utilisez le lien fourni ci-dessus.</b><br/><br/>"
						+ "Voulez-vous que DocExplore tente à nouveau de détecter HandBrakeCLI?<br/>"
						+ "<i>Si vous cliquez sur 'Non', aucune conversion ne sera effectuée</i>" : 
						"<b>Could not find HandBrakeCLI.</b><br/><br/>"
						+ "In order to enable video conversions, you must have HandBrakeCLI installed on your computer. "
						+ "Please visit this page for download instructions:<br/> "
						+ "<a href=\"https://handbrake.fr/downloads2.php\">https://handbrake.fr/downloads2.php</a><br/>"
						+ "<b>As of April 2017, do not use the default Ubuntu repository, <u>it will not work</u>. "
						+ "Instead, follow the link above.</b><br/><br/>"
						+ "Would you like DocExplore to retry the detection of HandBrakeCLI?<br/>"
						+ "<i>No conversions will be performed if you click on 'No'</i>")
					+ "</div></html>", 
					(Locale.getDefault().getLanguage().toLowerCase().equals("fr") ? "Conversion vidéo" : "Video conversion"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
			if (!isHandbrakeOnLinuxPath())
				return null;
		}
		else if (handBrake == null)
			return null;
		
		File tmpFile = new File(DocExploreTool.getHomeDir(), "tmpVidFile");
		try
		{
			FileOutputStream output = new FileOutputStream(tmpFile);
			ByteUtils.writeStream(in, output);
			output.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		File dest = new File(outDir, outName+".mp4");
		try
		{
			List<String> command = new LinkedList<String>(Arrays.asList(new String []
			{
				linux ? "HandBrakeCLI" : handBrake.getAbsolutePath(),
				"-i",
				tmpFile.getAbsolutePath(),
				"-o",
				dest.getAbsolutePath(),
				"-f",
				linux ? "mp4" : "av_mp4",
				"-e",
				"x264",
				"-E",
				linux ? "ffaac" : "av_aac"
			}));
			
			if (os.toLowerCase().contains("win"))
				command.addAll(0, Arrays.asList(new String [] {"CMD", "/C", "start"}));
			
			ProcessBuilder pb = new ProcessBuilder(command);
			Process p = pb.start();
			//p.waitFor();
			
			if (os.toLowerCase().contains("win"))
			{
				String line = null;
				BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        while ((line = stdOut.readLine()) != null) {System.out.println(line);}
		        stdOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		        while ((line = stdOut.readLine()) != null) {System.out.println(line);}
			}
			else
			{
				ProcessDialog pd = new ProcessDialog(outName+" conversion...");
				GuiUtils.centerOnScreen(pd);
				pd.monitor(p);
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (tmpFile.exists())
			tmpFile.delete();
		
		return dest;
	}
	
	static boolean isHandbrakeOnLinuxPath()
	{
		boolean found = false;
		try
		{
			Process p = Runtime.getRuntime().exec("whereis -b HandBrakeCLI");
			String line = null;
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while ((line = stdOut.readLine()) != null) {if (!found) found = line.contains("/");}
		}
		catch (Exception e) {e.printStackTrace();}
		return found;
	}
	
	public static void main(String [] args) throws Exception
	{
		File handBrake = new File("C:\\Users\\aburn\\DocExplorePlugins\\dependencies\\DocExploreVLCPlugin\\libs\\lib\\HandBrakeCLI.exe");
		File tmpFile = new File("C:\\Users\\aburn\\DocExplore\\tmpVidFile");
		File dest = new File("C:\\Users\\aburn\\DocExplore\\web-tmp\\book0\\media1.mp4");
		
		
		
		String [] command = new String []
		{
			handBrake.getAbsolutePath(),
			"-i",
			tmpFile.getName(),
			"-o",
			dest.getAbsolutePath(),
			"-f",
			"av_mp4",
			"-e",
			"x264",
			"-E",
			"av_aac"
		};
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(tmpFile.getParentFile());
		Process p = pb.start();//Runtime.getRuntime().exec(command, null, handBrake.getParentFile());
		//p.waitFor();
		String line = null;
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = stdOut.readLine()) != null) {System.out.println(line);}
	}
}
