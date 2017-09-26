package org.interreg.docexplore.video.vlc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class VideoExportOptions extends JPanel
{
	JCheckBox convertBox;
	
	public VideoExportOptions()
	{
		super(new BorderLayout());
		JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.convertBox = new JCheckBox();
		convertBox.setSelected(true);
		checkboxPanel.add(convertBox);
		checkboxPanel.add(new JLabel(
			Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
				"<html>Convertir les vidéos pour maximiser la comptatibilité" :
				"<html>Convert videos for highest compatibility"
			));
		add(checkboxPanel, BorderLayout.CENTER);
		add(new JLabel(
			Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
				"<html><div style=\"width: 360px;\">"
				+ "Cette option permet de réencoder les vidéos contenues dans la présentation en un format qui est supporté par "
				+ "un grand nombre de plateformes, en particulier les navigateurs web et les appareils mobiles. Si cet export "
				+ "est destiné au feuilleteur DocExplore, cette conversion n'est généralement pas nécessaire. <u>Notez qui si vous "
				+ "activez cette option, le temps de traitement de l'export peut augmenter considérablement.</u></div></html>" :
				"<html><div style=\"width: 360px;\">"
				+ "This option will reencode the videos contained in the presentation in a format which is supported by most "
				+ "platforms (web browsers and mobile devices in particular). If you are exporting to the DocExplore reader, "
				+ "this conversion isn't necessary in most cases. <u>Please note that if this option is enabled, the processing "
				+ "time required to complete the export may increase significantly.</u></div></html>"
			), BorderLayout.SOUTH);
		setBorder(BorderFactory.createTitledBorder(
			Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
					"<html>Convertir les vidéos" :
					"<html>Convert videos"
				));
	}
	
	boolean shouldConvert() {return convertBox.isSelected();}
}
