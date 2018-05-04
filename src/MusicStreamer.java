import javax.sound.sampled.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicStreamer {
	AudioInputStream in;
	AudioInputStream decoded_in;
	AudioFormat baseFormat;
	AudioFormat decodedFormat;
	DataLine.Info info;
}
