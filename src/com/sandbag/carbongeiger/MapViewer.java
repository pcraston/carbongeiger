package com.sandbag.carbongeiger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
	String currentLat;
	String currentLon;
	MyLocationOverlay myLocationOverlay;
	String nearestPolluter;
	Location closestInstallation;
	float distance = 42000000;
//	MediaPlayer mp = MediaPlayer.create(this, R.raw.geigerclick);
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	initMap();        
    	startGettingLocation();		
    }
    
    /** Called when the activity is first returned to front. */
    @Override
    public void onRestart()
    {
        super.onRestart();
    	startGettingLocation();		
    }
    	
    /** Called when the activity is stopped. */
    @Override
    protected void onPause() {
        super.onPause();
    	stopGettingLocation();
    }
    
    /** Called when the activity is stopped. */
    @Override
    protected void onStop() {
        super.onStop();
    	stopGettingLocation();
    }
    
    /** Called when the activity is destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    	stopGettingLocation();
    }
    
    public void initMap() {
        setContentView(R.layout.main);
    	mapView = (MapView) findViewById(R.id.mapview);
    	mapController = mapView.getController();
		mapController.setZoom(14);
		mapView.setBuiltInZoomControls(true);
    }
    
    public void startGettingLocation(){
		myLocationOverlay = new MyLocationOverlay(this, mapView);
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        
//        myLocationOverlay.onSensorChanged(Sensor.TYPE_ORIENTATION, values)
        
//    	SensorManager sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
//    	SensorListener sl = new SensorListener() {
//    		public void onSensorChanged(SensorEvent event) {
//    			
////    			if (currentLocation != null) {
////	    			GeomagneticField geofield = new GeomagneticField((float) currentLocation.getLatitude(),
////	    					(float) currentLocation.getLongitude(),
////	    					(float) currentLocation.getAltitude(),
////	    					currentLocation.getTime());
////	    			float declination = geofield.getDeclination();    			
////	    			Log.d("carbongeiger", "declination "+ declination);
////    			}
//    		}
//    	  
//    		public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    		}
//    	};
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                
        		Log.d("carbongeiger","my orientation: " + myLocationOverlay.getOrientation());
        		
            	if (currentLocation == null || location.distanceTo(currentLocation) > 100) {
                    mapController.animateTo(myLocationOverlay.getMyLocation());
	            	currentLocation = location;
	            	currentLat = Double.toString(location.getLatitude());
	            	currentLon = Double.toString(location.getLongitude());
	            	getMarkersForLocation();
            	}
            	if (currentLocation != null && closestInstallation != null) {
            		float bearing = currentLocation.bearingTo(closestInstallation);
            		if (bearing < 10 && bearing > 350) {
//            			mp.start();
            		}
            	}
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        sm.registerListener(sl, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),sm.SENSOR_DELAY_GAME);
    }
    
    public void stopGettingLocation(){
		myLocationOverlay = new MyLocationOverlay(this, mapView);
        myLocationOverlay.disableCompass();
        myLocationOverlay.disableMyLocation();
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	locationManager.removeUpdates(locationListener);
    }
    
    public void getMarkersForLocation() {
//		set current location
//    	mapController = mapView.getController();
//		mapController.setZoom(14);
//		GeoPoint current_position = new GeoPoint((int) (currentLocation.getLatitude() * 1E6), (int) (currentLocation.getLongitude() * 1E6));
//		mapController.animateTo(current_position);
//		Drawable sallyicon = this.getResources().getDrawable(R.drawable.sally);
//		InstallationMarkers current_position_marker = new InstallationMarkers(sallyicon);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		
//		add the current position marker
//		OverlayItem current_position_overlay = new OverlayItem(current_position, "Current position", "I'm here!");
//		current_position_marker.addOverlay(current_position_overlay);
//		mapOverlays.add(current_position_marker);

		String urltocall = "http://www.sandbag.org.uk/maps/installations_geiger/" + currentLat + '_' + currentLon + ".json";
		Log.d("carbongeiger: ", "Calling URL " + urltocall);
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
	 				Log.d("carbongeiger: ", "Current position: lat " + currentLat + " lon " + currentLon);
	 				Log.d("carbongeiger: ", nearestPolluter + " position: lat " + Double.toString(current.lat) + " lon " + Double.toString(current.lon));
	 				Log.d("carbongeiger: ", nearestPolluter + " distance: " + Double.toString(Math.floor(distance)));
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
	 		Log.d("carbongeiger Error: ", e.getMessage());
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