/*
 * AboutActivity.java is the about screen for DroidSniff 
 * Copyright (C) 2012 Evozi <email@evozi.com>
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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.evozi.droidsniff.R;

public class AboutActivity extends SherlockActivity implements
		ActionBar.TabListener {

	private TextView mSelected;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		mSelected = (TextView) findViewById(R.id.text);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab tab0 = getSupportActionBar().newTab();
		tab0.setText("About");
		tab0.setTabListener(this);
		getSupportActionBar().addTab(tab0);

		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText("FAQ");
		tab1.setTabListener(this);
		getSupportActionBar().addTab(tab1);

		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText("Guide");
		tab2.setTabListener(this);
		getSupportActionBar().addTab(tab2);
	}

	public void onTabReselected(Tab tab, FragmentTransaction transaction) {
	}

	public void onTabSelected(Tab tab, FragmentTransaction transaction) {

		if (tab.getText() == "About") {
			mSelected
					.setText("Version 1.0.0 Build 16\n\n\nPlease note:\n\nDroidSniff was developed as a tool for testing the security of your accounts.\n\nThis software is neither made for using it in public networks, nor for hijacking any other persons account.\n\nIt should only demonstrate the poor security properties network connections without encryption have.\nSo do not get DroidSniff to harm anybody or use it in order to gain unauthorized access to any account you do not own! Use this software only for analyzing your own security!");
		} else {
			mSelected.setText("Coming Soon");
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
		case android.R.id.home:
			this.finish();
			return true;
		}
		return false;
	}

}
