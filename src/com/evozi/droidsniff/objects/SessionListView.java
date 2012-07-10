/*    	SessionListView.java is the UI element for displaying the cookies
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class SessionListView extends ListView {

	public AuthListAdapter adapter = null;
	
	public SessionListView(Context context) {
		super(context);
		adapter = new AuthListAdapter(context);
		this.setAdapter(adapter);
		this.setLongClickable(false);
	}
	
	public SessionListView(Context c, AttributeSet attrset) {
		super(c, attrset);
		adapter = new AuthListAdapter(c);
		this.setAdapter(adapter);
		this.setLongClickable(false);
	}
	
	public void refresh() {
		adapter.notifyDataSetChanged(); 
		int index = getFirstVisiblePosition();
		View v = getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		this.setAdapter(adapter);
		setSelectionFromTop(index, top);
	}
	
}
