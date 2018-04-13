package lyp.com.netmusic.BtRecord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import lyp.com.netmusic.R;


public final class Recorder {
    private Context context;
    private AudioManager mAudioManager;
    private Recorder.RecordThread mRecordingThread;
    private Recorder.RecoderListener mListener;
    private int SAMPLE_RATE_HZ;
    private long startRecorderTime, stopRecorderTime;
    boolean mWorking = true;
    public RecoderListener getMListener() {
        return this.mListener;
    }


    public void startRecord(RecoderListener listener) {
        this.mListener = listener;
        AudioManager audioManager = this.mAudioManager;
        if(this.mAudioManager == null) {
        }

        if(audioManager.isBluetoothScoAvailableOffCall()) {

            if(audioManager.isBluetoothScoOn()) {
                audioManager.stopBluetoothSco();
                Log.e("BTRecordImpl", "1mAudioManager.stopBluetoothSco()");
            }
            Log.e("BTRecordImpl", "1startBluetoothSco");
            audioManager.startBluetoothSco();
            int timeout = 100;

            while(mWorking) {
                if(audioManager.isBluetoothScoOn() || timeout-- <= 0) {

                    if(this.mRecordingThread != null) {
                        mRecordingThread.pause();
                        mRecordingThread.interrupt();
                    }

                    mRecordingThread = new Recorder.RecordThread();
                    mRecordingThread.start();
                    break;
                }

                try {
                    Thread.sleep(10L);
                } catch (Exception e) {

                }
                if(timeout == 50) {
                    Log.e("BTRecordImpl", "2startBluetoothSco");
                    audioManager.startBluetoothSco();
                }
            }
        }

    }

    public Recorder(Context context) {
        super();
        this.context = context;
        Object audio = context.getSystemService(Context.AUDIO_SERVICE);
        if(audio != null) {
            this.mAudioManager = (AudioManager)audio;
            this.SAMPLE_RATE_HZ = 16000;
        }
    }

    public interface RecoderListener {
        void onData(byte[] var1);
    }


    public final class RecordThread extends Thread {
        private AudioRecord audioRecord;
        private final int bufferSize;
        private boolean isRun;

        public void run() {
            super.run();
            this.isRun = true;

            try {
                if(audioRecord.getState() == 1) {
                    audioRecord.startRecording();
                    startRecorderTime = System.currentTimeMillis();

                    while(this.isRun) {
                        byte[] buffer = new byte[this.bufferSize];
                        int readBytes = audioRecord.read(buffer, 0, this.bufferSize);
                        if(readBytes > 0 && getMListener() != null) {
                            getMListener().onData(buffer);
                        }
                    }
                    audioRecord.stop();
                    audioRecord.release();

                }
            } catch (Exception e) {
                    audioRecord.stop();
                    audioRecord.release();
                this.isRun = false;
            }

        }

        public final void pause() {
            this.isRun = false;
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
            }
        }

        public synchronized void start() {
            if(!this.isRun) {
                super.start();
            }
        }

        public RecordThread() {
            int audiosource = MediaRecorder.AudioSource.DEFAULT;
            bufferSize = AudioRecord.getMinBufferSize(Recorder.this.SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;
            audioRecord = new AudioRecord(audiosource, Recorder.this.SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, this.bufferSize);
        }
    }
}
