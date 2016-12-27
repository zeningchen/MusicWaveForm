
/*New LTI filter*/
public class LTIFilter
{
	/*Main window for filtering*/
	public float fltWndw[] = null;
	public int[] convData = null;
	public int lstDataSet[] = null;
	public int lstDataSetCurrIdx = 0;
	
	private void populateLastDataSet(int [] data, int numElements)
	{
		lstDataSet = null;
		lstDataSet = new int[numElements];
		
		/*Data window is > Filter Window*/
		if(data.length >= numElements)
		{
			for(int i = data.length - numElements; i < data.length; i++)
			{
				lstDataSet[lstDataSetCurrIdx] = data[i];
				lstDataSetCurrIdx = (lstDataSetCurrIdx + 1) % numElements;
			}
		}
		/*Filter is bigger than dataWindow*/
		else
		{
			for(int i = 0; i < data.length; i++)
			{
				lstDataSet[lstDataSetCurrIdx] = data[i];
				lstDataSetCurrIdx = (lstDataSetCurrIdx + 1) % numElements;
			}
			
		}
	}
	
	/*Init the convolution data*/
	private void initConvData(int numSamples)
	{
		convData = initDefaultArrayInt(numSamples, 0);
	}
	
	/*Init the filter*/
	private void initFilter(int numSamples)
	{
		fltWndw = initDefaultArray(numSamples, (float)0);
	}
	
	private void initLstDataSet(int numSamples)
	{
		lstDataSet = initDefaultArrayInt(numSamples, 0);
	}
	
	
	public int[] convolveXYStub(int[] dataArr)
	{
		return dataArr;
	}
	
	/*Piecewise convolution of a dataset and filter*/
	public int[] convolveXY(float [] filtArr, int[] dataArr)
	{
		/*Reset Convolution Data*/
		initConvData(dataArr.length);

		//System.out.println("filtArr.length = " + filtArr.length + " dataArr.length = " + dataArr.length);		
		if((filtArr == null) || (dataArr == null))
		{
			System.out.println("One of the buffers is null returning");
		}
		else if( lstDataSet != null)
		{
			for(int n = 0; n < convData.length; n++)
			{
				for(int m = 0; m < filtArr.length; m++)
				{
					if(n - m >= 0)
					{
						System.out.println("n-m GT 0");
						convData[n] += filtArr[m]*dataArr[n-m];
					}
					else
					{
						System.out.println("n-m LT 0!");
						if(n - m + lstDataSet.length > 0)
						{
							int lstDataSetIdx = lstDataSetCurrIdx + (n-m);
							if(lstDataSetIdx < 0)
							{
								lstDataSetIdx = lstDataSet.length + lstDataSetIdx;
							}
							convData[n] += lstDataSet[lstDataSet.length + (n-m)]*filtArr[m];
						}
					}
					
				}
			}
		}
		else
		{
			System.out.println("Last DataSet is Null!");
			for(int n = 0; n < convData.length; n++)
			{
				for(int m = 0; m < filtArr.length; m++)
				{
					if(n - m >= 0)
					{
						convData[n] += filtArr[m]*dataArr[n-m];
					}
					
				}
			}
		}
		
		/*Store the last convolved items*/
		populateLastDataSet(dataArr, filtArr.length - 1);
		return convData;
	}
	
	
	public int[] initDefaultArrayInt(int arrSize, int val)
	{
		int[] rtnArr = new int[arrSize];
		for(int i = 0; i < arrSize; i++)
		{
			rtnArr[i] = val;
		}
		return rtnArr;
	}
	
	
	/*Init the array with size arr_size with default value val*/
	public float[] initDefaultArray(int arrSize, float val) {
	    float[] rtnAr = new float[arrSize];
		for(int i = 0; i< arrSize; i++)
		{
			rtnAr[i] = val;
		}
		return rtnAr;
	}
	
	/*Returns the length of the filter window*/
	public int getFilterLength()
	{
		return fltWndw.length;
	}
	
	/*Sets the filter*/
	public void setFilter(int [] data)
	{
		int max_size = fltWndw.length;
		if(data!= null)
		{
			if(data.length > max_size)
			{
				max_size = data.length;
			}
			for(int i = 0; i < max_size; i++)
			{
				fltWndw[i] = (float)data[i];
			}
		}
		else
		{
			System.out.println("Null data being used to set filter Window! Returning");
		}
	}
	
	/*New LTI filter*/
	public LTIFilter(int numSamples)
	{
		initFilter(numSamples);
		initLstDataSet(numSamples-1);
		lstDataSetCurrIdx = 0;
	}
}