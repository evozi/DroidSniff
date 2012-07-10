/*
 * AuthDefinition.java defnies one Authentication, read from auth.xml resource
 * Copyright (C) 2011 Andreas Koch <koch.trier@gmail.com>
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

package com.evozi.droidsniff.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.evozi.droidsniff.objects.CookieWrapper;


public class AuthDefinition {

	ArrayList<String> cookieNames;
	String url;
	String domain;
	String name;
	String mobileurl;
	String regexp = null;
	String idurl  = null;

	public AuthDefinition(ArrayList<String> cookieNames, String url, String mobileurl, String domain, String name, String idurl, String regexp) {
		this.cookieNames = cookieNames;
		this.url = url;
		this.domain = domain;
		this.name = name;
		this.mobileurl = mobileurl;
		this.idurl = idurl != null ? idurl : url;
		this.regexp = regexp;
	}

	public Auth getAuthFromCookieString(String cookieListString) {
		String[] lst = cookieListString.split("\\|\\|\\|");
		if (lst.length < 3)
			return null;
		cookieListString = lst[0];

		ArrayList<CookieWrapper> cookieList = new ArrayList<CookieWrapper>();
		String[] cookies = cookieListString.split(";");
		for (String cookieString : cookies) {
			String[] values = cookieString.split("=");
			if (cookieString.endsWith("=")) {
				values[values.length - 1] = values[values.length - 1] + "=";
			}
			values[0] = values[0].replaceAll("Cookie:", "");
			values[0] = values[0].replaceAll(" ", "");
			if (cookieNames.contains(values[0])) {
				String val = "";
				for (int i = 1; i < values.length; i++) {
					if (i > 1)
						val += "=";
					val += values[i];
				}
				BasicClientCookie cookie = new BasicClientCookie(values[0], val);
				cookie.setDomain(domain);
				cookie.setPath("/");
				cookie.setVersion(0);
				cookieList.add(new CookieWrapper(cookie, url));
			}
		}
		if (cookieList != null && !cookieList.isEmpty() && cookieList.size() == cookieNames.size()) {
			return new Auth(cookieList, url, mobileurl, getIdFromWebservice(cookieList), lst[2], this.name);
		}
		return null;
	}

	private String getIdFromWebservice(List<CookieWrapper> cookieList) {
		try {
			Pattern pattern = Pattern.compile(regexp);

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet http = new HttpGet(idurl);
			StringBuffer cookies = new StringBuffer();
			for (CookieWrapper cookie : cookieList) {
				cookies.append(cookie.getCookie().getName());
				cookies.append("=");
				cookies.append(cookie.getCookie().getValue());
				cookies.append("; ");
			}
			http.addHeader("Cookie", cookies.toString());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpclient.execute(http, responseHandler);

			Matcher matcher = pattern.matcher(response);
			boolean matchFound = matcher.find();
			if (matchFound) {
				String s = matcher.group(2);
				return s;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}
	
}
