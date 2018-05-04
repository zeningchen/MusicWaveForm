
import java.io.File;
import java.io.IOException;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

enum PlayState {
    NO_FILE, PLAYING, PAUSED, STOPPED
}

class PlayStateRef {
	public PlayState pS;
	
	public PlayStateRef() {
		this.pS = PlayState.NO_FILE;
	}
}

class ToolsButton
extends JButton {
    public ToolsButton(String text) {
        super(text);
        
        setOpaque(true);
        setBorderPainted(true);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        
        setBorder(new LineBorder(Color.GRAY) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 4, 1, 4);
            }
            @Override
            public Insets getBorderInsets(Component c, Insets i) {
                return getBorderInsets(c);
            }
        });
        
        Font font = getFont();
        setFont(font.deriveFont(font.getSize() - 1f));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if(me.getButton() == MouseEvent.BUTTON1) {
                    setForeground(Color.BLUE);
                }
            }
            @Override
            public void mouseReleased(MouseEvent me) {
                if(me.getButton() == MouseEvent.BUTTON1) {
                    setForeground(Color.WHITE);
                }
            }
        });
    }
}

interface PlayerRef {
    public Object getLock();
    public PlayState getState();
    public File getFile();
    public void playbackEnded();
    public void drawDisplay(float[] samples, int svalid);
}

public class MusicPlayer
implements ActionListener{
	private MusicPanel mPanel;
	private Dimension d;
	private MusicWindow mWindow;
	private JToolBar playbackTools;
	private MusicScreen playerRef;
	private File audioFile;
	private AudioFormat audioFormat;
	private volatile PlayStateRef playState = new PlayStateRef();
	
	/*Initialize these files for text saving purposes*/
    private ToolsButton bOpen = new ToolsButton("Open");
    private ToolsButton bPlay = new ToolsButton("Play");
    private ToolsButton bPause = new ToolsButton("Pause");
    private ToolsButton bStop = new ToolsButton("Stop");
    private JLabel fileLabel = new JLabel("No file loaded");
    private JPanel contentPane = new JPanel(new BorderLayout());
    private Object stateLock = new Object();
    
    
    private static void showError(Throwable t) {
        JOptionPane.showMessageDialog(null,
            "Exception <" + t.getClass().getName() + ">" +
            " with message '" + t.getMessage() + "'.",
            "There was an error",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    private void loadAudio() {
        JFileChooser openDiag = new JFileChooser();
        File selected = null;
        if(JFileChooser.APPROVE_OPTION == openDiag.showOpenDialog(mWindow)) {
            selected = openDiag.getSelectedFile();
        	//AudioFileFormat fmt = AudioSystem.getAudioFileFormat(selected);
            audioFile = selected;
            this.playerRef.setAudioFile(audioFile);
            //audioFormat = fmt.getFormat();
            fileLabel.setText(audioFile.getName());
            playState.pS = PlayState.STOPPED;
        }
    }
    
    private void systemExit() {
        boolean wasPlaying;
        synchronized(stateLock) {
            if(wasPlaying = (playState.pS == PlayState.PLAYING)) {
                playState.pS = PlayState.STOPPED;
            }
        }
        
        mWindow.setVisible(false);
        mWindow.dispose();
        
        if(wasPlaying) {
            /* 
             * helps prevent 'tearing' sound
             * if exit happens while during playback
             * 
             */
            try {
                Thread.sleep(250L);
            } catch(InterruptedException ie) {}
        }
        
        System.exit(0);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicPlayer();
            }
        });
    }
	
    
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if(source == bOpen) {
            synchronized(stateLock) {
                if(playState.pS == PlayState.PLAYING) {
                    playState.pS = PlayState.STOPPED;
                }
            }
            loadAudio();
        } else if(source == bPlay && audioFile != null && playState.pS != PlayState.PLAYING) {
        	synchronized(stateLock) {
                switch(playState.pS) {
                    
                    case STOPPED: {
                        playState.pS = PlayState.PLAYING;
                        MusicReader mR = new MusicReader(audioFile, playerRef);
                        mR.execute();
                        break;
                    }
                        
                    case PAUSED: {
                        playState.pS = PlayState.PLAYING;
                        stateLock.notifyAll();
                        break;
                    }
                    
                    default:
                    	break;
                }
            }
        } else if(source == bPause
                && playState.pS == PlayState.PLAYING) {
            
            synchronized(stateLock) {
                playState.pS = PlayState.PAUSED;
            }  
        } else if(source == bStop
                && (playState.pS == PlayState.PLAYING || playState.pS == PlayState.PAUSED)) {
            
            synchronized(stateLock) {
                switch(playState.pS) {
                    
                    case PAUSED: {
                        playState.pS = PlayState.STOPPED;
                        stateLock.notifyAll();
                        break;
                    }
                        
                    case PLAYING: {
                        playState.pS = PlayState.STOPPED;
                        break;
                    }
                }
            }
        }
	}
	
	public MusicPlayer() {
		assert EventQueue.isDispatchThread();
		playbackTools = new JToolBar();
		d = new Dimension(Global.SCREEN_WIDTH, Global.SCREEN_HEIGHT);
		mWindow = new MusicWindow("Music Player", d);
		mPanel = new MusicPanel(d, Global.BGROUND_COLOR);
		mWindow.setDefaultCloseOperation(MusicWindow.DO_NOTHING_ON_CLOSE);
        mWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                systemExit();
            }
        });
		
		playerRef = new MusicScreen(mPanel, this.stateLock, playState);
		
		playbackTools.setFloatable(false);
        playbackTools.add(bOpen);
        playbackTools.add(bPlay);
        playbackTools.add(bPause);
        playbackTools.add(bStop);
        playbackTools.setBackground(Color.GRAY);
        playbackTools.setMargin(new Insets(0, 24, 0, 0));
		
		bOpen.addActionListener(this);
        bPlay.addActionListener(this);
        bPause.addActionListener(this);
        bStop.addActionListener(this);
        
        fileLabel.setOpaque(true);
        fileLabel.setBackground(Color.BLACK);
        fileLabel.setForeground(Color.WHITE);
        fileLabel.setHorizontalAlignment(JLabel.CENTER);

        contentPane.add(fileLabel, BorderLayout.NORTH);
        contentPane.add(mPanel, BorderLayout.CENTER);
        contentPane.add(playbackTools, BorderLayout.SOUTH);
        
        mWindow.setContentPane(contentPane);
        
        mWindow.pack();
        mWindow.setResizable(false);
        mWindow.setLocationRelativeTo(null);
        mWindow.setVisible(true);
	}
}
