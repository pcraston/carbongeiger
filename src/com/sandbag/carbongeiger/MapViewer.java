package com.sandbag.carbongeiger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
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
	String nearestPolluter;
	float distance = 42000000;
	MediaPlayer mp;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    	mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
    	startGettingLocation();		
        
    }
    
    /** Called when the activity is first returned to front. */
    @Override
    public void onRestart()
    {
        super.onRestart();

        setContentView(R.layout.main);
    	mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
    	startGettingLocation();		
        
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
    
    public void startGettingLocation(){
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
            	if (currentLocation == null || location.distanceTo(currentLocation) > 100) {
	            	currentLocation = location;
	            	currentLat = Double.toString(location.getLatitude());
	            	currentLon = Double.toString(location.getLongitude());
	            	getMarkersForLocation();
            	}
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
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	locationManager.removeUpdates(locationListener);
    }
    
    public void getMarkersForLocation() {
//		set current location
    	mapController = mapView.getController();
		mapController.setZoom(14);
		GeoPoint current_position = new GeoPoint((int) (currentLocation.getLatitude() * 1E6), (int) (currentLocation.getLongitude() * 1E6));
		mapController.animateTo(current_position);
		Drawable sallyicon = this.getResources().getDrawable(R.drawable.sally);
		InstallationMarkers current_position_marker = new InstallationMarkers(sallyicon);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		
//		add the current position marker
		OverlayItem current_position_overlay = new OverlayItem(current_position, "Current position", "I'm here!");
		current_position_marker.addOverlay(current_position_overlay);
		mapOverlays.add(current_position_marker);

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
			
	 		for(installations current : insts){
	 			
	 			float [] temp = new float [10];
	 			
	 			Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), current.lat, current.lon, temp);
	 			
	 			
	 			if(temp[0]<distance){
	 				distance = temp[0];
	 				nearestPolluter = current.name;
	 				Log.d("carbongeiger: ", "Current position: lat " + currentLat + " lon " + currentLon);
	 				Log.d("carbongeiger: ", nearestPolluter + " position: lat " + Double.toString(current.lat) + " lon " + Double.toString(current.lon));
	 				Log.d("carbongeiger: ", nearestPolluter + " distance: " + Double.toString(Math.floor(distance)));
	 			}
	 			
	 			GeoPoint point = new GeoPoint((int) (current.lat * 1E6), (int) (current.lon * 1E6));
		        OverlayItem overlayitem = new OverlayItem(point, nearestPolluter, "Look at my carbon!");
				
				Drawable markericon = getMarkerIcon(current.power, current.overalloc);
		        installation_marker = new InstallationMarkers(markericon);
		        
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