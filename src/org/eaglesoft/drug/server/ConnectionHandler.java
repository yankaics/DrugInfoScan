package org.eaglesoft.drug.server;

import java.util.Map;

import org.eaglesoft.drug.MainActivity;
import org.eaglesoft.drug.R;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ConnectionHandler extends Handler {
	private final static String TAG = "ConnectionHandler";
	private MainActivity activity;
	public ConnectionHandler(MainActivity activity){
		this.activity = activity;
	}
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		Map<String,Object> pack = null;
		switch(msg.what){
		case R.id.drug_info_success:
			pack = (Map)msg.obj; 
			activity.showMessage((String)pack.get("msg"));
			activity.refreshDrugInfo((Map)pack.get("drugInfo"));
			
			break;
		case R.id.drug_info_fail:
			pack = (Map)msg.obj; 
			activity.showMessage((String)pack.get("msg"));
			break;
		case R.id.network_not_found:
			activity.showMessage(activity.getResources().getString(R.string.not_netword));
			break;
		case R.id.server_error:
			activity.showMessage(activity.getResources().getString(R.string.server_error));
			break;
		}
		activity.closeLoading();
	}
	
	
	
	
}
