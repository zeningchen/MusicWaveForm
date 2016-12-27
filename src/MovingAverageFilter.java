
public class MovingAverageFilter extends LTIFilter
{	
	
	private short[] convertIntArr2ShortArr(int[] data)
	{
		short[] rtnData = new short[data.length];
		
		for(int i = 0; i< data.length; i++)
		{
			rtnData[i] = (short)data[i];
		}
		
		return rtnData;
	}
	
	public short[] filterData(int[] data)
	{
		/*DEBUG_TAG*/
		int[] intData2Convert = convolveXY(fltWndw, data);
		//int[] intData2Convert= convolveXYStub(data);
		short[] finalData = convertIntArr2ShortArr(intData2Convert);
		return finalData;
	}
	
	public MovingAverageFilter(int numSamples)
	{
		super(numSamples);
		fltWndw = initDefaultArray(numSamples, (float)1/numSamples);
	}
}
