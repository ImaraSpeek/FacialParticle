package com.watchdog.pubnub;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class PubNub {
	
	private static PubNub instance;
	private Pubnub pubnub = new Pubnub("pub-aa2a0352-26f3-49e2-a8b0-d4900483fa91", "sub-fa66338c-bfc6-11e1-875e-d3dc39565a38");
	private List<PubNubReceiver> receivers = new ArrayList<PubNubReceiver>();
	
	public static PubNub getInstance() {
		if (instance == null) {
			instance = new PubNub();
		}
		return instance;
	}
	
	private PubNub() {
	}
	
	public void subscribe() {
		try {
	    	pubnub.subscribe("WatchDogChannel", callback);
	    } catch (PubnubException e) {
	    	Log.d("PUBNUB", e.toString());
	    }
	}
	
	Callback callback = new Callback() {

		@Override
		public void connectCallback(String channel, Object message) {
			Log.d("PUBNUB", "SUBSCRIBE : CONNECT on channel:" + channel + " : "
					+ message.getClass() + " : " + message.toString());
		}

		@Override
		public void disconnectCallback(String channel, Object message) {
			Log.d("PUBNUB", "SUBSCRIBE : DISCONNECT on channel:" + channel
					+ " : " + message.getClass() + " : " + message.toString());
		}

		public void reconnectCallback(String channel, Object message) {
			Log.d("PUBNUB", "SUBSCRIBE : RECONNECT on channel:" + channel
					+ " : " + message.getClass() + " : " + message.toString());
		}

		@Override
		public void successCallback(String channel, Object message) {
			Log.d("PUBNUB",
					"SUBSCRIBE : " + channel + " : " + message.getClass()
							+ " : " + message.toString());
			String res;
			try {
				if (message.getClass().equals(JSONObject.class)) {
					res = new JSONObject(message.toString()).getString("text");
				} else {
					res = message.toString();
				}
				for (PubNubReceiver r : receivers) {
					r.onReceiveMessage(res);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void errorCallback(String channel, PubnubError error) {
			Log.d("PUBNUB", "SUBSCRIBE : ERROR on channel " + channel + " : "
					+ error.toString());
		}
	};
	
	public void unsubscribe() {
		pubnub.unsubscribe("WatchDogChannel");
	}
	
	public void addReceiver(PubNubReceiver r) {
		receivers.add(r);
	}
	
	public void removeReceiver(PubNubReceiver r) {
		receivers.remove(r);
	}
	
	public void sendMessage(String message) {
		pubnub.publish("WatchDogChannel", message, null);
	}
	
	public interface PubNubReceiver {
		public void onReceiveMessage(String message);
	}

}
