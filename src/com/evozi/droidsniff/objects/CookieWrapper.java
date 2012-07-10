/*    	CookieWrapper.java wraps an Android cookie
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

package com.evozi.droidsniff.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.http.impl.cookie.BasicClientCookie;

public class CookieWrapper implements Serializable{
	private static final long serialVersionUID = 7338603313707503698L;
	
	org.apache.http.cookie.Cookie cookie = null;
	String url = null;
	
	public CookieWrapper(org.apache.http.cookie.Cookie cookie, String url) {
		this.cookie = cookie;
		this.url = url;
	}

	
	public org.apache.http.cookie.Cookie getCookie() {
		return cookie;
	}

	public String getUrl() {
		return url;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(cookie.getDomain());
		out.writeObject(cookie.getName());
		out.writeObject(cookie.getPath());
		out.writeObject(cookie.getValue());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		String domain = (String) in.readObject();
		String name   = (String) in.readObject();
		String path   = (String) in.readObject();
		String value  = (String) in.readObject();
		
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		cookie.setDomain(domain);
		cookie.setPath(path);
		cookie.setVersion(0);
		
		this.cookie = cookie;
	}

}
