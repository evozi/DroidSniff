package com.evozi.droidsniff.helper;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.evozi.droidsniff.auth.AuthHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import com.evozi.droidsniff.R;

public class DialogHelper {

	private static Activity context = null;

	public static void installBusyBox(Activity context) {
		DialogHelper.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.installbusybox).setCancelable(false)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent goToMarket = null;
						goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=stericson.busybox"));
						DialogHelper.context.startActivity(goToMarket);
						dialog.cancel();
					}
				}).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static void clearBlacklist(Activity context) {
		DialogHelper.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.clear_blacklist).setCancelable(false)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						DBHelper.clearBlacklist(DialogHelper.context);
						AuthHelper.clearBlacklist();
					}
				}).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	public static void downloadUpdate(Activity context) {
		try {
			String versionStr = getContentFromWeb("http://apps.evozi.com/android/droidsniff/version.php");
			int versionWeb = Integer.valueOf(versionStr);
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			int myVersion = info.versionCode;
			if (myVersion < versionWeb) {
				DialogHelper.context = context;
				String message = context.getString(R.string.updatetext);
				message += getContentFromWeb("http://apps.evozi.com/android/droidsniff/changelog.php");
	
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(message).setCancelable(false)
						.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}
		} catch (Exception e) {
			Log.e(Constants.APPLICATION_TAG, "Error while checking update: ", e);
		}
	}
	**/

	public static void showUnrooted(Activity context) {
		DialogHelper.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.unrooted).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	public static void showDisclaimer(final Activity context) {

	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	
	builder.setMessage(R.string.license);
	builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				context.finish();
			}
		});

	builder.setCancelable(false);
	builder.show();
	}

	/**
	private static String getContentFromWeb(String url) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet http = new HttpGet(url);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpclient.execute(http, responseHandler);
			return response;
		} catch (Exception e) {
			return "";
		}
	}
	**/

}
