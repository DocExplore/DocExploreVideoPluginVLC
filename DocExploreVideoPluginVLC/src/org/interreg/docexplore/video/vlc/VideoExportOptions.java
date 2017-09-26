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
				"<html>Convertir les vid�os pour maximiser la comptatibilit�" :
				"<html>Convert videos for highest compatibility"
			));
		add(checkboxPanel, BorderLayout.CENTER);
		add(new JLabel(
			Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
				"<html><div style=\"width: 360px;\">"
				+ "Cette option permet de r�encoder les vid�os contenues dans la pr�sentation en un format qui est support� par "
				+ "un grand nombre de plateformes, en particulier les navigateurs web et les appareils mobiles. Si cet export "
				+ "est destin� au feuilleteur DocExplore, cette conversion n'est g�n�ralement pas n�cessaire. <u>Notez qui si vous "
				+ "activez cette option, le temps de traitement de l'export peut augmenter consid�rablement.</u></div></html>" :
				"<html><div style=\"width: 360px;\">"
				+ "This option will reencode the videos contained in the presentation in a format which is supported by most "
				+ "platforms (web browsers and mobile devices in particular). If you are exporting to the DocExplore reader, "
				+ "this conversion isn't necessary in most cases. <u>Please note that if this option is enabled, the processing "
				+ "time required to complete the export may increase significantly.</u></div></html>"
			), BorderLayout.SOUTH);
		setBorder(BorderFactory.createTitledBorder(
			Locale.getDefault().getLanguage().toLowerCase().equals("fr") ?
					"<html>Convertir les vid�os" :
					"<html>Convert videos"
				));
	}
	
	boolean shouldConvert() {return convertBox.isSelected();}
}
