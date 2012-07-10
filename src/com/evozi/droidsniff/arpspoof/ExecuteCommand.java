/*
 * The arpspoof package containts Software, initially developed by Robboe
 * Clemons, It has been used, changed and published in DroidSheep by Andreas
 * Koch according the GNU GPLv3
 */

/*
 * ExecuteCommand.java uses java's exec to execute commands as root Copyright
 * (C) 2011 Robbie Clemons <robclemons@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
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

package com.evozi.droidsniff.arpspoof;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.evozi.droidsniff.activities.ListenActivity;
import com.evozi.droidsniff.auth.AuthHelper;
import com.evozi.droidsniff.helper.Constants;

import android.util.Log;

public class ExecuteCommand extends Thread {
	private static final String TAG = "ExecuteCommand";
	private final String command;
	private Process process = null;
	private BufferedReader reader = null;
	private BufferedReader errorReader = null;
	private DataOutputStream os = null;

	private boolean listen = false;

	public ExecuteCommand(String cmd, boolean listen) throws IOException {
		command = cmd;
		process = Runtime.getRuntime().exec("su");
		reader = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		errorReader = new BufferedReader(new InputStreamReader(
				process.getErrorStream()));
		os = new DataOutputStream(process.getOutputStream());
		this.listen = listen;
	}

	public void run() {

		class StreamGobbler extends Thread {
			/*
			 * "gobblers" seem to be the recommended way to ensure the streams
			 * don't cause issues
			 */

			public BufferedReader buffReader = null;

			public StreamGobbler(BufferedReader br) {
				buffReader = br;
			}

			public void run() {
				try {
					while (true) {
						String line = "";
						if (buffReader.ready()) {
							line = buffReader.readLine();
							if (line == null)
								continue;
						} else {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
							continue;
						}
						if (Constants.DEBUG) {
							Log.d(Constants.APPLICATION_TAG, line);
						}
						if (ListenActivity.debugging) {
							ListenActivity.debugBuffer.append("command: "
									+ command + "line: " + line + "\n");
						}
						if (listen) {
							AuthHelper.process(line);
						}
					}
				} catch (IOException e) {
					Log.w(TAG,
							"StreamGobbler couldn't read stream or stream closed");
					if (Constants.DEBUG) {
						Log.w(TAG,
								"StreamGobbler couldn't read stream or stream closed",
								e);
					}
				} finally {
					try {
						buffReader.close();
						if (buffReader != null) {
							buffReader.close();
							buffReader = null;
						}
					} catch (IOException e) {
						// swallow error
					}
				}
			}
		}

		try {
			if (Constants.DEBUG) {
				Log.d(Constants.APPLICATION_TAG, "COMMAND: " + command);
			}
			if (ListenActivity.debugging) {
				ListenActivity.debugBuffer.append("executing command: "
						+ command + "\n");
			}
			os.writeBytes(command + '\n');
			os.flush();
			StreamGobbler errorGobler = new StreamGobbler(errorReader);
			StreamGobbler stdOutGobbler = new StreamGobbler(reader);
			errorGobler.setDaemon(true);
			stdOutGobbler.setDaemon(true);
			errorGobler.start();
			stdOutGobbler.start();
			os.writeBytes("exit\n");
			os.flush();
			// The following catastrophe of code seems to be the best way to
			// ensure this thread can be interrupted
			while (!Thread.currentThread().isInterrupted()) {
				try {
					process.exitValue();
					Thread.currentThread().interrupt();
				} catch (IllegalThreadStateException e) {
					// the process hasn't terminated yet so sleep some, then
					// check again
					Thread.sleep(250);// .25 seconds seems reasonable
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "error running commands", e);
			if (ListenActivity.debugging) {
				ListenActivity.debugBuffer.append(e);
			}
		} catch (InterruptedException e) {
			try {
				/**
				 * os.close();//key to killing executable and process
				 * reader.close(); errorReader.close();
				 **/
				if (os != null) {
					os.close();// key to killing executable and process
					os = null;
				}
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (errorReader != null) {
					errorReader.close();
					errorReader = null;
				}
			} catch (IOException ex) {
				// swallow error
			} finally {
				if (process != null) {
					process.destroy();
					process = null;
				}
			}
		} finally {
			if (process != null) {
				process.destroy();
				process = null;
			}
		}
	}

}