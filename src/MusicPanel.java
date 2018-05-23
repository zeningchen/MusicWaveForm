import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

import javax.swing.JPanel;

class MusicPanel extends JPanel
{
	RenderingHints rh;
	
	/*Class that contains all of the music Info*/
	MusicInfo mInfo;
	Object imageLock = new Object();
	
	public BufferedImage image;
	
    private final Path2D.Float[] paths = {
            new Path2D.Float(), new Path2D.Float(), new Path2D.Float()
        };
	
	
	private void initVars()
	{
		
		/*Points to runnable in this class*/
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
    public void reset() {
        Graphics2D g2d = image.createGraphics();
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
    }
	
	public MusicPanel(Dimension d, Color c)
	{
		/*Setup screen params*/
		super();
        image = (
                GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(
                    d.width, d.height, Transparency.OPAQUE
                )
            );
		this.setPreferredSize(d);
		this.setBackground(c);
	
		/*Initialize the memory reader*/
		initVars();

	}
	
	public void musicDrawMethod(float[] samples, int svalid)
	{
		Graphics2D g2d = image.createGraphics();
        
        /* shuffle */
        
        Path2D.Float current = paths[0];
        
        /* lots of ratios */
        
        float avg = 0f;
        float hd2 = getHeight() / 2f;
        
        final int channels = 2;
        
        /* 
         * have to do a special op for the
         * 0th samples because moveTo.
         * 
         */
        
        int i = 0;
        while(i < channels && i < svalid) {
            avg += samples[i++];
        }
        
        avg /= channels;
        
        current.reset();
        current.moveTo(0, hd2 - avg * hd2);
        
        int fvalid = svalid / channels;
        int ch, frame;
        for(ch=0, frame = 0; i < svalid; frame++) {
            avg = 0f;
            
            /* average the channels for each frame. */
            
            for(ch = 0; ch < channels; ch++) {
                avg += samples[i++];
            }
            
            avg /= channels;
            
            current.lineTo(
                ((float)frame / (float)fvalid) * image.getWidth(), hd2 - avg * hd2
            );
        }
        //System.out.format("frame =%d", frame);
        paths[0] = current;
        
        
        synchronized(image) {
            g2d.setBackground(Color.BLACK);
            g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
            //System.out.format("Image width = %d\n", image.getWidth());
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2d.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
            );
            
            g2d.setPaint(Color.WHITE);
            g2d.draw(paths[0]);
        }
        
        g2d.dispose();
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		synchronized(image) {
			g.drawImage(image, 0, 0, null);
		}
	}
}