public class MusicInfo
{
	float sampleRate; //Samples per second
	float frameRate; //Frames per second
	float samplesPerTick;//Samples per Tick
	int numChannels; //Mono/Stereo e.t.c
	int frameSize; //Size in bytes of frame rate
	int timeTick; //Draw/Update rate
	int musicReadSize; //Size of buffer used to write to PCM
	
	/*E.G:
	 * SampleRate = 44.1 Khz, sample_size = 2 bytes, num_channels = 2
	 * Frame Size in bytes = sample_size * num_channels
	 * */
	
	public MusicInfo(float sRate, float fRate, 
					 int nChannels, int fSize, 
					 int tTick, int sPerTick,
					 int mReadSize)
	{
		sampleRate = sRate;
		frameRate = fRate;
		numChannels = nChannels;
		frameSize = fSize;
		timeTick = tTick;
		samplesPerTick = sPerTick;
		musicReadSize = mReadSize;
	}
	
}
