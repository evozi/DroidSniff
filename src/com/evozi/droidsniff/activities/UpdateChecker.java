/*
 * UpdateChecker.java check for latest updates
 * Copyright (C) 2012 Evozi <email@evozi.com>
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

package com.evozi.droidsniff.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import com.evozi.droidsniff.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class UpdateChecker {

	private String versionUrl;
	private String remoteApkUrl;
	private int alertIcon;
	private final String TAG_SUFFIX = "UpdateChecker";
	private String TAG = TAG_SUFFIX;
	// The name of the saved file
	public String localApkName = "DroidSniff.apk";
	// The dialog that tells you to update
	public String alertTitle = "Update now";
	// public String alertMessage = "Download file and install";
	// The dialog which notifies there was an error checking for updates
	public String alertTitleError = "Download error";
	public String alertMessageError = "There was an error downloading the file\nPlease check fb.com/evozi for latest update";
	// Download progress text
	public String progressMessage = "Downloading file...";

	private Handler mHandler;
	private Context context;
	private AlertDialog alertUpdate, alertError;
	private boolean enabled = true;

	/* This Thread checks for Updates in the Background */
	private Thread checkUpdate;
	private Activity activity;

	/**
	 * Instantiates the update checker
	 * 
	 * @param c
	 *            The activity to be used for displaying the messages
	 * @param versionUrl
	 *            The url of the file containing the version name
	 * @param remoteApkUrl
	 *            The url of the apk
	 * @param alertIcon
	 *            The icon to show in the dialog, usually the application icon
	 */
	public UpdateChecker(Activity c, String versionUrl, String remoteApkUrl,
			int alertIcon) {
		this.activity = c;
		this.context = c.getApplicationContext();
		this.versionUrl = versionUrl;
		this.remoteApkUrl = remoteApkUrl;
		this.alertIcon = alertIcon;
		mHandler = new Handler();
	}

	/**
	 * Starts to check for updates
	 */
	public void startUpdateChecker() {
		if (!enabled)
			return;
		if (checkUpdate == null || !checkUpdate.isAlive()
				|| checkUpdate.isInterrupted()) {
			checkUpdate = new Thread() {
				public void run() {
					checkupdate();
				}
			};
			checkUpdate.start();
		}
	}

	/**
	 * Interrupts update check
	 */
	public void stopUpdateChecker() {
		if (!enabled)
			return;
		if (checkUpdate.isAlive() && !checkUpdate.isInterrupted())
			try {
				checkUpdate.interrupt();
			} catch (Exception e) {
				Log.w(TAG, "checkUpdate.interrupt() exception");
				// e.printStackTrace();
			}
	}

	/* This Runnable creates a Dialog and asks the user to download the update */
	private Runnable showError = new Runnable() {
		public void run() {
			alertError = new AlertDialog.Builder(activity)
					.setIcon(alertIcon)
					.setTitle(alertTitleError)
					.setMessage(alertMessageError)
					.setCancelable(true)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	};

	/* This Runnable creates an Error message */
	private Runnable showUpdate = new Runnable() {
		public void run() {
			alertUpdate = new AlertDialog.Builder(activity)
					.setIcon(alertIcon)
					.setTitle(alertTitle)
					.setMessage(R.string.updatetext)
					.setCancelable(true)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Log.d(TAG, "TAG:Starting to download");
									DownloadFilesTask downloadFile = new DownloadFilesTask();
									downloadFile.execute(remoteApkUrl,
											localApkName);
								}
							}).show();
		}
	};

	private void checkupdate() {
		if (alertUpdate != null && alertUpdate.isShowing()) { 
			// There is already an download message
			return;
		}
		Log.v(TAG, "Checking updates...");
		try {
			URL updateURL = new URL(versionUrl);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			final String s = new String(baf.toByteArray());

			/* Get current Version Number */
			String curVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			String newVersion = s;

			Log.d(TAG, "Current version is: " + curVersion
					+ " and new one is: " + newVersion);
			/* Is a higher version than the current already out? */
			if (!curVersion.equals(newVersion)) {
				/* Post a Handler for the UI to pick up and open the Dialog */
				if (alertUpdate == null || !alertUpdate.isShowing()) {
					if (alertError != null && alertError.isShowing())
						alertError.dismiss();
					mHandler.post(showUpdate);
				}
			} else
				Log.v(TAG, "The software is updated to the latest version: "
						+ newVersion);
		} catch (Exception e) {
			e.printStackTrace();
			// if(alertError==null || !alertError.isShowing())
			// mHandler.post(showError);
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, Integer> {
		private ProgressDialog mProgressDialog;
		private String outFileName;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(activity);
			mProgressDialog.setMessage(progressMessage);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... urls) {
			String inFileName = urls[0];
			outFileName = urls[1];
			try {
				// connecting to url
				URL u = new URL(inFileName);
				HttpURLConnection c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				// lenghtOfFile is used for calculating download progress
				int lenghtOfFile = c.getContentLength();

				// this is where the file will be seen after the download
				OutputStream out = context.openFileOutput(outFileName,
						Context.MODE_WORLD_READABLE); //
				// file input is from the url
				InputStream in = c.getInputStream();

				// here's the download code
				byte[] buffer = new byte[1024];
				int readLenght = 0;
				long total = 0;
				int lastProgress = 0;

				while ((readLenght = in.read(buffer)) > 0) {
					total += readLenght;
					int cProgress = (int) ((total * 100) / lenghtOfFile);
					if (cProgress != lastProgress) {
						publishProgress((int) ((total * 100) / lenghtOfFile));
						lastProgress = cProgress;
					}
					out.write(buffer, 0, readLenght);
				}
				out.flush();
				out.close();
				in.close();
				Log.d(TAG, "Saved file with name: " + outFileName + " | Size: "
						+ total);
			} catch (Exception e) {
				e.printStackTrace();
				return 1;
			}
			return 0;
		}

		@Override
		public void onProgressUpdate(Integer... args) {
			mProgressDialog.setProgress(args[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			mProgressDialog.dismiss();
			if (result == 0)
				InstallFile(outFileName);
			else
				mHandler.post(showError);
		}

	}

	private void InstallFile(String fileName) {
		Log.d(TAG, "Installing file  " + fileName);
		File file = new File(context.getFilesDir(), fileName);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		activity.startActivity(intent);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void disable() {
		enabled = false;
	}

	public void enable() {
		enabled = true;
	}

	public void setTagPrefix(String tagPrefix) {
		TAG = tagPrefix + " " + TAG_SUFFIX;
	}
}
