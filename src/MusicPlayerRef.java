import java.io.File;


public interface MusicPlayerRef {
        public Object getLock();
        public PlayState getState();
        public File getFile();
        public void playbackEnded();
        public void drawDisplay(float[] samples, int svalid);
    }


class MusicScreen implements MusicPlayerRef {
	public Object stateLock;
	public PlayStateRef playStateRef;
	public MusicPanel mP;
	public File audioFile;
	
	public void setAudioFile(File aF) {
		this.audioFile = aF;
	}

	@Override
	public Object getLock() {
		return this.stateLock;
	}

	@Override
	public PlayState getState() {
		return this.playStateRef.pS;
	}

	@Override
	public File getFile() {
		return audioFile;
	}

	@Override
	public void playbackEnded() {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawDisplay(float[] samples, int svalid) {
		this.mP.musicDrawMethod(samples);
		this.mP.repaint();
	}
	
	public MusicScreen(MusicPanel mP, Object stateLock, PlayStateRef pStateRef) {
		this.mP = mP;
		this.stateLock = stateLock;
		this.playStateRef = pStateRef;
	}
	
	
}
