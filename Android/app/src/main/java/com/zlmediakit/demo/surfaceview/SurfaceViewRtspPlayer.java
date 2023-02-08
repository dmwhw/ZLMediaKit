package com.zlmediakit.demo.surfaceview;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import com.zlmediakit.jni.ZLMediaKit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurfaceViewRtspPlayer {
    public static final String TAG = "ZLMediaKit";
    private H264DeCodePlay h264DeCodePlay;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private ZLMediaKit.MediaPlayer _player;


    private Surface surface;
    private String url;
    private boolean hasOpend = false;
    private boolean isPaused = false;
    private boolean isReleased = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SurfaceViewRtspPlayer(Surface surface, String url) {
        this.surface = surface;
        this.url = url;
        h264DeCodePlay = new H264DeCodePlay(this.surface);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start() {
        if (hasOpend) {
            return;
        }
        synchronized (this) {
            if (hasOpend) {
                return;
            }
            hasOpend = true;

            h264DeCodePlay.startDecode();

            test_player(this.url);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void release() {
        h264DeCodePlay.release();
        _player.release();

    }

    public void pause() {

    }


    public void resume() {

    }


    private void test_player(String rtsp) {
        _player = new ZLMediaKit.MediaPlayer(rtsp, new ZLMediaKit.MediaPlayerCallBack() {
            @Override
            public void onPlayResult(int code, String msg) {
                Log.d(TAG, "onPlayResult:" + code + "," + msg);
            }

            @Override
            public void onShutdown(int code, String msg) {
                Log.d(TAG, "onShutdown:" + code + "," + msg);
            }

            @Override
            public void onData(ZLMediaKit.MediaFrame frame) {
                if (frame.trackType == 0) {
                    es.execute(new Runnable() {
                        @Override
                        public void run() {
                            h264DeCodePlay.feedBytes(frame.data);
                        }
                    });
                }
                Log.d(TAG, "onData:"
                        + frame.trackType + ","
                        + frame.codecId + ","
                        + frame.dts + ","
                        + frame.pts + ","
                        + frame.keyFrame + ","
                        + frame.prefixSize + ","
                        + frame.data.length);
            }
        });
    }

}
