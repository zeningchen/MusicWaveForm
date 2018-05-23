import javax.sound.sampled.*;
import javax.swing.SwingWorker;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class MusicReader extends SwingWorker<Void, Void>
{
	/*Music Info*/
	private float sampleRate = Global.SAMPLE_RATE;
	private int sampleSize = Global.SAMPLE_SIZE;
	private int numChannels = Global.NUM_CHANNELS;
	private int musicReadSize = Global.MUSIC_READ_SIZE;	
	private boolean doneLoading = false;
	private boolean playBack = false;
	
	/*Buffered Music*/
	private ArrayList<byte[]> bufferedMusicData;
	private MusicStreamer mStreamer;
	private Thread playMusicThread;
	private int drawFinish;
	private int timeTick;
	private MusicScreen playerRef = null;
	
	/*Takes in a file that points to a file and sets up the necessary data*/
	private void setupMusicFileData(File f)
	{	
		try
		{	
			mStreamer.in = AudioSystem.getAudioInputStream(f);
			mStreamer.baseFormat = mStreamer.in.getFormat();
			mStreamer.decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					mStreamer.baseFormat.getSampleRate(), (int) (getSampleSize()*8), mStreamer.baseFormat.getChannels(),
					mStreamer.baseFormat.getChannels() * 2, mStreamer.baseFormat.getSampleRate(),
					true);
			setSampleRate(mStreamer.baseFormat.getSampleRate());
			setNumChannels(mStreamer.baseFormat.getChannels());
			
			mStreamer.decoded_in = AudioSystem.getAudioInputStream(mStreamer.decodedFormat, mStreamer.in);
			mStreamer.info = new DataLine.Info(SourceDataLine.class, mStreamer.decodedFormat);
			
			/*Debug Print*/
			long musicFileSize = f.length();
			System.out.format("MusicFileSize = %d, SampleRate = %f, numChannels = %d, frameSize = %d, frameRate = %f\n",
					musicFileSize, 
					mStreamer.baseFormat.getSampleRate(), 
					mStreamer.baseFormat.getChannels(), 
					mStreamer.baseFormat.getChannels() * 2, 
					mStreamer.baseFormat.getSampleRate());
			/*End Debug Print*/
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static float[] unpack(
	        byte[] bytes,
	        long[] transfer,
	        float[] samples,
	        int bvalid,
	        AudioFormat fmt) {
        if(fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                && fmt.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
            
            return samples;
        }
        
        final int bitsPerSample = fmt.getSampleSizeInBits();
        final int bytesPerSample = bitsPerSample / 8;
        
        if(fmt.isBigEndian()) {
            for(int i = 0, k = 0, b; i < bvalid; i += bytesPerSample, k++) {
                transfer[k] = 0L;
                
                int least = i + bytesPerSample - 1;
                for(b = 0; b < bytesPerSample; b++) {
                    transfer[k] |= (bytes[least - b] & 0xffL) << (8 * b);
                }
            }
        } else {
            for(int i = 0, k = 0, b; i < bvalid; i += bytesPerSample, k++) {
                transfer[k] = 0L;
                
                for(b = 0; b < bytesPerSample; b++) {
                    transfer[k] |= (bytes[i + b] & 0xffL) << (8 * b);
                }
            }
        }
        
        final long fullScale = (long)Math.pow(2.0, bitsPerSample - 1);
        
        /*
         * the OR is not quite enough to convert,
         * the signage needs to be corrected.
         * 
         */
        
        if(fmt.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            final long signShift = 64L - bitsPerSample;
            
            for(int i = 0; i < transfer.length; i++) {
                transfer[i] = (
                    (transfer[i] << signShift) >> signShift
                );
            }
        } else {
            
            for(int i = 0; i < transfer.length; i++) {
                transfer[i] -= fullScale;
            }
        }
        
        /* finally normalize to range of -1.0f to 1.0f */
        
        for(int i = 0; i < transfer.length; i++) {
            samples[i] = (float)transfer[i] / (float)fullScale;
        }
        
        return samples;
    }

	
	/*Initialize Music Data*/
	private void initVars()
	{
		bufferedMusicData = new ArrayList<byte []>();
		mStreamer = new MusicStreamer();
		playMusicThread = new Thread(this);
		drawFinish = 0;
	}

	/*Helper Functions*/
	/*Returns the # of channels*/
	public void setNumChannels(int nChannels)
	{
		this.numChannels = nChannels;
	}
	public int getNumChannels()
	{
		return this.numChannels;
	}
	
	/*Returns the #of bytes per sample*/
	public void setSampleSize(int sSize)
	{
		this.sampleSize = sSize;
	}
	public int getSampleSize()
	{
		return this.sampleSize;
	}
	

	

	/*Gets the music sample rate*/
	public float getSampleRate()
	{
		return this.sampleRate;
	}
	/*Sets the music sample rate*/
	public void setSampleRate(float sR)
	{
		this.sampleRate = sR;
	}
	
	public MusicReader(File f, MusicScreen playerRef)
	{
		initVars();
		setupMusicFileData(f);
		this.playerRef = playerRef;
	}
	
    public static float[] window(
            float[] samples,
            int svalid,
            AudioFormat fmt
        ) {
            /*
             * most basic window function
             * multiply the window against a sine curve, tapers ends
             * 
             * nested loops here show a paradigm for processing multi-channel formats
             * the interleaved samples can be processed "in place"
             * inner loop processes individual channels using an offset
             * 
             */
            
            int channels = fmt.getChannels();
            int slen = svalid / channels;
            
            for(int ch = 0, k, i; ch < channels; ch++) {
                for(i = ch, k = 0; i < svalid; i += channels) {
                    samples[i] *= Math.sin(Math.PI * k++ / (slen - 1));
                }
            }
            
            return samples;
        }



	@Override
	protected Void doInBackground() throws Exception {
		try {
            AudioInputStream in = null;
            SourceDataLine out = null;
            
            try {
            		in = mStreamer.decoded_in;
                    out = (SourceDataLine) AudioSystem.getLine(mStreamer.info);
                    int bytes_per_sample = mStreamer.decodedFormat.getSampleSizeInBits()/8;
                    
                    float[] samples = new float[musicReadSize * mStreamer.decodedFormat.getChannels()];
                    long[] transfer = new long[samples.length];
                    byte[] bytes = new byte[samples.length * bytes_per_sample];
                    
                    out.open(mStreamer.decodedFormat, bytes.length);
                    out.start();
                    
                    /*
                     * feed the output some zero samples
                     * helps prevent the 'stutter' issue.
                     * 
                     */
                    
                    for(int feed = 0; feed < 6; feed++) {
                        out.write(bytes, 0, bytes.length);
                    }
                    
                    int bread;
                    
                    play_loop: do {
                        while(this.playerRef.getState() == PlayState.PLAYING) {
                            
                            if((bread = in.read(bytes, 0, bytes.length)) == -1) {
                                break play_loop; // eof
                            }
                            
                            samples = unpack(bytes, transfer, samples, bread, mStreamer.decodedFormat);
                            samples = window(samples, bread/bytes.length, mStreamer.decodedFormat);
                            
                           this.playerRef.drawDisplay(samples, bread / bytes_per_sample);
                            
                           out.write(bytes, 0, bread);
                           
                        }
                        
                        if(this.playerRef.getState() == PlayState.PAUSED) {
                            out.flush();
                            try {
                                synchronized(this.playerRef.getLock()) {
                                    this.playerRef.getLock().wait(1000L);
                                }
                            } catch(InterruptedException ie) {}
                            continue;
                        } else {
                            break;
                        }
                    } while(true);
                    
            } finally {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.flush();
                    out.close();
                }
            }
        } catch(Exception e) {
        	e.printStackTrace();
        }
		return (Void)null;
	}
}
