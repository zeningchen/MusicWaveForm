public class MusicInfo
{
	float sampleRate;
	float frameRate;
	float samplesPerTick;
	int numChannels;
	int frameSize;
	int timeTick;
	
	public MusicInfo(float sRate, float fRate, int nChannels, int fSize, int tTick, int sPerTick)
	{
		sampleRate = sRate;
		frameRate = fRate;
		numChannels = nChannels;
		frameSize = fSize;
		timeTick = tTick;
		samplesPerTick = sPerTick;
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
	
}
