/*
 * SystemHelper.java executed superuser commands Copyright (C) 2011 Andreas Koch
 * <koch.trier@gmail.com>
 * 
 * This software was supported by the University of Trier
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
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

package com.evozi.droidsniff.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.evozi.droidsniff.activities.ListenActivity;
import com.evozi.droidsniff.auth.Auth;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SystemHelper {

	static Process process = null;
	
	public static boolean execSUCommand(String command, boolean debug) {
		try {
			if (process == null || process.getOutputStream() == null) {
				process = new ProcessBuilder().command("su").start();
			}
			if (Constants.DEBUG) {
				Log.d(Constants.APPLICATION_TAG, "Command: " + command);
			}
			if (debug) {
				ListenActivity.debugBuffer.append("executing command: " + command + "\n");
			}
			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			if (ListenActivity.debugging || Constants.DEBUG) {
				StringBuffer sb = new StringBuffer();
				BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				Thread.sleep(10);
				while (bre.ready()) {
					sb.append(bre.readLine());
				}
				String s = sb.toString();
				if (!s.replaceAll(" ", "").equalsIgnoreCase("")) {
					Log.e(Constants.APPLICATION_TAG, "Error with command: " + s);
					if (debug) {						
						ListenActivity.debugBuffer.append("Error with command: " + command + ": " + s + "\n");
					}
					return false;
				}
				sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				Thread.sleep(10);
				while (br.ready()) {
					sb.append(br.readLine());
				}
				s = sb.toString();
				if (!s.replaceAll(" ", "").equalsIgnoreCase("")) {
					Log.e(Constants.APPLICATION_TAG, "Output from command: " + s);
					if (debug) {						
						ListenActivity.debugBuffer.append("Output from command: " + command + ": " + s + "\n");
					}
					return false;
				}
			}
			Thread.sleep(100);
			return true;
		} catch (Exception e) {
			Log.e(Constants.APPLICATION_TAG, "Error executing: " + command, e);
			return false;
		}
	}
	
	public static void execNewSUCommand(String command, boolean debug) {
		try {
			if (Constants.DEBUG) {
				Log.d(Constants.APPLICATION_TAG, "Command: " + command);
			}
			Process process = new ProcessBuilder().command("su").start();
			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			if (ListenActivity.debugging || Constants.DEBUG) {
				StringBuffer sb = new StringBuffer();
				BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				Thread.sleep(10);
				while (bre.ready()) {
					sb.append(bre.readLine());
				}
				String s = sb.toString();
				if (!s.replaceAll(" ", "").equalsIgnoreCase("")) {
					Log.e(Constants.APPLICATION_TAG, "Error with command: " + sb.toString());
					if (debug) {						
						ListenActivity.debugBuffer.append("Error with command: " + command + ": " + s + "\n");
					}
				}
				sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				Thread.sleep(10);
				while (br.ready()) {
					sb.append(br.readLine());
				}
				s = sb.toString();
				if (!s.replaceAll(" ", "").equalsIgnoreCase("")) {
					Log.e(Constants.APPLICATION_TAG, "Output from command: " + s);
					if (debug) {						
						ListenActivity.debugBuffer.append("Output from command: " + command + ": " + s + "\n");
					}
				}
			}
			Thread.sleep(100);
		} catch (Exception e) {
			Log.e(Constants.APPLICATION_TAG, "Error executing: " + command, e);
		}
	}

	public static String getDroidSheepBinaryPath(Context c) {
		return c.getFilesDir().getAbsolutePath() + File.separator + "droidsniff";
	}

	public static String getARPSpoofBinaryPath(Context c) {
		return c.getFilesDir().getAbsolutePath() + File.separator + "arpspoof";
	}

	public static void saveAuthToFile(Context c, Auth a) {
		File dir = new File(c.getFilesDir() + File.separator + "saved");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		File f = new File(c.getFilesDir() + File.separator + "saved" + File.separator + "droidsniff" + a.getId());
		try {
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(a);
			out.close();
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			bos.writeTo(new FileOutputStream(f.getAbsolutePath()));
			a.setSaved(true);
		} catch (IOException ioe) {
			Log.e("serializeObject", "error", ioe);
		}
	}
	
	public static void deleteAuthFile(Context c, Auth a) {
		if (a == null) {
			return;
		}
		File f = new File(c.getFilesDir() + File.separator + "saved" + File.separator + "droidsniff" + a.getId());
		if (f.exists()) {
			for (int i = 0; i < 5; i++) {
				if (f.delete()) break; // In case deletion fails, retry 5 times...
			}
			
		}
		a.setSaved(false);
	}


	public static void readAuthFiles(Context c, Handler handler) {
		File f = new File(c.getFilesDir() + File.separator + "saved");
		if (!f.exists() || !f.isDirectory()) {
			Log.e(Constants.APPLICATION_TAG, c.getFilesDir() + File.separator + "saved" + " does not exist or is no folder!");
			return;
		}
		
		for (File objFile : f.listFiles()) {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream(objFile));
				Auth object = (Auth) in.readObject();
				in.close();
				object.setSaved(true);
				Message m = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putSerializable(Constants.BUNDLE_KEY_AUTH, object);
				bundle.putString(Constants.BUNDLE_KEY_TYPE, Constants.BUNDLE_TYPE_LOADAUTH);
				m.setData(bundle);
				handler.sendMessage(m);
			} catch (Exception e) {
				Log.e(Constants.APPLICATION_TAG, "Error while deserialization!", e);
			}
		}
	}

	public static void debugInformation(Context c) {
		ListenActivity.debugBuffer.append("Droidsniff path: " + getDroidSheepBinaryPath(c) + "\n");
		ListenActivity.debugBuffer.append("ARPSpoof Path: " + getARPSpoofBinaryPath(c) + "\n");
		ListenActivity.debugBuffer.append("Testing SU\n");
		execNewSUCommand("", true);
	}
}
