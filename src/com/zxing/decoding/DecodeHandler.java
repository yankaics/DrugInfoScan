/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxing.decoding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;

import org.eaglesoft.drug.R;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.zxing.AbsCaptureActivity;
import com.zxing.camera.CameraManager;
import com.zxing.camera.PlanarYUVLuminanceSource;

final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final AbsCaptureActivity activity;
  private final MultiFormatReader multiFormatReader;

  DecodeHandler(AbsCaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case R.id.decode:
        Log.d(TAG, "Got decode message");
        Log.d(TAG,"width : "+message.arg1+", height : "+message.arg2);
        decode((byte[]) message.obj,  message.arg1,message.arg2);
        break;
      case R.id.quit:
        Looper.myLooper().quit();
        break;
    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
 * @throws IOException 
   */
  private void decode(byte[] data, int width, int height)  {
	/**
	 * 修正竖屏
	 */
	 
	  byte[] rotatedData = new byte[data.length];
	  for(int y = 0; y < height; y++) {
		  for(int x = 0; x < width; x++)
	      rotatedData[x * height + height - y - 1] = data[x + y * width];
	      //rotatedData[x * height + y] = data[x + y * width];
	  }
	  int tmp = width; // Here we are swapping, that's the difference to #11
	  width = height;
	  height = tmp;
	  data = rotatedData;
	  
    long start = System.currentTimeMillis();
    Result rawResult = null;
    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
    File file = new File(Environment.getExternalStorageDirectory()+"/downloadTest/2_"+System.currentTimeMillis()+".bmp");
    
    try {
      rawResult = multiFormatReader.decodeWithState(bitmap);
    } catch (ReaderException re) {
      // continue
    } finally {
      multiFormatReader.reset();
    }

    if (rawResult != null) {
      //writeSuccessData(data);
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
      Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
      Bundle bundle = new Bundle();
      bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
      message.setData(bundle);
      //Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
      message.sendToTarget();
    }
  }

  private void writeSuccessData(byte[] data){
	  try{
		  Log.d(TAG, "获取成功样张");
		  File file = new File(Environment.getExternalStorageDirectory()+"/downloadTest/1.txt");
		  FileOutputStream fos = new FileOutputStream(file);
		  fos.write(data);
		  fos.flush();
		  fos.close();
		  Log.d(TAG, "样张写入完成");
	  }catch(Exception e){
		  e.printStackTrace();
	  }
  }
}
