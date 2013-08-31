package com.zxing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.zxing.view.ViewfinderView;

public abstract class AbsCaptureActivity extends Activity  {
	public  abstract ViewfinderView getViewfinderView();
	public  abstract Handler getHandler();
	public  abstract void drawViewfinder();
	public  abstract void handleDecode(Result obj, Bitmap barcode);
}
