import java.awt.Color;

class Global
{
	public static int PRE_BUFFERING_SIZE = 500;
    public static int TITLE_BAR_WIDTH = 15;
    public static int SCREEN_WIDTH = 900;
    public static int SCREEN_HEIGHT = 700;
    public static int TIME_TICK = 10; //in MS
    public static int SAMPLES_PER_TICK= 5;
    public static int TOTAL_TIME_X = 100; //in MS
    public static Color BGROUND_COLOR = Color.BLACK;
    public static boolean DEBUG_MODE = false;
    public static int MOVING_AVG_SIZE = 1;
    public static int SAMPLE_SIZE = 2;
    public static int SAMPLE_RATE = 0;
    public static int NUM_CHANNELS = 2;
    public static int DIVIDING_FACTOR = 8;
    public static int MUSIC_READ_SIZE = 2048;
    public static int MUSIC_DRAW_SIZE = MUSIC_READ_SIZE/DIVIDING_FACTOR;
    public static int MUSIC_READ_DELAY = 1;
}

