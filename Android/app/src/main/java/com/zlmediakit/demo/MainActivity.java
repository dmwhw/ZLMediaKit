package com.zlmediakit.demo;

import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zlmediakit.demo.nosurfaceview.MyRtspPlayer;
import com.zlmediakit.demo.opengl.MyGLSurfaceView;
import com.zlmediakit.demo.surfaceview.SurfaceViewRtspPlayer;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "ZLMediaKit";
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET"};

    private SurfaceView surfaceView;
    private EditText rtsp;
    private SurfaceViewRtspPlayer rtspPlayer;
    private int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = this.findViewById(R.id.surfaceview);
        findViewById(R.id.btn).setOnClickListener(this);
        rtsp = findViewById(R.id.rtsp);
        findViewById(R.id.mediacode_only).setOnClickListener(this);
        findViewById(R.id.btn_startDown).setOnClickListener(this);

        vrv = findViewById(R.id.videoRenderView);



    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (MainActivity.this) {
                        if (rtspPlayer == null) {

                            rtspPlayer = new SurfaceViewRtspPlayer(surfaceView.getHolder().getSurface(), rtsp.getText().toString());
                            rtspPlayer.start();
                        }
                    }
                }
            }).start();

        } else if (view.getId() == R.id.mediacode_only) {
            Intent intent = new Intent(MainActivity.this, MediaCodeActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_startDown) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (MainActivity.this) {
                        if (myRtspPlayer == null) {
                            myRtspPlayer = new MyRtspPlayer(rtsp.getText().toString());
                            myRtspPlayer.setDataListener(new MyRtspPlayer.DataListener() {
                                @Override
                                public void onDataDecode(byte[] buffer, int h, int w) {
                                    if (!isInit) {
                                        ByteBuffer bb = ByteBuffer.allocate(buffer.length);
                                        bb.put(buffer);
                                        Log.e("opengl", "config!");

                                        MainActivity.this.vrv.setYuvDataSize(w, h);

                                        Log.e("opengl", "config finish!");

                                        isInit = true;
                                    } else {

                                        Log.e("opengl", "dataIn for view start " + buffer.length);
                                        MainActivity.this.vrv.feedData(buffer, 2);
                                        Log.e("opengl", "dataIn for view end..." + buffer.length);


                                    }
                                }
                            });
                            myRtspPlayer.start();
                        }else{
                            myRtspPlayer.release();
                        }
                    }


                }
            }).start();
        }

    }

    private MyGLSurfaceView vrv;
    private boolean isInit = false;
    private MyRtspPlayer myRtspPlayer;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtspPlayer != null) {
            rtspPlayer.release();
            rtspPlayer = null;
        }

        synchronized (MainActivity.this) {
            if (myRtspPlayer != null) {
                myRtspPlayer.release();
                myRtspPlayer = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        vrv.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onClick(findViewById(R.id.btn_startDown));
            new Thread((()->{
                SystemClock.sleep(15*1000);
                Intent intent = new Intent(MainActivity.this,MediaCodeActivity.class);
                MainActivity.this.startActivity(intent);
            })).start();
        }
        this.setTitle("Demo Count:" + (++i));

    }

    @Override
    protected void onPause() {
        super.onPause();
        vrv.onPause();
        this.isInit = false;
        synchronized (MainActivity.this) {
            if (myRtspPlayer != null) {
                myRtspPlayer.release();
                myRtspPlayer = null;
            }
        }
    }
}
