package org.eaglesoft.drug.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.eaglesoft.drug.R;

import com.google.gson.Gson;

import android.os.Message;
import android.util.Log;

public class DrugQueryServerManager {
	private final static String TAG = "DrugQueryServerManager";
	private final static String REQUEST_URL = "http://api.aomask.com/drugInfoQuery.json";
	private final static String PARAM_CODE_NAME = "code";
	private final static String PARAM_KEY_NAME = "e_key";
	private final static String E_KEY = "jsutForFuns";
	private static DefaultHttpClient mHttpClient;
	private static HttpPost mPost;
	public static void query(String code,ConnectionHandler callbackHandler){
		
		try {
			
			if(mHttpClient==null){
				mHttpClient = new DefaultHttpClient();
				mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			}
			if(mPost==null){
				mPost = new HttpPost(REQUEST_URL);
			}
			List<NameValuePair> params = new LinkedList();
			params.add(new BasicNameValuePair(PARAM_KEY_NAME, E_KEY));
			params.add(new BasicNameValuePair(PARAM_CODE_NAME, code));
			mPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			
			HttpResponse mResponse = mHttpClient.execute(mPost);
			
			if(mResponse.getStatusLine().getStatusCode()==200){
				HttpEntity entity = mResponse.getEntity();
				String responseStr = EntityUtils.toString(entity, HTTP.UTF_8);
				
				Gson gson = new Gson();
				Map<String,Object> pack = new HashMap();
				pack = gson.fromJson(responseStr, new HashMap<String,Object>().getClass());
				//Log.d(TAG, pack.toString());
				String result = (String)pack.get("result");
				if("SUCCESS".equals(result)){
					Message msg = new Message();
					msg.what = R.id.drug_info_success;
					msg.obj = pack;
					callbackHandler.sendMessage(msg);
				}else if("FAIL".equals(result)){
					Message msg = new Message();
					msg.what = R.id.drug_info_fail;	
					msg.obj = pack;
					callbackHandler.sendMessage(msg);
				}
			}else{
				callbackHandler.sendEmptyMessage(R.id.server_error);
			}
			
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackHandler.sendEmptyMessage(R.id.server_error);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackHandler.sendEmptyMessage(R.id.server_error);
		}
	}
}
