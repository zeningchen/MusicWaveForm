import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

class MyPanel extends JPanel implements Runnable
{
	RenderingHints rh;
	
	Thread waveUpdate;
	
	/*Music Wave Display*/
	MusicWaveForm mWaveForm;
	
	/*Class that reads/buffers/plays music*/
	MusicReader mReader;
	
	/*Class that contains all of the music Info*/
	MusicInfo mInfo;
	
	
	private void initVars()
	{
		/*Setup music reader*/
		mReader = new MusicReader("Sway.mp3");
		
		/*Setting up Music Info*/
		if(mReader != null)
		{
			mInfo = new MusicInfo(mReader.getSampleRate(), 
								  mReader.getSampleRate(), 
								  mReader.getNumChannels(), 
								  mReader.getSampleSize()*mReader.getNumChannels(),
								  Global.TIME_TICK,
								  Global.SAMPLES_PER_TICK);
			
		}
	
		/*Setting up Music waveform*/
		mWaveForm = new MusicWaveForm(Global.TOTAL_TIME_X , mInfo, mReader);
		
		/*Points to runnable in this class*/
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		/*run the waveform*/
		waveUpdate = new Thread(this);
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			if(mReader.getPlayBack())
			{
				repaint();

			}
            try { 
                Thread.sleep(Global.TIME_TICK);
            } catch (InterruptedException ex) {
                
                Logger.getLogger(this.getName()).log(Level.SEVERE, 
                    null, ex);
            }
		}
		
	}
	
	public MyPanel(Dimension d, Color c)
	{
		/*Setup screen params*/
		super();
		this.setPreferredSize(d);
		this.setBackground(c);
	
		/*Initialize the memory reader*/
		initVars();

		/**/
		waveUpdate.start();
		mReader.startMusicPlayer();
	}
	
	private void musicDrawMethod(Graphics2D g2d)
	{
		mWaveForm.drawWave(g2d);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.addRenderingHints(rh);
		g2d.setColor(Global.B_COLOR);
		musicDrawMethod(g2d);
	}
}