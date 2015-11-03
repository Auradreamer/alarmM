package com.example.wangmengyu.alarmm;

import android.widget.Toast;


public class HttpHelper {
	
	private static DatabaseHelper myDB;
	private static URL url = new URL("http://db.science.uoit.ca:9000");
	private static HttpURLConnection urlConnection;
	private static OutputStream writer;
	private static InputStream listener;
	
	public HttpHelper (DatabaseHelper db) {
		myDB = db;
	}
	
	public void createConnection() {
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			writer = new BufferedOutputStream(urlConnection.getOutputStream());
			listener = new BufferedInputStream(urlConnection.getInputStream());
		} catch (Exception e) {
			Toast.makeText(this, "Error establishing connection: "+e.toString(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void removeConnection() {
		urlConnection.disconnect();
	}
	
	/*
	 * Send a single scan ssid result to the server. 
	 * Will be used to send data directly to server instead
	 * of storing on local database. If send fails, then 
	 * it is inserted into the local database and returns false,
	 * otherwise it returns true.
	 */
	public boolean send(String t, String ssid, int strength) {
		
		String params = "ssid="+ssid+"&strength="+Integer.toString(strength)+"&stamp="+t;
		
		try {
			
			// send params to writer
			
			// read response from listener
			
			//if response contains a error
			
				//output error to toast

		finally {
			urlConnection.disconnect();
		}
   
		return true;
	}
}