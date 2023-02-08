package com.zlmediakit.demo.nosurfaceview;

import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import com.zlmediakit.jni.ZLMediaKit;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyRtspPlayer implements H264DeCodePlay2.EventListener{
    private ZLMediaKit.MediaPlayer _player;
    private String url;
    public static final String TAG = "ZLMediaKit";
    private H264DeCodePlay2 h264DeCodePlay;
     private ExecutorService es = Executors.newSingleThreadExecutor();
    public  static interface DataListener {
        void onDataDecode(byte[] buffer,int h,int w);
    }


    private boolean hasOpend = false;
    private boolean isRelease = false;




    private DataListener dataListener;
    public MyRtspPlayer(String url) {
        this.url = url;
        h264DeCodePlay = new H264DeCodePlay2();
        h264DeCodePlay.setEventListener(this);

    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
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

     public void release() {
        synchronized (this) {
            h264DeCodePlay.release();
            if (_player != null) {
                _player.release();
                _player=null;
                isRelease = true;
            }
            es.shutdown();
        }



    }

    private void reconnect(){
        // reopen
        new Thread(){
            @Override
            public void run() {
                synchronized (MyRtspPlayer.this) {
                    if (_player!=null){
                        _player.release();
                        _player=null;
                    }
                    if (!isRelease){
                        SystemClock.sleep(1000);
                        test_player(MyRtspPlayer.this.url);
                    }
                }
            }
        }.start();
    }


    private synchronized  void test_player(String rtsp) {

        _player = new ZLMediaKit.MediaPlayer(rtsp, new ZLMediaKit.MediaPlayerCallBack() {
            @Override
            public void onPlayResult(int code, String msg) {
                Log.d(TAG, "onPlayResult:" + code + "," + msg);
                if (code!=0){
                    reconnect();
                }
            }

            @Override
            public void onShutdown(int code, String msg) {
                Log.d(TAG, "onShutdown:" + code + "," + msg);
                if (code!=0){
                    reconnect();
                }

            }

            @Override
            public void onData(ZLMediaKit.MediaFrame frame) {
                if (frame.trackType == 0) {
                    if(!es.isShutdown() &&! es.isTerminated()){
                        es.execute(()-> h264DeCodePlay.feedBytes(frame.data));
                    }


                }
                Log.v(TAG, "onData:"
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

    @Override
    public void onDataDecode(byte[] buffer,int h,int w) {
        if (this.dataListener!=null){
            this.dataListener.onDataDecode(buffer,h ,w);

        }
    }
}
