/*
 * AuthListAdapter.java shows the captured authentications within a list
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

package com.evozi.droidsniff.objects;

import jcifs.UniAddress;

import com.evozi.droidsniff.activities.ListenActivity;
import com.evozi.droidsniff.auth.Auth;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evozi.droidsniff.R;

public class AuthListAdapter extends BaseAdapter {

	private Context context;
	private String hostname;

	public AuthListAdapter(Context context) {
		this.context = context;
	}

	public int getCount() {
		return ListenActivity.authList.size();
	}

	@Override
	public Auth getItem(int position) {
		return ListenActivity.authList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		//TODO Use ConvertView! Reuse the view! 
		// It should looks something like this http://pastebin.com/fZNZRUPi <- But this code 
		// have problem when you scroll down and up again the list position will changed.
		LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.listelement, parent, false);

		if (position >= ListenActivity.authList.size())
			return itemLayout;

		if (ListenActivity.authList == null
				|| ListenActivity.authList.get(position) == null) {
			return itemLayout;
		}
		Auth auth = ListenActivity.authList.get(position);

		TextView tv1 = (TextView) itemLayout.findViewById(R.id.listtext1);
		TextView tv2 = (TextView) itemLayout.findViewById(R.id.listtext2);
		TextView tv3 = (TextView) itemLayout.findViewById(R.id.listtext3);
		ImageView imgView = (ImageView) itemLayout.findViewById(R.id.image);

		tv1.setText(auth.getName());

		if (auth.isGeneric()) {
			tv1.setTextColor(Color.argb(255, 0, 153, 204));
		} else {
			tv1.setTextColor(Color.argb(255, 255, 136, 0));
		}

		new ResolveHostName().execute(auth.getIp());
		 
		if (auth.isGeneric() || auth.getName() == null || auth.getName().equals("")) {
			tv2.setText(auth.getIp().replaceAll("IP=", "IP: ") + (auth.isSaved() ? "" : " Hostname: " + hostname));
			tv3.setText("ID: " + auth.getId());
			//tv3.setText("ID: " + auth.getId() + (auth.isSaved() ? " [SAVED] " : ""));
		} else {
			tv2.setText(auth.getIp().replaceAll("IP=", "IP: ") + (auth.isSaved() ? "" : " Hostname: " + hostname));
			tv3.setText(auth.getName() + "@" + auth.getUrl());
		}

		if (auth.isSaved()) {
			itemLayout.setBackgroundColor(Color.argb(150, 193, 205, 205));
			tv2.setTextColor(Color.WHITE);
			tv3.setTextColor(Color.WHITE);
		}
		
		

		// This needs some code cleaning , looks very untidy :O
		Resources r = context.getResources();
		Drawable[] layers = new Drawable[2];

		if (auth.getUrl().contains("amazon")) {
			// imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.amazon));
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.amazon);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.amazon));
			}
		} else if (auth.getUrl().contains("ebay")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.ebay);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.ebay));
			}
		} else if (auth.getUrl().contains("facebook")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.facebook);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.facebook));
			}
		} else if (auth.getUrl().contains("flickr")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.flickr);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.flickr));
			}
		} else if (auth.getUrl().contains("google")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.google);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.google));
			}
		} else if (auth.getUrl().contains("linkedin")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.linkedin);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.linkedin));
			}
		} else if (auth.getUrl().contains("twitter")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.twitter);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.twitter));
			}
		} else if (auth.getUrl().contains("youtube")) {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.youtube);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.youtube));
			}
		} else {
			if (auth.isSaved()) {
				layers[0] = r.getDrawable(R.drawable.icon);
				layers[1] = r.getDrawable(R.drawable.saved);
				LayerDrawable layerDrawable = new LayerDrawable(layers);
				imgView.setImageDrawable(layerDrawable);
			} else {
				imgView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon));
			}
		}
		return itemLayout;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	 private class ResolveHostName extends AsyncTask<String, Void, Void> {

		 
	     protected Void doInBackground(String... params) {
	    	try {
	    	 	    params[0] = params[0].replaceAll("IP=", "");
	    	 	    params[0] = UniAddress.getByName(params[0]).getHostName();
	    	 	    hostname = params[0];

	    		} catch (Exception e) {

	    	}
	         return null;
	     }
	     
	     /**
	     protected void onProgressUpdate(String... progress) {
		        //do stuff
		 }
		 **/
	     
		 protected void onPostExecute(Void unused) {
	     
			Log.d("DroidSheep", "Hostname : "+ hostname);

	     }
	 }
}
