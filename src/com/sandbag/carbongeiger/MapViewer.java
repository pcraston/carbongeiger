package com.sandbag.carbongeiger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
//import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MapViewer extends MapActivity {

	MapView mapView;
	MapController mapController;
	GeoPoint geoPoint;
	Location currentLocation;
    LocationManager locationManager;
	LocationListener locationListener;
	SensorManager mSensorManager;
	Sensor mSensor;
	String currentLat;
	String currentLon;
	MyLocationOverlay myLocationOverlay;
	String nearestPolluter;
	Location closestInstallation;
	float orientation = 0;
	float difference = 0;
	float distance;
	MediaPlayer mp;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		mp = MediaPlayer.create(this, R.raw.geigerclick);
    	initMap();        
//    	startGettingLocation();
    	mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }
    
    /** Called when the activity is first returned to front. */
    @Override
    public void onResume()
    {
        super.onResume();
    	startGettingLocation();	
    	mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }
    	
//    /** Called when the activity is stopped. */
//    @Override
//    protected void onPause() {
//        super.onPause();
//    	stopGettingLocation();
//    }
    
    /** Called when the activity is stopped. */
    @Override
    protected void onStop() {
        super.onStop();
    	stopGettingLocation();
    	mSensorManager.unregisterListener(mListener);
    }
    
//    /** Called when the activity is destroyed. */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    	stopGettingLocation();
//    }
    
    public void initMap() {
        setContentView(R.layout.main);
    	mapView = (MapView) findViewById(R.id.mapview);
    	mapController = mapView.getController();
		mapController.setZoom(14);
		mapView.setBuiltInZoomControls(true);
		myLocationOverlay = new MyLocationOverlay(this, mapView);
    }
    
    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        	Log.d("carbongeiger","sensorChanged (new Azimuth " + event.values[0] + " east of magnetic north)");
        	
	    	if (closestInstallation != null) {
				orientation = closestInstallation.bearingTo(currentLocation);
//				this seems to be right, need to figure out why bearing seems to be counter-clockwise east of north
				orientation = 360-orientation;
//				Log.d("carbongeiger","Nearest polluter location: " + closestInstallation.toString());
				Log.d("carbongeiger","orientation to nearest polluter: " + orientation);
				difference = orientation - event.values[0];
				difference = Math.abs(difference);
				Log.d("carbongeiger","Difference between this and phone orientation: " + difference);
	    		if (difference < 40) {
	    			Log.d("carbongeiger","BEEP!");
	    			mp.start();
	    		}
			} else {
				Log.d("carbongeiger","no orientation to nearest polluter!");
			}
	
//	    	if (currentLocation != null && closestInstallation != null) {
//	    		float bearing = currentLocation.bearingTo(closestInstallation);
//	    		if (bearing < 10 && bearing > 350) {
//	    			Log.d("carbongeiger","BEEP!");
//	//    			mp.start();
//	    		}
//	    	}
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    
//    public void startGettingOrientation() {
//    	sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    	

//        myLocationOverlay.onSensorChanged(Sensor.TYPE_ORIENTATION, values)
    		
//        		Log.d("carbongeiger","myLocationOverlay: my orientation relative to north: " + myLocationOverlay.getOrientation());
//        		Log.d("carbongeiger","location: my orientation relative to north: " + location.getBearing());
//

//    	  
//    		public void onAccuracyChanged(int something, int accuracy) {
//    		}
//    	};
//      sm.registerListener(sl, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),sm.SENSOR_DELAY_GAME);    
//    }
    
//    public void stopGettingOrientation() {	
////    	sensorManager.unregisterListener(sensorEventListener);
//    }
    
    public void startGettingLocation(){
//        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
        		
            	if (currentLocation == null || location.distanceTo(currentLocation) > 40000) {
            		if (currentLocation == null) {
//            			Log.d("carbongeiger","No currentLocation. Polling for first time...");
            		} else if (closestInstallation == null ) {
//            			Log.d("carbongeiger","No closestInstallation. Polling sandbag to get new markers...");            			
            		} else {
//            			Log.d("carbongeiger","Change in location is " + location.distanceTo(currentLocation) + ". Polling sandbag to get new markers...");
            		}
                    mapController.animateTo(myLocationOverlay.getMyLocation());
	            	currentLocation = location;
	            	currentLat = Double.toString(location.getLatitude());
	            	currentLon = Double.toString(location.getLongitude());
	            	getMarkersForLocation();
            	} else {
//        			Log.d("carbongeiger","Change in location is " + location.distanceTo(currentLocation) + ". No need to poll right now...");            		
            	}
            	
//            	Log.d("carbongeiger","location object says: " + location.toString());
//            	Log.d("carbongeiger","MyLocationOverlay object says: " + myLocationOverlay.getMyLocation());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    public void stopGettingLocation(){
//        myLocationOverlay.disableCompass();
        myLocationOverlay.disableMyLocation();
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	locationManager.removeUpdates(locationListener);
    }
    
    public void getMarkersForLocation() {
    	distance = 42000000;
		List<Overlay> mapOverlays = mapView.getOverlays();

		String urltocall = "http://www.sandbag.org.uk/maps/installations_geiger/" + currentLat + '_' + currentLon + ".json";
//		Log.d("carbongeiger", "Calling URL " + urltocall);
	    MapDataService webService = new MapDataService(urltocall);
	    //Pass the parameters if needed , if not then pass dummy one as follows
	 	Map<String, String> params = new HashMap<String, String>();
	 	params.put("var", "");
	 	try
	 	{
		 	String response = webService.webGet("", params);
	 		//Parse Response into our object
	 		List<installations> insts = new Gson().fromJson(response, new TypeToken<List<installations>>(){}.getType());
			InstallationMarkers installation_marker;

	 		closestInstallation = new Location(LocationManager.GPS_PROVIDER);
	 		for(installations current : insts){
	 			
	 			float [] temp = new float [10];
	 			
	 			Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), current.lat, current.lon, temp);
	 			
	 			
	 			if(temp[0]<distance){
	 				distance = temp[0];
	 				nearestPolluter = current.name;
	 		 		closestInstallation.setLatitude(current.lat);
	 		 		closestInstallation.setLongitude(current.lon);
//	 				Log.d("carbongeiger", "Current position: lat " + currentLat + " lon " + currentLon);
//	 				Log.d("carbongeiger", nearestPolluter + " position: lat " + Double.toString(current.lat) + " lon " + Double.toString(current.lon));
//	 				Log.d("carbongeiger", nearestPolluter + " distance: " + Double.toString(Math.floor(distance)));
	 			}
	 			
	 			GeoPoint point = new GeoPoint((int) (current.lat * 1E6), (int) (current.lon * 1E6));
	 			String snippet = "Emissions 2009: " + current.emissions2009 + "\n" + "Allocations 2009: " + current.alloc2009 + "\nTonnes of C02";
		        OverlayItem overlayitem = new OverlayItem(point, current.company + ": " + current.name, snippet);
				
				Drawable markericon = getMarkerIcon(current.power, current.overalloc);
		        installation_marker = new InstallationMarkers(markericon, this);
		        
		        installation_marker.addOverlay(overlayitem);
		        
		        mapOverlays.add(installation_marker);
	 		}
	 		
			((TextView) findViewById(R.id.polluterdistance)).setText("Nearest Polluter: " + nearestPolluter + " is " + Double.toString(Math.floor(distance)) + "m away!");
	 	
	 	}
	 	catch(Exception e)
	 	{
	 		Log.d("carbongeiger", "ERROR:" + e.getMessage());
	 	}
    }
    
    public Drawable getMarkerIcon(boolean power, boolean overalloc) {
    	if (power) {
    		if (overalloc) {
    	    	return this.getResources().getDrawable(R.drawable.icon_plant_red);    			
    		} else {
    	    	return this.getResources().getDrawable(R.drawable.icon_plant_green);  
    		}
    	} else {
    		if (overalloc) {
    	    	return this.getResources().getDrawable(R.drawable.icon_factory_red); 
    		} else {
    	    	return this.getResources().getDrawable(R.drawable.icon_factory_green); 
    		}
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}

class installations {
	public String name;
	public String company;
	public int emissions2009;
	public int alloc2009;
	public int id;
    public double lat;
    public double lon;
    public boolean power;
    public boolean overalloc;

    @Override
    public String toString()
    {
            return "lat: "+lat+ " lon: "+lon;

    }

    public GeoPoint createPoint() {
    GeoPoint point = new GeoPoint((int) (lat * 1E6),(int) (lon * 1E6));
    return point;
    }
}