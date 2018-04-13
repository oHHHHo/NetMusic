package lyp.com.netmusic;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import lyp.com.netmusic.BtRecord.Recorder;

public class MainActivity extends Activity {
    private Context context;
    private Button btnPause, btnPlayUrl, btnStop, startRecord;
    private SeekBar skbProgress;
    private Player player;

    private AudioManager mAudioManager;
    private int audioBufSize;
    private AudioTrack audioTrack;
    private Recorder mRecorder;
    private Boolean isRecording = false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle("在线音乐播放---hellogv编写");

        btnPlayUrl = (Button) this.findViewById(R.id.btnPlayUrl);
        btnPlayUrl.setOnClickListener(new ClickEvent());

        btnPause = (Button) this.findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new ClickEvent());

        btnStop = (Button) this.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new ClickEvent());

        skbProgress = (SeekBar) this.findViewById(R.id.skbProgress);
        skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        context = getApplicationContext();
        startRecord = (Button) findViewById(R.id.start_record);
        startRecord.setOnClickListener(new ClickEvent());

        // 音乐播放
        player = new Player(context, skbProgress);


    }

    class ClickEvent implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            if (arg0 == btnPause) {
                player.pause();
            } else if (arg0 == btnPlayUrl) {
                //在百度MP3里随便搜索到的,大家可以试试别的链接
                String url="http://192.168.126.23:9000/sb.mp4";
                player.playUrl(url);
            } else if (arg0 == btnStop) {
                player.stop();
            } else if (arg0 == startRecord) {
                if (!isRecording) {
                    startRecord();
                } else {
                    Toast.makeText(context,"已在录音中...",Toast.LENGTH_SHORT).show();
                    startRecord.setVisibility(View.GONE);
                }
            }
        }
    }

    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
            this.progress = progress * player.mediaPlayer.getDuration() / seekBar.getMax();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
//            player.mediaPlayer.seekTo(progress);
            player.userSeekTo(progress);
        }
    }



    private void startRecord() {
        Object service = this.getSystemService(Context.AUDIO_SERVICE);
        if(service != null) {
            mAudioManager = (AudioManager)service;
            audioBufSize = AudioTrack.getMinBufferSize(8000, 12, 2);
            audioTrack = new AudioTrack(0, 8000, 12, 2, this.audioBufSize, 1);
            mRecorder = new Recorder(this);
            mRecorder.startRecord((new Recorder.RecoderListener() {
                public void onData(byte[] data) {
                    if(audioTrack != null) {
                        audioTrack.write(data, 0, data.length);
                    }
                }
            }));
            audioTrack.play();
            isRecording = true;
        }
    }
}