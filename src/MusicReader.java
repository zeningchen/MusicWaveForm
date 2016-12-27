import javax.sound.sampled.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*Class is like a queue*/
class DynamicMusicQueue
{
	/*Main storage structure*/
	private ArrayList<byte[]> dynBuf;
	
	/*Constructor*/
	public DynamicMusicQueue()
	{
		dynBuf = new ArrayList<byte[]>();
	}
	
	/*Queue data to dynamic buffer*/
	public void queueData(byte[] mData)
	{
		synchronized(dynBuf)
		{
			dynBuf.add(mData);
		}
	}
	
	/*Dequeue data from dynamic buffer*/
	public byte[] dequeueData()
	{
		byte[] mData = null;
		synchronized(dynBuf)
		{
			mData = dynBuf.remove(0);
		}
		return mData;
	}
	
	public int size()
	{
		return dynBuf.size();
	}
	
	public byte[] get(int i)
	{
		return dynBuf.get(i);
	}
}

class MusicReader implements Runnable
{
	/*Music Info*/
	private float sampleRate = Global.SAMPLE_RATE;
	private int sampleSize = Global.SAMPLE_SIZE;
	private int numChannels = Global.NUM_CHANNELS;
	private int musicReadSize = Global.MUSIC_READ_SIZE;	
	private boolean doneLoading = false;
	private boolean playBack = false;
	
	/*Buffered Music*/
	private DynamicMusicQueue preBufferedMusicData;
	private ArrayList<byte[]> bufferedMusicData;
	private MusicStreamer mStreamer;
	private Thread playMusicThread;
	private int drawFinish;
	private int timeTick;
	

	
	/*Takes in a string that points to a file and sets up the necessary data*/
	private void setupMusicFileData(String s)
	{	
		try
		{
			File f = new File(s);
			System.out.println(s);
			
			mStreamer.in = AudioSystem.getAudioInputStream(f);
			mStreamer.baseFormat = mStreamer.in.getFormat();
			mStreamer.decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					mStreamer.baseFormat.getSampleRate(), (int) (getSampleSize()*8), mStreamer.baseFormat.getChannels(),
					mStreamer.baseFormat.getChannels() * 2, mStreamer.baseFormat.getSampleRate(),
					true);
			setSampleRate(mStreamer.baseFormat.getSampleRate());
			setNumChannels(mStreamer.baseFormat.getChannels());
			
			mStreamer.din = AudioSystem.getAudioInputStream(mStreamer.decodedFormat, mStreamer.in);
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
	
	
	
	/*Prebuffer the music a bit*/
	private void preBufferMusic()
	{
		long lastMeasTime = System.nanoTime();
		System.out.println("Prebuffer Stime = " + lastMeasTime);
		try
		{
			if(mStreamer.din != null) {				
				/*Buffer Data*/
				int nBytesRead = 0;
				int nBytesReadTotal = 0;
				byte[] data = new byte[musicReadSize];
				if(preBufferedMusicData != null)
				{
					System.out.println("Pre Buffering Music Started");
					for(int i = 0; 
						(i < Global.PRE_BUFFERING_SIZE); 
						i++)
					{
						nBytesRead = mStreamer.din.read(data, 0, data.length);
						if( nBytesRead == -1)
						{
							break;
						}
						else
						{
							preBufferedMusicData.queueData(data);
							data = null;
							data = new byte[musicReadSize];
							nBytesReadTotal += nBytesRead;
						}
					}
	
					System.out.println("PreBuffering Finished: " + nBytesReadTotal + " bytes");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Time take to preBuffer = " + (System.nanoTime() - lastMeasTime));
	}
	
	/*Buffers music*/
	private void bufferMusic()
	{
		long lastMeasTime = System.nanoTime();
		System.out.println("Buffer Stime = " + lastMeasTime);
		try
		{
			if(mStreamer.din != null) {				
				/*Buffer Data*/
				int nBytesRead = 0;
				int nBytesReadTotal = 0;
				byte[] data = new byte[musicReadSize];
				if(bufferedMusicData != null)
				{
					System.out.println("Buffering Started");
					while ((nBytesRead = mStreamer.din.read(data, 0, data.length)) != -1) {	
						bufferedMusicData.add(data);
						data = null;
						data = new byte[musicReadSize];
						nBytesReadTotal += nBytesRead;
					}
					System.out.println("Buffering Finished: " + nBytesReadTotal + " bytes");
				}
				mStreamer.din.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(mStreamer.din != null) {
				try 
				{ 
					mStreamer.din.close(); 
				} 
				catch(IOException e) 
				{ 
					e.printStackTrace(); 
				}
			}
		}
		System.out.println("Time take to Buffer = " + (System.nanoTime() - lastMeasTime));
	}
	
	private void playPreBufferedMusic()
	{
		try
		{
			if(mStreamer.din != null) 
			{	
				SourceDataLine line = null;
				if(mStreamer.info != null)
				{
					line = (SourceDataLine) AudioSystem.getLine(mStreamer.info);
				}
				
				if(line != null) {
					line.open(mStreamer.decodedFormat);
					
					
					/*Flush Buffer*/
					for(int i = 0; i < preBufferedMusicData.size(); i++)
					{
						line.write(preBufferedMusicData.get(i), 0, musicReadSize);
					}
					
					
					/*Start Music Playback here*/
					setPlayBack(true);
					
					// Start
					line.start();
					
					byte[] data = new byte[musicReadSize];
					
					int nBytesRead = 0;
					int nBytesReadTotal = 0;
					
					while ((nBytesRead = mStreamer.din.read(data, 0, data.length)) != -1) {
						preBufferedMusicData.queueData(data);
						line.write(data, 0, musicReadSize);
						TimeUnit.MILLISECONDS.sleep(Global.MUSIC_READ_DELAY);
						data = null;
						data = new byte[musicReadSize];
						nBytesReadTotal += nBytesRead;
					}
					
					setPlayBack(false);
					
					// Stop
					line.drain();
					line.stop();
					line.close();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(mStreamer.din != null) {
				try 
				{ 
					mStreamer.din.close(); 
				} 
				catch(IOException e) 
				{ 
					e.printStackTrace(); 
				}
			}
		}	
	}
	
	/*Plays music from the buffer*/
	private void playMusic()
	{
		
		try
		{
			
			SourceDataLine line = null;
			if(mStreamer.info != null)
			{
				line = (SourceDataLine) AudioSystem.getLine(mStreamer.info);
			}
			
			if(line != null) {
				line.open(mStreamer.decodedFormat);

				// Start
				line.start();
				
				/*Flush Buffer*/
				setPlayBack(true);
				for(int i = 0; i < bufferedMusicData.size(); i++)
				{
					//ByteBuffer.wrap(bufferedMusicData.get(i)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(bufferedMusicData16bit);
					line.write(bufferedMusicData.get(i), 0, musicReadSize);
				}
				setPlayBack(false);
				
				// Stop
				line.drain();
				line.stop();
				line.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	/*Initialize Music Data*/
	private void initVars()
	{
		preBufferedMusicData = new DynamicMusicQueue();
		bufferedMusicData = new ArrayList<byte []>();
		mStreamer = new MusicStreamer();
		playMusicThread = new Thread(this);
		drawFinish = 0;
	}

	/*Helper Functions*/
	/*Returns the # of channels*/
	public void setNumChannels(int nChannels)
	{
		numChannels = nChannels;
	}
	public int getNumChannels()
	{
		return numChannels;
	}
	
	/*Returns the #of bytes per sample*/
	public void setSampleSize(int sSize)
	{
		sampleSize = sSize;
	}
	public int getSampleSize()
	{
		return sampleSize;
	}
	
	/*Sets/Gets the playback status when playing*/
	public void setPlayBack(boolean play)
	{
		playBack = play;
	}
	public boolean getPlayBack()
	{
		return playBack;
	}
	

	/*Gets the music sample rate*/
	public float getSampleRate()
	{
		return sampleRate;
	}
	/*Sets the music sample rate*/
	public void setSampleRate(float sR)
	{
		sampleRate = sR;
	}
	
	/*Return Dynamic Music Buffer*/
	public DynamicMusicQueue getPreloadedMusicBuffer()
	{
		return preBufferedMusicData;
	}
	
	/*Return Music Buffer*/
	public ArrayList<byte[]> getMusicBuffer()
	{
		return bufferedMusicData;
	}
	
	/*Returns the size of the buffer used to write to PCM*/
	public int getMusicDataSize()
	{
		return musicReadSize;
	}
	
	
	/*Starts the musicPlayerThreadf*/
	public void startMusicPlayer()
	{
		playMusicThread.start();
	}
	
	/*NEED TO UPDATE*/
	public short[] returnMusicData16bit()
	{
		return null;
	}
	
	
	public int getDrawFinish()
	{
		return drawFinish;
	}
	
	public void setDrawFinish(int val)
	{
		drawFinish = val;
	}
	
	public MusicReader(String s)
	{
		initVars();
		setupMusicFileData(s);
		if(Global.DEBUG_MODE)
		{
			bufferMusic();
		}
		else
		{
			preBufferMusic();
		}
		
		

	}

	@Override
	public void run() {
		if(Global.DEBUG_MODE)
		{
			playMusic();
		}
		else
		{
			playPreBufferedMusic();
		}
		
			
	}
}
