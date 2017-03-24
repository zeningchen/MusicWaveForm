import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Point2D.Float;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

import java.lang.Math;


public class MusicWindow extends JFrame {
	private MyPanel mP;

	public MusicWindow()
	{
		Dimension d = new Dimension(Global.SCREEN_WIDTH, Global.SCREEN_HEIGHT);
		mP = new MyPanel(d, Global.BGROUND_COLOR);
		this.add(mP, BorderLayout.CENTER);
		this.pack();
		
        /*Set the background, size and window change states*/
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                MusicWindow mw = new MusicWindow();
                mw.setVisible(true);
            }
        });  
	}
}
