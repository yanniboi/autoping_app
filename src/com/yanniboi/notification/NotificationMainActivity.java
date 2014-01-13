package com.yanniboi.notification;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.ConnectivityManager;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.Button;
//import android.util.Log;

public class NotificationMainActivity extends Activity {
 
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 50; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000 * 60; // in Milliseconds
   
    protected LocationManager locationManager;
    protected Location activeLocation;
    protected ConnectivityManager connectivityManager;
    protected Button retrieveLocationButton;
    protected Button sendLocationButton;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);

    	retrieveLocationButton = (Button) findViewById(R.id.retrieve_location_button);
    	sendLocationButton = (Button) findViewById(R.id.send_location_button);
  
    	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
  
    	locationManager.requestLocationUpdates(
    			LocationManager.GPS_PROVIDER, 
    			MINIMUM_TIME_BETWEEN_UPDATES, 
    			MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
    			new MyLocationListener()
    	);
  
  
    	connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if ((connectivityManager.getActiveNetworkInfo() != null) && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()) {
			Toast.makeText(NotificationMainActivity.this, getString(R.string.update_online), Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(NotificationMainActivity.this, getString(R.string.update_offline), Toast.LENGTH_LONG).show();
		}
  
		retrieveLocationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showCurrentLocation();
			}
		});   

		sendLocationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new updateTask().execute();
			}
		});     
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_main, menu);
    	return true;
    }
 
    public void sendBigPictureStyleNotification(View view) {
    	PendingIntent pi = getPendingIntent();
    	Builder builder = new Notification.Builder(this);
    	builder.setContentTitle("BP notification")
    	// Notification title
    	.setContentText("BigPicutre notification")
    	// you can put subject line.
    	.setSmallIcon(R.drawable.ic_launcher)
    	// Set your notification icon here.
    	.addAction(R.drawable.ic_launcher, "show activity", pi)
    	.addAction(
			R.drawable.ic_launcher,
			"Share",
			PendingIntent.getActivity(getApplicationContext(), 0,
				getIntent(), 0, null
			)
    	);
 
    	// Now create the Big picture notification.
    	Notification notification = new Notification.BigPictureStyle(builder)
    	.bigPicture(
			BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher
			)
    	).build();
    	// Put the auto cancel notification flag
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	NotificationManager notificationManager = getNotificationManager();
    	notificationManager.notify(0, notification);
    }

    public PendingIntent getPendingIntent() {
    	return PendingIntent.getActivity(this, 0, new Intent(
			this, HandleNotificationActivity.class), 0
    	);
    }
 
    public NotificationManager getNotificationManager() {
    	return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
 
    public void showCurrentLocation() {
    	Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    	if (location != null) {
    		String message = String.format(
				"Current Location \n Longitude: %1$s \n Latitude: %2$s",
                 location.getLongitude(), location.getLatitude()
    	);
    		Toast.makeText(NotificationMainActivity.this, message,
    		Toast.LENGTH_LONG).show();
    	}
    }


 
    /**
     * Update task.
     */
    class updateTask extends AsyncTask<Context, Integer, String> {

    	protected String doInBackground(Context... params) {

    		int siteStatus = -1;

    		try {
    			siteStatus = sendCurrentLocation();
    		}
    		catch (IOException ignored) {}
             
    		if (siteStatus == 200) {
    			return "online";
    		}
    		else {
    			return "offline";
    		}
    	}
     
     
    	@Override
    	protected void onPostExecute(String sResponse) {
    		if (sResponse.equals("offline")) {
    			String message = "site response is not 200....";
    			Toast.makeText(NotificationMainActivity.this, message,
					Toast.LENGTH_LONG).show();
    		}
    		else if (sResponse.equals("online")) {
    			String message = "site response is 200! :)";
    			Toast.makeText(NotificationMainActivity.this, message,
    				Toast.LENGTH_LONG).show();
    		}
    		else {
    			String message = "unknown error";
    			Toast.makeText(NotificationMainActivity.this, message,
    				Toast.LENGTH_LONG).show();
    		}     
    	}
    }
     
    /**
     * Download the program from the internet and save it locally.
     */
    public int sendCurrentLocation() throws IOException {
    	int siteStatus = -1;

    	if (activeLocation == null) {
        	activeLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	}
    	//Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    	if (activeLocation != null) {
    		String lat = Double.toString(activeLocation.getLatitude());
    		String lng = Double.toString(activeLocation.getLongitude());
     
    		try {
    			String link = "http://six-gs.com/autoping/checkin?id=1234&lat=" + lat + "&lng=" + lng;

    			URL url = new URL(link);

    			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    			siteStatus = urlConnection.getResponseCode();
	     
    			urlConnection.disconnect();
    		}
    		catch (IOException ignored) {}
    	}
    	return siteStatus;
    }


    private class MyLocationListener implements LocationListener {

    	public void onLocationChanged(Location location) {
    		String message = String.format(
    			"New Location \n Longitude: %1$s \n Latitude: %2$s",
    			location.getLongitude(), location.getLatitude()
    		);
    		//Toast.makeText(NotificationMainActivity.this, message, Toast.LENGTH_LONG).show();
    		
    		// Make sure a location is set.
        	if (activeLocation == null) {
            	activeLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        	}
        	
        	// Check if new location is better.
        	if (isBetterLocation(location, activeLocation)) {
        		new updateTask().execute();
        		String distance = Float.toString(location.distanceTo(activeLocation));
            	activeLocation = location;
            	
        		Toast.makeText(NotificationMainActivity.this, message + " Distance: " + distance, Toast.LENGTH_LONG).show();
        	}

    	}

    	public void onStatusChanged(String s, int i, Bundle b) {
    		Toast.makeText(NotificationMainActivity.this, "Provider status changed",
                 Toast.LENGTH_LONG).show();
    	}

    	public void onProviderDisabled(String s) {
    		Toast.makeText(NotificationMainActivity.this,
                 "Provider disabled by the user. GPS turned off",
                 Toast.LENGTH_LONG).show();
    	}

    	public void onProviderEnabled(String s) {
    		Toast.makeText(NotificationMainActivity.this,
                 "Provider enabled by the user. GPS turned on",
                 Toast.LENGTH_LONG).show();
    	}
    }

    /**
     * Determines whether one location reading is better than the current location.
     * 
     * @param location
     *            The new Location that you want to evaluate
     * @param currentBestLocation
     *            The current Location fix, to which you want to compare the new one
     *            
     * @return indicates if you should use the new location
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MINIMUM_TIME_BETWEEN_UPDATES;
        boolean isSignificantlyOlder = timeDelta < - MINIMUM_TIME_BETWEEN_UPDATES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same locationProvider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Validates if the provider are equal.
     * 
     * @param provider1 - provider
     * @param provider2 - provider
     * 
     * @return <code>TRUE</code> if the provider are the same
     */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}