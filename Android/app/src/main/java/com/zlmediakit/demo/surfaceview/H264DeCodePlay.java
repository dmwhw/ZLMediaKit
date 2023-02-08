package com.zlmediakit.demo.surfaceview;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author zhangqingfa
 * @createDate 2020/12/10 11:39
 * @description 解码H264播放
 */
public class H264DeCodePlay {

    private static final String TAG = "zqf-dev";
    //视频路径
    private String videoPath;
    //使用android MediaCodec解码
    private MediaCodec mediaCodec;
    private Surface surface;
    private boolean isClosed=false;

     @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
     public H264DeCodePlay(Surface surface) {
        this.surface = surface;


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void startDecode(){
        if (mediaCodec!=null){
          //  this.release();
            isClosed=false;
        }
        initMediaCodec();
        mediaCodec.start();
        SystemClock.sleep(3000);
    }

     @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
     private void initMediaCodec() {
        try {
            Log.e(TAG, "videoPath " + videoPath);
            //创建解码器 H264的Type为  AAC
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            //创建配置
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 960, 540);
            //设置解码预期的帧速率【以帧/秒为单位的视频格式的帧速率的键】
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //配置绑定mediaFormat和surface
            mediaCodec.configure(mediaFormat, surface, null, 0);
//            mediaCodec.setCallback(new MediaCodec.Callback() {
//                @Override
//                public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
//
//                }
//
//                @Override
//                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
//
//                }
//
//                @Override
//                public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
//
//                }
//
//                @Override
//                public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
            //创建解码失败
            Log.e(TAG, "创建解码失败");
        }
    }


    //https://blog.csdn.net/Ae_fring/article/details/110955380
    //https://blog.csdn.net/yinshipin007/article/details/124203935
    //https://blog.csdn.net/wangbuji/article/details/124990244
    //https://www.cnblogs.com/liangjingfu/p/15439086.html
    public synchronized void feedBytes(byte[] bytes) {
        if (mediaCodec ==null){
            return;
        }
        //2、拿到 mediaCodec 所有队列buffer[]
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        // 查询10000毫秒后，如果dSP芯片的buffer全部被占用，返回-1；存在则大于0
        int inIndex = mediaCodec.dequeueInputBuffer(10000);
        if (inIndex >= 0) {
            //根据返回的index拿到可以用的buffer
            ByteBuffer byteBuffer = inputBuffers[inIndex];
            //清空缓存
            byteBuffer.clear();
            //开始为buffer填充数据
            byteBuffer.put(bytes, 0, bytes.length);
            //填充数据后通知mediacodec查询inIndex索引的这个buffer,
            mediaCodec.queueInputBuffer(inIndex, 0, bytes.length, 0, 0);
        } else {
            //等待查询空的buffer
            return;
        }
        //mediaCodec 查询 "mediaCodec的输出方队列"得到索引
        int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
        Log.e(TAG, "outIndex " + outIndex);
        if (outIndex >= 0) {
            // get w-h start
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            ByteBuffer outputBuffer = outputBuffers[outIndex];
            //记录pps和sps
            int type = outputBuffer.get(4) & 0x07; // 判断是什么帧
           // if (type == 7  ) {
                byte[] outData = new byte[info.size];
                outputBuffer.get(outData);

                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                int width = outputFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = outputFormat.getInteger(MediaFormat.KEY_HEIGHT);
                Log.e("whwtest",width+":"+height+"type:"+type);
           // }

            // get w-h end


            //如果surface绑定了，则直接输入到surface渲染并释放
            mediaCodec.releaseOutputBuffer(outIndex, true);
        } else {
            Log.e(TAG, "没有解码成功");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void release(){
        if (!isClosed){
            mediaCodec.flush();
            mediaCodec.stop();
            mediaCodec.reset();
            mediaCodec.release();
            mediaCodec = null;
        }
    }

}
