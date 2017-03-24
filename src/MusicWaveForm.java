import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.geom.Line2D.Float;
import java.lang.reflect.Array;
import java.nio.ShortBuffer;
import java.util.ArrayList;



/*Uses Music Reader to read music Data to generate waveform that gets sent to MusicWindow*/
public class MusicWaveForm {
	
	private class ScreenMeasurements
	{
		public float midScreen;
		public float waveXIncrement;
		public float numTicks;
		public float screenWidth;
		public float screenHeight;
		public float waveShift;
		public float yScale;
		private MusicInfo mInfo;
		public ScreenMeasurements(float timeFrame, float screenHeight, float screenWidth, MusicInfo mInfoN)
		{
			mInfo = mInfoN;
			calcMidscreen(screenHeight);
			calcWaveXIncAndNumTicks(timeFrame, screenWidth);
			this.screenHeight = screenHeight;
			this.screenWidth = screenWidth;
			this.waveShift = (float)((mInfo.samplesPerTick-1)*this.waveXIncrement);
			this.yScale = (float)(screenHeight/2)/(float)32768;
		}
		
		private void calcMidscreen(float screenHeight)
		{
			midScreen = screenHeight/2;
		}
		
		private void calcWaveXIncAndNumTicks(float timeFrame, float screenWidth)
		{
			if(mInfo != null)
			{
				numTicks = (timeFrame/(float)mInfo.timeTick)*mInfo.samplesPerTick;
				waveXIncrement = screenWidth/(float)numTicks;
				//System.out.println("numTicks = " + numTicks + "timeTick = " +  (float)mInfo.timeTick);
				//System.out.println("waveXIncrement = " + waveXIncrement);
			}
		}
	}
	
	private class MusicDataStore
	{
		/*For Setting up draw data*/
		public int currentArrayListIdx;
		public int currentBufferIdx;
		public int arrListDelta;
		public int bufferIdxDelta;
		public int bufferIdxStep;
		public int bufferSize;
		
		/*For Drawing*/
		public int waveFormArrayDrawIndex;
		public int waveFormWriteIndex;
		public int mAvgBufSize;
		public float waveXPos;
		public float waveYPos;
		public byte[] currMDataBuffer;
		public boolean nextBuffer;
		
		public MusicDataStore(int arrListIdx, int buffIdx)
		{
			currentArrayListIdx = arrListIdx;
			currentBufferIdx = buffIdx;
		}
	}
	
	/*LTI Filter*/
	private MovingAverageFilter mAvFlt;
	
	/*MusicInfo*/
	private MusicInfo mInfo;
	
	/*Global Vars*/
	private int arrayListIndexInitialPos;
	private ArrayList<Line2D.Float> waveFormArray;
	private MusicReader mReaderG;
	private ScreenMeasurements sMeas;
	private MusicDataStore mDataStore;
	
	/*Initialize Filter Data*/
	private void initFilterData()
	{
		mAvFlt = new MovingAverageFilter(mDataStore.mAvgBufSize);
	}
	
	/*Initialize Music Data Store*/
	private void initMusicDataStore()
	{
		/*Local Variables*/
		float bufferTimeInMs = 
				((float)mReaderG.getMusicDataSize()/((float)mInfo.frameRate*(float)mInfo.frameSize))*1000;
		
		/*Calculate new indices per tick*/
		float idxArrDelta = ((float)mInfo.timeTick)/bufferTimeInMs;
		float idxBufDelta = (idxArrDelta - (float)Math.floor(idxArrDelta))*mReaderG.getMusicDataSize();
		
		
		/*Setup Music Data Store*/
		mDataStore = new MusicDataStore(0, 0);
		mDataStore.arrListDelta = (int)idxArrDelta;
		
		/*Buffer step for each time_tick (draw time)*/
		mDataStore.bufferIdxDelta = (int)idxBufDelta;
		
		/*Buffer step for each sample per tick*/
		mDataStore.bufferIdxStep = 
			(mDataStore.arrListDelta*mReaderG.getMusicDataSize() + mDataStore.bufferIdxDelta)/(int)mInfo.samplesPerTick;
		mDataStore.bufferSize = mReaderG.getMusicDataSize();
		mDataStore.waveXPos = Global.SCREEN_WIDTH;
		mDataStore.waveYPos = sMeas.midScreen;
		mDataStore.waveFormArrayDrawIndex = 0;
		mDataStore.waveFormWriteIndex = 0;
		mDataStore.nextBuffer = true;
		mDataStore.mAvgBufSize = Global.MOVING_AVG_SIZE;
		
	}
	
	
	/*Draw a blank wave to the screen*/
	private void initWave(float timeFrame)
	{
		if((waveFormArray != null) && (sMeas != null))
		{
			waveFormArray.clear();
			float waveX = 0;
			float waveXPrev = 0;
			
			for(int i = 0; i < sMeas.numTicks; i++)
			{
				/*Endpoint*/
				waveX += sMeas.waveXIncrement;
				
				/*Construct and add line*/
				Point2D.Float start = new Point2D.Float(waveXPrev, sMeas.midScreen);
				Point2D.Float end = new Point2D.Float(waveX, sMeas.midScreen);
				Line2D.Float nextLine = new Line2D.Float(start, end);
				waveFormArray.add(nextLine);
				
				/*New Beginning Point*/
				waveXPrev = waveX;
			}
		}
		else
		{
			System.out.println("waveFormArray not initialized");
		}
	}
	
	/*Converts two bytes to a short*/
	private short convertBytesToShort(byte b1, byte b2, boolean bigEndian)
	{
		short lsbS = 0;
		short msbS = 0;
		if(!bigEndian)
		{
			lsbS = (short)b1;
			msbS = (short)(((short)b2) << 8);
		}
		else
		{
			msbS = (short)(((short)b1) << 8);
			lsbS = (short)b2;
		}
		short condensedByte = (short)(lsbS|msbS);
		return condensedByte;
	}
	
	private int[] convertShortArr2IntArr(short[] data)
	{
		int[] intArr = new int[data.length];
		for(int i = 0; i < intArr.length; i++)
		{
			intArr[i] = (int)data[i];
		}
		return intArr;
	}
	
	/*Get the timetick info*/
	private short [] getTimeTickBufferPreloaded(DynamicMusicQueue mQ)
	{		
		/*Data converted to 16bit*/
		short [] music16bitBuffer = new short[(int)mInfo.samplesPerTick];
		
		/*Beginning Indices*/
		int bufferIter = mDataStore.currentBufferIdx;
		
		System.out.println("Current DynBufferSize = " + mQ.size());
		
		/*Populate buffers for this tick*/
		for(int i = 0; i < mInfo.samplesPerTick; i++)
		{
			if(mDataStore.nextBuffer)
			{
				/*Get dataBuffer and the samples*/
				mDataStore.currMDataBuffer = mQ.dequeueData();
			}
			music16bitBuffer[i] = convertBytesToShort(mDataStore.currMDataBuffer[bufferIter], mDataStore.currMDataBuffer[bufferIter+1], true);
			
			/*ArrayListIter Increment*/
			if(bufferIter + mDataStore.bufferIdxStep > mDataStore.bufferSize)
			{
				mDataStore.nextBuffer = true;
			}
			else
			{
				mDataStore.nextBuffer = false;
			}
			
			/*Get the buffer iterator rounded down to the closest multiple of 4*/
			bufferIter = (bufferIter + mDataStore.bufferIdxStep)%mDataStore.bufferSize;
			bufferIter = bufferIter - (bufferIter%4);
		}
		
		/*Update new indices*/
		mDataStore.currentBufferIdx = bufferIter;
		
		return music16bitBuffer;
	}
	
	/*Gets the music data in MusicInfo timetick*/
	private short [] getTimeTickBuffer(ArrayList<byte[]> arrDataBuffer)
	{
		
		/*Get the current index of the buffer*/
		short [] music16bitBuffer = new short[(int)mInfo.samplesPerTick];
		byte[] dataBuffer;
		
		/*Beginning Indices*/
		int arrListIter = mDataStore.currentArrayListIdx;
		int bufferIter = mDataStore.currentBufferIdx;
		
		/*Populate buffers for this tick*/
		for(int i = 0; i < mInfo.samplesPerTick; i++)
		{
			
			/*Get dataBuffer and the samples*/
			dataBuffer = arrDataBuffer.get(arrListIter);
			music16bitBuffer[i] = convertBytesToShort(dataBuffer[bufferIter], dataBuffer[bufferIter+1], true);
			
			/*ArrayListIter Increment*/
			if(bufferIter + mDataStore.bufferIdxStep > mDataStore.bufferSize)
			{
				arrListIter++;
			}
			
			/*Get the buffer iterator rounded down to the closest multiple of 4*/
			bufferIter = (bufferIter + mDataStore.bufferIdxStep)%mDataStore.bufferSize;
			bufferIter = bufferIter - (bufferIter%4);
		}
		
		/*Update new indices*/
		mDataStore.currentArrayListIdx = arrListIter;
		mDataStore.currentBufferIdx = bufferIter;
		
		return music16bitBuffer;
	}
	
	/*Shifts the wave over*/
	private void waveShift(float xUnits)
	{
		for(int i = 0; i < waveFormArray.size(); i++)
		{
			waveFormArray.get(i).x1 -= xUnits;
			waveFormArray.get(i).x2 -= xUnits;
		}
	}
	
	/*Adds the line specified by input params to arrayList used to draw*/
	private void addLineToWave(float waveXLast, float waveYLast, float waveX, float waveY)
	{
		/*Construct and add line*/
		Point2D.Float start = new Point2D.Float(waveXLast, waveYLast);
		Point2D.Float end = new Point2D.Float(waveX, waveY);
		Line2D.Float nextLine = new Line2D.Float(start, end);
		waveFormArray.set(mDataStore.waveFormWriteIndex, nextLine);
		mDataStore.waveFormWriteIndex = (mDataStore.waveFormWriteIndex + 1)%waveFormArray.size();
	}
	
	/*Write the new wave data to ArrayList used to Draw*/
	private void writeNewWaveData(short [] musicData)
	{
		
		/*The first sample point to be displayed at the end of the screen*/
		float waveX = sMeas.screenWidth;
		float waveXLast = sMeas.screenWidth;
		float waveY = sMeas.midScreen;
		float waveYLast = sMeas.midScreen + musicData[musicData.length-1]*sMeas.yScale;
		
		/*Update the waveform*/
		for(int i = musicData.length-2; i >= 0; i--)
		{	
			if(sMeas != null)
			{
				/*Calculate new starting point*/
				/*Connect the last line of this segment, with the first line of the last segment*/
				if(i == 0)
				{
					waveY = mDataStore.waveYPos;
				}
				/*Otherwise connect adjoining segments*/
				else
				{
					waveY = sMeas.midScreen + musicData[i]*sMeas.yScale;	
				}
				waveX -= sMeas.waveXIncrement;
				
				
				/*Take into account wrapping*/
				if(waveX < 0)
				{
					waveX = Global.SCREEN_WIDTH + waveX;
					waveXLast = Global.SCREEN_WIDTH;
				}
				
				/*Add line to wave*/
				addLineToWave(waveXLast, waveYLast, waveX, waveY);
				
				/*Update Last Point*/
				waveYLast = waveY;
				waveXLast = waveX;
			}
		}
		
		/*Update new Indices*/
		mDataStore.waveYPos = sMeas.midScreen + musicData[musicData.length-1]*sMeas.yScale;;
		mDataStore.waveXPos = Global.SCREEN_WIDTH;
		mDataStore.waveFormArrayDrawIndex = mDataStore.waveFormWriteIndex;
	}
	
	/*Used for dynamic buffering*/
	private void updateWavePreBuffered(DynamicMusicQueue mQ)
	{
		/*Get the next tick buffer*/
		short [] musicData = getTimeTickBufferPreloaded(mQ);
		
		/*Comment out filtering function for now until efficient way is used*/
		//int[] musicData32Bit = convertShortArr2IntArr(musicData);
		//musicData = mAvFlt.filterData(musicData32Bit);
		
		/*Waveform Array*/
		if(waveFormArray != null)
		{
			/*Shift the wave if needed*/
			waveShift(sMeas.waveShift);
			
			/*Write the new wave data*/
			writeNewWaveData(musicData);
			
		}
		else
		{
			System.out.println("waveFormArray not initialized");
		}
		
		/*Recycle Memory*/
		musicData = null;
	}
	
	/*Used for Debug-Mode testing (buffering whole music file at once)*/
	private void updateWave(ArrayList<byte[]> arrDataBuffer)
	{
		/*Get the next tick buffer*/
		short [] musicData = getTimeTickBuffer(arrDataBuffer);
		
		/*Waveform Array*/
		if(waveFormArray != null)
		{
			/*Shift the wave if needed*/
			waveShift(sMeas.waveShift);
			
			/*Write the new wave data*/
			writeNewWaveData(musicData);
			
		}
		else
		{
			System.out.println("waveFormArray not initialized");
		}
		
		/*Recycle Memory*/
		musicData = null;
	}
	
	public void setMusicReader(MusicReader mReader)
	{
		mReaderG = mReader;
	}
	
	public void setMusicInfo(MusicInfo info)
	{
		if(info != null)
		{
			mInfo = info;
		}
		else
		{
			System.out.println("Null Music Info passed in! Initializing musicInfo");
			mInfo = new MusicInfo(0, 0, 0, 0, 0, 0);
		}
	}
	
	public void drawWave(Graphics2D g2d)
	{
		if(mReaderG != null)
		{
			if(Global.DEBUG_MODE)
			{
				if(mReaderG.getMusicBuffer() != null)
				{
					/*Build waveFormArray*/
					updateWave(mReaderG.getMusicBuffer());
				}
			}
			else
			{
				if(mReaderG.getPreloadedMusicBuffer() != null)
				{
					updateWavePreBuffered(mReaderG.getPreloadedMusicBuffer());
				}
			}
		}
		
		/*Draw waveFormArray*/
		for(int i = 0; i < waveFormArray.size(); i++)
		{
			if(waveFormArray.get(i) != null)
			{
				g2d.draw(waveFormArray.get(i));
			}
			else
			{
				System.out.println("Null line segment at indext = " + i);
			}
		}
		
	}
	
	public MusicWaveForm(float timeFrame, MusicInfo mInfo, MusicReader mReader)
	{		
		/*Setup Music Reader and Music Info*/
		setMusicInfo(mInfo);
		setMusicReader(mReader);
		
		/*Setup waveFormArray*/
		waveFormArray = new ArrayList<Line2D.Float>();
		
		/*Setup Screen Boundaries*/
		sMeas = new ScreenMeasurements(timeFrame, 
									  (float)Global.SCREEN_HEIGHT,
									  (float)Global.SCREEN_WIDTH,
									  mInfo);
		
		/*Initialize MusicDataStore variables*/
		initMusicDataStore();
		
		/*Initialize the Wave Array*/
		initWave(timeFrame);
		
		/*Initialize Filter*/
		initFilterData();
	}
}
