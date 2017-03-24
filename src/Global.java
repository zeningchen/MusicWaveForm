import java.awt.Color;

class Global
{
	public static int PRE_BUFFERING_SIZE = 500;
    public static int TITLE_BAR_WIDTH = 15;
    public static int SCREEN_WIDTH = 900;
    public static int SCREEN_HEIGHT = 700;
    public static int DX = 0;
    public static int DY = 3;
    public static int GRAVITY = 3;
    public static int TIME_TICK = 100; //in MS
    public static int SAMPLES_PER_TICK= 50;
    public static int TOTAL_TIME_X = 1000; //in MS
    public static float DIAMETER = 30;
    public static float ELASTICITY_C = .90f;
    public static float FRICTION = .05f;
    public static float MOUSE_DAMPENER = .1f;
    public static float DENSITY = .2f;
    public static float BALL_WEIGHT = DIAMETER*DENSITY;
    public static float D_OMEGA = (float)((-1/(DIAMETER/2))*(180/Math.PI)); //ratio of angular velocity over velocity
    public static float TRACTOR_BEAM_CONSTANT_VELO = 15;
    public static float DOWN_SAMPLE_RATE = 5;
    public static Color B_COLOR = Color.white;
    public static Color BGROUND_COLOR = Color.BLACK;
    public static Color L_COLOR = Color.black;
    public static Color TRAJECTORY_COLOR = Color.red; 
    public static boolean DEBUG_MODE = false;
    public static int MOVING_AVG_SIZE = 1;
    public static int SAMPLE_SIZE = 2;
    public static int SAMPLE_RATE = 0;
    public static int NUM_CHANNELS = 2;
    public static int MUSIC_READ_SIZE = 1024;
    public static int MUSIC_READ_DELAY = 1;
}

