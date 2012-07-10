/*    	Auth.java is a wrapper for a requires cookie list of one Authentication
    	Copyright (C) 2011 Andreas Koch <koch.trier@gmail.com>
    	
    	This software was supported by the University of Trier 

	    This program is free software; you can redistribute it and/or modify
	    it under the terms of the GNU General Public License as published by
	    the Free Software Foundation; either version 3 of the License, or
	    (at your option) any later version.
	
	    This program is distributed in the hope that it will be useful,
	    but WITHOUT ANY WARRANTY; without even the implied warranty of
	    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	    GNU General Public License for more details.
	
	    You should have received a copy of the GNU General Public License along
	    with this program; if not, write to the Free Software Foundation, Inc.,
	    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. */

package com.evozi.droidsniff.auth;

import java.io.Serializable;
import java.util.ArrayList;

import com.evozi.droidsniff.objects.CookieWrapper;

public class Auth implements Serializable {
	private static final long serialVersionUID = 7124255590593980755L;
	
	
	ArrayList <CookieWrapper> cookieList = null;
	String url = null;
	String mobileurl = null;
	int id = 0; // Id contains a hash sum of all cookies in the object. 
	boolean generic = true;
	boolean saved = false;
	String name = null;
	String authName = null;
	String ip = null;
	
	public Auth(ArrayList<CookieWrapper> cookieList, String url, String mobileUrl, String name, String ip, String authName) {
		this.cookieList = cookieList;
		this.mobileurl = mobileUrl;
		this.authName = authName;
		this.generic = authName.equalsIgnoreCase("generic");
		this.url = url;
		this.ip = ip;
		this.name = (name == null || name.equals(""))?url:name+" [" + url + "]";
		for (CookieWrapper c : cookieList) {
			id += c.getCookie().getValue().hashCode();
		}
	}
	
	// Two authentications are supposed to be identical, in case their hashes are the same.
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Auth)) return false;
		Auth a = (Auth) o;
		return (a.getId() == this.id);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	

	public int getId() {
		return id;
	}

	public ArrayList<CookieWrapper> getCookies() {
		return cookieList;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
	
	public String getIp() {
		return ip;
	}

	public String getMobileUrl() {
		return mobileurl;
	}
	
	
	public boolean isGeneric() {
		return generic;
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	public void setSaved(boolean saved) {
		this.saved = saved;
	}

}
