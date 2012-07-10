package com.evozi.droidsniff.helper;

import com.evozi.droidsniff.auth.Auth;
import com.evozi.droidsniff.objects.CookieWrapper;

import android.content.Context;
import android.content.Intent;

public class MailHelper {
	
	public static void sendAuthByMail(Context c, Auth a) {
		StringBuffer sb = new StringBuffer();
		for (CookieWrapper cw : a.getCookies()) {
			sb.append("[Cookie: \n");
			sb.append("domain: " + cw.getCookie().getDomain() + "\n");
			sb.append("path: " + cw.getCookie().getPath() + "\n");
			sb.append(cw.getCookie().getName());
			sb.append("=");
			sb.append(cw.getCookie().getValue());
			sb.append(";]\n");
		}
				
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent .setType("plain/text");
	    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "DroidSniff Cookie export");
	    emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
	    c.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

	public static void sendStringByMail(Context c, String string) {
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent .setType("plain/text");
	    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "DROIDSNIFF DEBUG INFORMATION");
	    emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, string);
	    c.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

}
