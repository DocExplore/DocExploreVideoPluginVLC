package org.interreg.docexplore.video.vlc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JPanel;

import org.interreg.docexplore.util.StringUtils;

import uk.co.caprica.vlcj.binding.internal.libvlc_state_t;

import com.sun.jna.Memory;

@SuppressWarnings("serial")
public class MediaPanel extends JPanel implements MediaReader.MediaListener
{
	MediaReader player;
	BufferedImage image;
	double pos, ratio;
	int controlHeight;
	
	public MediaPanel(final int controlHeight)
	{
		this.pos = 0;
		this.ratio = 1;
		this.controlHeight = controlHeight;
		
		setPreferredSize(new Dimension(MediaReader.defaultWidth, MediaReader.defaultHeight));
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (player == null)
					return;
				if (player.getMediaState() == libvlc_state_t.libvlc_Ended)
				{
					player.stop();
					player.start();
					player.pause();
				}
				
				float pos = getSeekPosition(e.getX(), e.getY(), 0, getHeight()-controlHeight, getWidth(), controlHeight);
				if (pos >= 0)
				{
					player.setPosition(pos);
					MediaPanel.this.pos = pos;
				}
				else if (e.getY() < getHeight()-controlHeight || getPlaybackToggle(e.getX(), e.getY(), 0, getHeight()-controlHeight, getWidth(), controlHeight))
				{
					if (player.getMediaPlayerState() == libvlc_state_t.libvlc_Playing) player.pause();
					else player.start();
				}
				
				repaint();
			}
		});
	}
	
	public void set(MediaReader player)
	{
		if (this.player != null)
		{
			this.player.removeMediaListener(this);
			this.player.dispose();
		}
		this.player = player;
		this.image = new BufferedImage(player.width, player.height+controlHeight, BufferedImage.TYPE_3BYTE_BGR);
		setPreferredSize(new Dimension(player.width, player.height));
		player.addMediaListener(this);
		pos = 0;
		
		final MediaReader monitored = player;
		new Thread() {public void run()
		{
			while (!monitored.released)
				try
				{
					Thread.sleep(300);
					pos = monitored.getPosition(); 
					if (monitored.getMediaPlayerState() == libvlc_state_t.libvlc_Playing)
						repaint();
				}
				catch (Exception e) {}
		}}.start();
	}

	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.black);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if (player != null)
		{
			int h = getHeight(), w = (int)(ratio*getHeight());
			if (w > getWidth())
				{w = getWidth(); h = (int)(getWidth()/ratio);}
			g2.drawImage(image, (getWidth()-w)/2, (getHeight()-h)/2, w, h, null);
			drawControls(g2, (float)pos, player.getLength(), player.getMediaPlayerState() == libvlc_state_t.libvlc_Playing, 0, getHeight()-controlHeight, getWidth(), controlHeight);
		}
		else drawControls(g2, 0, 0, false, 0, getHeight()-controlHeight, getWidth(), controlHeight);
	}

	public void mediaProduced(Memory nativeBuffer)
	{
		byte [] buffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		nativeBuffer.read(0, buffer, 0, (int)nativeBuffer.size());//buffer.length);
		Dimension dim = player.getVideoDimension();
		ratio = dim == null ? 1 : dim.getWidth()*1./dim.getHeight();
		repaint();
	}
	
	static Color blueGray = new Color(.5f, .5f, .8f);
	static int [] xs = {0, 0, 0}, ys = {0, 0, 0};
	static Font font = Font.decode("Tahoma-Bold-11");
	public static void drawControls(Graphics2D g, float pos, long length, boolean playing, int x, int y, int w, int h)
	{
		int margin = h/8;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.gray);
		g.fillRect(x, y, w, h);
		
		g.setColor(Color.lightGray);
		int bx = getButtonX(x, y, w, h), bw = getButtonW(bx, y, w, h);
		if (!playing)
		{
			xs[0] = bx+margin; xs[1] = bx+bw-margin; xs[2] = bx+margin;
			ys[0] = y+margin; ys[1] = y+h/2; ys[2] = y+h-margin;
			g.fillPolygon(xs, ys, 3);
		}
		else
		{
			g.fillRect(bx+margin, y+margin, (bw-2*margin)/3, h-2*margin);
			g.fillRect(bx+margin+2*(bw-2*margin)/3, y+margin, (bw-2*margin)/3, h-2*margin);
		}
		
		int fontSize = (int)(h/1.4);
		if (font.getSize() != fontSize)
			font = Font.decode("Tahoma-Bold-"+fontSize);
		g.setFont(font);
		g.drawString(pos < 0 ? "-:--:--" : StringUtils.formatMillis((long)(pos*length), false), getCurrentX(x, y, w, h)+margin, y+h-2*margin);
		g.drawString(length == 0 ? "-:--:--" : StringUtils.formatMillis(length, false), getTotalX(x, y, w, h)+margin, y+h-2*margin);
		
		int sx = getSliderX(x, y, w, h), sw = getSliderW(x, y, w, h);
		g.fillRoundRect(sx+margin, y+margin, sw-2*margin, h-2*margin, 3*h/4, 3*h/4);
		
		g.setColor(blueGray);
		int cx = (int)(sx+2*margin+pos*(sw-4*margin)), cr = (h-2*margin)/2;
		g.fillOval((int)(cx-cr), y+margin, 2*cr, 2*cr);
	}
	
	public static int getButtonX(int x, int y, int w, int h) {return x;}
	public static int getButtonW(int x, int y, int w, int h) {return h;}
	public static int getCurrentX(int x, int y, int w, int h) {return getButtonX(x, y, w, h)+getButtonW(x, y, w, h);}
	public static int getCurrentW(int x, int y, int w, int h) {return 3*h;}
	public static int getSliderX(int x, int y, int w, int h) {return getCurrentX(x, y, w, h)+getCurrentW(x, y, w, h);}
	public static int getSliderW(int x, int y, int w, int h) {return w-getButtonW(x, y, w, h)-getCurrentW(x, y, w, h)-getTotalW(x, y, w, h);}
	public static int getTotalX(int x, int y, int w, int h) {return w-getTotalW(x, y, w, h);}
	public static int getTotalW(int x, int y, int w, int h) {return 3*h;}
	public static float getSeekPosition(int mx, int my, int x, int y, int w, int h)
	{
		if (my < y || my > y+h)
			return -1;
		int sx = getSliderX(x, y, w, h), sw = getSliderW(x, y, w, h);
		if (mx < sx || mx > sx+sw)
			return -1;
		int margin = h/8;
		sx += 2*margin; sw -= 4*margin;
		float pos = (mx-sx)*1f/sw;
		return pos < 0 ? 0 : pos > 1 ? 1 : pos;
	}
	public static boolean getPlaybackToggle(int mx, int my, int x, int y, int w, int h)
	{
		return mx > getButtonX(x, y, w, h) && mx < getButtonX(x, y, w, h)+getButtonW(x, y, w, h);
	}
}
