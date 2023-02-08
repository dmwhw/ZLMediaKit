package com.zlmediakit.demo;

import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.zlmediakit.demo.nosurfaceview.MyRtspPlayer;
import com.zlmediakit.demo.opengl.MyGLSurfaceView;

import java.nio.ByteBuffer;

public class MediaCodeActivity extends AppCompatActivity implements View.OnClickListener, MyRtspPlayer.DataListener {

    private EditText et;

    private MyGLSurfaceView vrv;
    private boolean isInit=false;
    private MyRtspPlayer myRtspPlayer;


    private MyGLSurfaceView vrv2;
    private boolean isInit2=false;
    private MyRtspPlayer myRtspPlayer2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_code);
        findViewById(R.id.btn).setOnClickListener(this);
        et = findViewById(R.id.rtsp);
        vrv=findViewById(R.id.videoRenderView);
        vrv2=findViewById(R.id.videoRenderView2);
        new Thread((()->{
            SystemClock.sleep(25*1000);
            Intent intent = new Intent(MediaCodeActivity.this,MediaCodeActivity.class);
            MediaCodeActivity.this.finish();
        })).start();
    }

     @Override
    public   void onClick(View view) {
        if (view.getId()==R.id.btn){
            new Thread(){
                @Override
                public void run() {
                    synchronized(MediaCodeActivity.this) {
                        if (myRtspPlayer == null) {
                            myRtspPlayer = new MyRtspPlayer(et.getText().toString());
                            myRtspPlayer.setDataListener(MediaCodeActivity.this);
                            myRtspPlayer.start();

                        }
                        if (myRtspPlayer2 == null) {
                            myRtspPlayer2 = new MyRtspPlayer("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4");
                            myRtspPlayer2.setDataListener(new MyRtspPlayer.DataListener() {
                                @Override
                                public void onDataDecode(byte[] buffer, int h, int w) {
                                    if (!isInit2){
                                        ByteBuffer bb = ByteBuffer.allocate(buffer.length);
                                        bb.put(buffer);
                                        Log.e("opengl","config!");

                                        vrv2.setYuvDataSize(w,h);

                                        Log.e("opengl","config finish!");

                                        isInit2=true;
                                    }
                                    else{

                                        Log.e("opengl","dataIn for view start "+buffer.length);
                                        vrv2.feedData(buffer,2);
                                        Log.e("opengl","dataIn for view end..."+buffer.length);



                                    }
                                }
                            });
                            myRtspPlayer2.start();

                        }
                    }
                }
            }.start();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.onClick(this.findViewById(R.id.btn));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRtspPlayer!=null){
            this.myRtspPlayer.release();
            this.myRtspPlayer=null;
        }
        if (myRtspPlayer2!=null){
            this.myRtspPlayer2.release();
            this.myRtspPlayer2=null;
        }

    }

    @Override
    public void onDataDecode(byte[] buffer,int h,int w) {
        if (!isInit){
            ByteBuffer bb = ByteBuffer.allocate(buffer.length);
            bb.put(buffer);
            Log.e("opengl","config!");

            this.vrv.setYuvDataSize(w,h);

             Log.e("opengl","config finish!");

            isInit=true;
        }
        else{

            Log.e("opengl","dataIn for view start "+buffer.length);
                this.vrv.feedData(buffer,2);
                Log.e("opengl","dataIn for view end..."+buffer.length);



        }
    }
}