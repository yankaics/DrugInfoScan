package org.eaglesoft.drug.animation.listener;

import org.eaglesoft.drug.MainActivity;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

public class RotationAnimationListener implements AnimationListener {
	private final static String TAG = "AnimationL";
	private View view;
	private boolean running = false;
	private View.OnClickListener onClick;
	private Animation anim;
	
	private MainActivity mainActivity;
	
	public RotationAnimationListener(View view,Animation anim,MainActivity mainActivity){
		this.view = view;
		this.anim = anim;
		this.mainActivity = mainActivity;
		anim.setAnimationListener(this);
		onClick = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				v.startAnimation(RotationAnimationListener.this.anim);
			}
		};
		view.setOnClickListener(onClick);
		
	}
	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		Log.d(TAG, "start");
		running  = true;
		view.setOnClickListener(null);
		RotationAnimationListener.this.mainActivity.toggleScan();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		Log.d(TAG, "end");
		
		running = false;
		
		view.setOnClickListener(onClick);
		
	}

}
