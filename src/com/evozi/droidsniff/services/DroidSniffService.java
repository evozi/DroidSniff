package com.evozi.droidsniff.services;

import java.io.IOException;

import com.evozi.droidsniff.activities.ListenActivity;
import com.evozi.droidsniff.arpspoof.ExecuteCommand;
import com.evozi.droidsniff.helper.Constants;
import com.evozi.droidsniff.helper.SystemHelper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;


public class DroidSniffService extends IntentService {

	private volatile Thread myThread;
	private static volatile WifiManager.WifiLock wifiLock;
	private static volatile PowerManager.WakeLock wakeLock;

	public DroidSniffService() {
		super("ListenService");
	}

	@Override
	public void onHandleIntent(Intent intent) {
		final String command = SystemHelper.getDroidSheepBinaryPath(this);
		SystemHelper.execSUCommand("chmod 777 " + SystemHelper.getDroidSheepBinaryPath(this), ListenActivity.debugging);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
		wifiLock.acquire();
		wakeLock.acquire();
		
		try {
			myThread = new ExecuteCommand(command, true);
			myThread.setDaemon(true);
			myThread.start();
			myThread.join();
		} catch (IOException e) {
			Log.e(Constants.APPLICATION_TAG, "error initializing DroidSniff command", e);
		} catch (InterruptedException e) {
			Log.i(Constants.APPLICATION_TAG, "DroidSniff was interrupted", e);
		} finally {
			if (myThread != null)
				myThread = null;
			if (wifiLock != null && wifiLock.isHeld()) {
				wifiLock.release();
			}
			if (wakeLock != null && wakeLock.isHeld()) {
				wakeLock.release();
			}
			stopForeground(true);
		}
	}

	@Override
	public void onDestroy() {
		//at the suggestion of the internet
		if (myThread != null) {
			Thread tmpThread = myThread;
			myThread = null;
			tmpThread.interrupt();
		}
		SystemHelper.execSUCommand(Constants.CLEANUP_COMMAND_DROIDSNIFF, ListenActivity.debugging);
	}
}