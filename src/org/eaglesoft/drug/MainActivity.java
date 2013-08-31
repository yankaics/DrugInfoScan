package org.eaglesoft.drug;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.eaglesoft.drug.animation.listener.RotationAnimationListener;
import org.eaglesoft.drug.server.ConnectionDetector;
import org.eaglesoft.drug.server.ConnectionHandler;
import org.eaglesoft.drug.server.DrugQueryServerManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.AbsCaptureActivity;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

public class MainActivity extends AbsCaptureActivity implements Callback  {
	
	private final static String TAG = "MainActivity";
	private  ImageButton submit ;
	private  ImageButton scanButton ;
	private  EditText codeInput ;
	private  SurfaceView surfaceView;
	private RelativeLayout drugInfo;
	private RelativeLayout drugScan;
	
	//药品信息
	private TextView drugCodeView;			//电子监管码
	private TextView drugNameView;			//药品名称
	private TextView drugSpeciView;			//药品规格
	private TextView drugPackView; 			//包装规格
	private TextView drugBussView;			//生产企业
	private TextView drugDateView;			//生产日期
	private TextView drugBatchView;			//生产批号
	private TextView drugPeriodView;		//有效期
	private TextView drugApprovalView;		//批准号
	private TextView drugFlowView;			//药品流向
	
	private ProgressDialog loadingMsgDialog;
	private AlertDialog.Builder alertDialogBuilder ;
	private AlertDialog mAlertDialog;
	
	private ConnectionHandler connHandler;
	private CaptureActivityHandler handler;
	
	private ViewfinderView viewfinderView;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	
	private InactivityTimer inactivityTimer;
	
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private boolean hasSurface ;
	private boolean isScan = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);
		
		connHandler = new ConnectionHandler(this);
		
		layoutInit();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	/**
	 * 开始扫描
	 */
	public void openScan(){
		Log.d(TAG, "开始扫描");
		isScan = true;
		drugInfo.setVisibility(View.GONE);
		drugScan.setVisibility(View.VISIBLE);
		sysInit();
		
		// 准备摄像头
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		if (!hasSurface) {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
			
		if (isScan && hasSurface)
			initCamera(surfaceHolder);

		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}
	/**
	 * 关闭扫描
	 */
	public void closeScan(){
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		if(CameraManager.get()!=null)
			CameraManager.get().closeDriver();
		drugScan.setVisibility(View.GONE);
		drugInfo.setVisibility(View.VISIBLE);
		hasSurface = true;
		isScan = false;
	}
	
	/**
	 * 切换扫描状态
	 */
	public void toggleScan(){
		//showMessage(getString(R.string.toggle_scan_stat));
		if(isScan)
			closeScan();
		else
			openScan();
		closeMessage();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeScan();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestory()...");
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	private void sysInit(){
		CameraManager.init(getApplication(),drugScan);
	}
	
	
	private void layoutInit(){
		loadingMsgDialog = new ProgressDialog(this);
		alertDialogBuilder = new AlertDialog.Builder(this);
		
		mAlertDialog = alertDialogBuilder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			}).create();
		
		
		submit = (ImageButton)findViewById(R.id.submit) ;
		scanButton = (ImageButton)findViewById(R.id.imgScan);
		codeInput = (EditText)findViewById(R.id.codeInput);
		drugInfo = (RelativeLayout)findViewById(R.id.drugInfo);
		drugScan = (RelativeLayout)findViewById(R.id.drugScan);
		surfaceView = (SurfaceView)findViewById(R.id.preview_view);
		viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
		
		drugCodeView = (TextView)findViewById(R.id.drugCode);
		drugNameView = (TextView)findViewById(R.id.drugName);
		drugSpeciView = (TextView)findViewById(R.id.drugSpeci);
		drugPackView = (TextView)findViewById(R.id.drugPack);
		drugBussView = (TextView)findViewById(R.id.drugBuss);
		drugDateView = (TextView)findViewById(R.id.drugDate);
		drugPeriodView = (TextView)findViewById(R.id.drugPeriod);
		drugBatchView = (TextView)findViewById(R.id.drugBatch);
		drugApprovalView = (TextView)findViewById(R.id.drugApproval);
		drugFlowView = (TextView)findViewById(R.id.drugFlow);
		
		inactivityTimer = new InactivityTimer(this);
		
		submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(v==submit){
					//先隐藏键盘
					hiddenKeyboard();
					String text = codeInput.getText().toString();
					sendCodeToServer(text);
				}
				
			}
		});
		
		codeInput.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(keyCode==event.KEYCODE_ENTER){
					//先隐藏键盘，然后出发查询按钮
					hiddenKeyboard();
					submit.performClick();
				}
				return false;
			}
		});
		
		final Animation scanBtnAnim = AnimationUtils.loadAnimation(this,R.anim.scan_btn_animation );
		RotationAnimationListener ral = new RotationAnimationListener(scanButton,scanBtnAnim,MainActivity.this);
		
	}
	
	private void hiddenKeyboard(){
		//隐藏键盘
		
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(
				MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		//--
	}

	private void sendCodeToServer(final String code){
		
		if(code.length()!=20){
			showMessage(getResources().getString(R.string.drugCodeiSError));
			return ;
		}
		showLoading(getResources().getString(R.string.querying));
		Log.d(TAG, "发送信息中。。。。。");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isHasNetwork = ConnectionDetector.isConnectingToInternet(MainActivity.this);
				if(!isHasNetwork){
					connHandler.sendEmptyMessage(R.id.network_not_found);
					return;
				}
				DrugQueryServerManager.query(code,connHandler);
			}
		}).start();
		
		
		Log.d(TAG, "~------------------~");
	}
	
	public void refreshDrugInfo(Map<String,Object> drugInfo){
		closeScan();
		String drugName = (String)drugInfo.get("drugName");
		drugCodeView.setText(codeInput.getText());
		drugNameView.setText(drugName);
		drugSpeciView.setText( (String) drugInfo.get("preSpec") );
		drugPackView.setText( (String) drugInfo.get("packageSpec") );
		drugBussView.setText( (String) drugInfo.get("manufacturer") );
		drugDateView.setText( (String) drugInfo.get("productionDate") );
		drugBatchView.setText( (String) drugInfo.get("batchNumber") );
		drugPeriodView.setText( (String) drugInfo.get("validDate") );
		drugApprovalView.setText( (String) drugInfo.get("licenseNumber") );
		drugFlowView.setText( (String) drugInfo.get("flow") );
		
	}
	
	public void showLoading(String msg){
		loadingMsgDialog.setMessage(msg);
		loadingMsgDialog.show();
	}
	
	public void closeLoading(){
		loadingMsgDialog.cancel();
	}
	
	public void showMessage(String msg){
		mAlertDialog.setMessage(msg);
		mAlertDialog.show();
		//mAlertDialog = alertDialogBuilder.setMessage(msg).show();
		//Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	public void closeMessage(){
		if(mAlertDialog.isShowing()){
			mAlertDialog.cancel();
		}
		
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}
	
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	

	private  AlertDialog.Builder aboutAlertDialogBuilder; 
	private  WebView mWebView ;
	private  AlertDialog aboutAlertDialog ;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int itemId = item.getItemId();
		if(mWebView==null){
			mWebView = new WebView(this);
			mWebView
			.loadUrl("http://eaglesoft.org/public/jsp/web/ColumnArticleView.xhtml?columnNo=C01001");			
		}
		
		if(aboutAlertDialogBuilder==null){
			aboutAlertDialogBuilder = new AlertDialog.Builder(this);
			aboutAlertDialogBuilder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
		}
		
		if(aboutAlertDialog==null){
			aboutAlertDialog = aboutAlertDialogBuilder.setTitle("loading about page")
					.setView(mWebView).create();
		}
		switch(itemId){
		case R.id.about : 
			aboutAlertDialog.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public ViewfinderView getViewfinderView() {
		// TODO Auto-generated method stub
		return viewfinderView;
	}

	@Override
	public Handler getHandler() {
		// TODO Auto-generated method stub
		return handler;
	}

	@Override
	public void drawViewfinder() {
		// TODO Auto-generated method stub
		viewfinderView.drawViewfinder();
	}

	@Override
	public void handleDecode(Result obj, Bitmap barcode) {
		// TODO Auto-generated method stub
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		 playBeepSoundAndVibrate();
		 codeInput.setText(obj.getText());
		 Log.d(TAG, "已查询：->"+obj.getText());
	}
	
	private static final long VIBRATE_DURATION = 200L;
	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "surfaceCreated ....");
		if (!hasSurface) {
			if(isScan)
				initCamera(holder);
			hasSurface = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "surface destoryed...");
		hasSurface = false;
		closeScan();
	}
	
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
}
