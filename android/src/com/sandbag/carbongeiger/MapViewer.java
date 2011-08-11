package com.sandbag.carbongeiger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

	private MapView mapView;
	private MapController mapController;
	private Location currentLocation;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private String currentLat;
	private String currentLon;
	private MyLocationOverlay myLocationOverlay;
	private String nearestPolluter;
	private Location closestInstallation;
	private float orientation = 0;
	private float difference = 0;
	private float distance = 42000000;
	private MediaPlayer mp;
	private Vibrator vibes;
	private boolean sound = true;
	private String current_year = "2010";
	
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
        vibes = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    /** Called when the activity is first returned to front. */
    @Override
    public void onResume()
    {
        super.onResume();
        showPopup();
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
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem info = menu.add(0,0,0,"Information");
		info.setIcon(android.R.drawable.ic_menu_info_details);
		
		MenuItem sound = menu.add(0,1,1,"Turn Sound Off");
		sound.setIcon(android.R.drawable.ic_lock_silent_mode);
	
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			showPopup();
		    return true;		
		case 1:
			if (sound == true) {
				sound = false;
				item.setIcon(android.R.drawable.ic_lock_silent_mode_off);
				item.setTitle("Turn Sound On");
			} else {
				sound = true;
				item.setIcon(android.R.drawable.ic_lock_silent_mode);
				item.setTitle("Turn Sound Off");				
			}
		    return true;		
		default:
			return super.onOptionsItemSelected(item);
    }

	}
    
    public void showPopup() {    	
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setTitle("Welcome to Sandbag's Carbon Geiger");
        TextView myView = new TextView(getApplicationContext());
        myView.setText("instructions here..\n\nQuestions, Comments?\ninfo@sandbag.org.uk");
        myView.setTextSize(15);
        myView.setPadding(15, 0, 15, 10);
        alertbox.setView(myView);
        alertbox.setIcon(R.drawable.sally);
        alertbox.setNeutralButton("Continue", null);
        alertbox.show();
    }
    
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
//        	Log.d("carbongeiger","sensorChanged (new Azimuth " + event.values[0] + " east of magnetic north)");
        	
	    	if (closestInstallation != null) {
//	    		this gets the orientation between current location and the closest installations in east of true north
				orientation = currentLocation.bearingTo(closestInstallation);
//				convert from -180 to 180 scale to 0 to 360 scale
				if (orientation < 0) {
					orientation = 360 + orientation;
				}
//				Log.d("carbongeiger","Nearest polluter location: " + closestInstallation.toString());
//				Log.d("carbongeiger","orientation to nearest polluter: " + orientation);
//				need to get declination (difference true north to magnetic north at current location) and add to orientation of phone
				GeomagneticField geoField = new GeomagneticField(
				         Double.valueOf(currentLocation.getLatitude()).floatValue(),
				         Double.valueOf(currentLocation.getLongitude()).floatValue(),
				         Double.valueOf(currentLocation.getAltitude()).floatValue(),
				         System.currentTimeMillis()
				      );
				float declination = geoField.getDeclination();
				difference = orientation - (event.values[0] + declination);
				difference = Math.abs(difference);
//				Log.d("carbongeiger","Difference between this and phone orientation: " + difference);
//				((TextView) findViewById(R.id.orientation)).setText("Orientation. Phone: " + Math.round(event.values[0]) + ", Me->Polluter: " + Math.round(orientation) + ", Phone->Polluter: " + Math.round(difference));
	    		if (difference < 40 && distance < 5000) {
//					((TextView) findViewById(R.id.orientation)).setText("Now pointing at nearest polluter");
	    			vibes.vibrate(25);
	    			if (sound == true) {
	    				mp.start();
	    			}
	    		}
			} else {
//				Log.d("carbongeiger","no orientation to nearest polluter!");
			}
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    
    public void startGettingLocation(){
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
        
        locationManager	= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
        		
            	if (currentLocation == null || location.distanceTo(currentLocation) > 1000) {
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
			int nearestPolluterId = 0;
	 		closestInstallation = new Location(LocationManager.GPS_PROVIDER);
	 		for(installations current : insts){
	 			
	 			float [] temp = new float [10];
	 			Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), current.lat, current.lon, temp); 			
	 			if(temp[0]<distance){
	 				distance = temp[0];
	 				nearestPolluter = current.name;
	 				nearestPolluterId = current.id;
	 		 		closestInstallation.setLatitude(current.lat);
	 		 		closestInstallation.setLongitude(current.lon);
//	 				Log.d("carbongeiger", "Current position: lat " + currentLat + " lon " + currentLon);
//	 				Log.d("carbongeiger", nearestPolluter + " position: lat " + Double.toString(current.lat) + " lon " + Double.toString(current.lon));
//	 				Log.d("carbongeiger", nearestPolluter + " distance: " + Double.toString(Math.floor(distance)));
	 			}
	 		}
	 		
	 		for(installations current : insts){
	 			GeoPoint point = new GeoPoint((int) (current.lat * 1E6), (int) (current.lon * 1E6));
	 			String company;
	 			if (current.parent_company.length() == 0 || current.parent_company.equals(current.company)) {
	 				company = current.company;
	 			} else {
	 				company = current.company + ", " + current.parent_company;
	 			}
	 			String snippet = company + "\n" + "Emissions "+current_year+": " + current.emissions_current_year + "\n" + "Allocations "+current_year+": " + current.alloc_current_year + "\nTonnes of C02";
		        OverlayItem overlayitem = new OverlayItem(point, current.name, snippet);
		        Drawable markericon;
		        if (nearestPolluterId > 0 && current.id == nearestPolluterId) {
		        	markericon = getMarkerIcon(current.power, current.overalloc, true);
		        } else {
		        	markericon = getMarkerIcon(current.power, current.overalloc, false);
		        }
		        installation_marker = new InstallationMarkers(markericon, this);
		        installation_marker.addOverlay(overlayitem);
		        mapOverlays.add(installation_marker);
	 		}
	        
	 		
			((TextView) findViewById(R.id.polluterdistance)).setText("Nearest Polluter: " + nearestPolluter + " is " + Math.round(distance) + "m away!");
	 	
	 	}
	 	catch(Exception e)
	 	{
	 		Log.d("carbongeiger", "ERROR:" + e.getMessage());
	 	}
    }
    
    public Drawable getMarkerIcon(boolean power, boolean overalloc, boolean nearest) {
    	if (power) {
    		if (overalloc) {
    			if (nearest) {
    				return this.getResources().getDrawable(R.drawable.icon_plant_red_closest);    				
    			} else {
    				return this.getResources().getDrawable(R.drawable.icon_plant_red);    
    			}
    		} else {
    			if (nearest) {
    				return this.getResources().getDrawable(R.drawable.icon_plant_purple_closest);      				
    			} else {
    				return this.getResources().getDrawable(R.drawable.icon_plant_purple);  
    			}
    		}
    	} else {
    		if (overalloc) {
    			if (nearest) {
        	    	return this.getResources().getDrawable(R.drawable.icon_factory_red_closest);
    			} else {
    				return this.getResources().getDrawable(R.drawable.icon_factory_red); 
    			}
    		} else {
    			if (nearest) {
        	    	return this.getResources().getDrawable(R.drawable.icon_factory_purple_closest);     				
    			} else {
    				return this.getResources().getDrawable(R.drawable.icon_factory_purple); 
    			}
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
	public String parent_company;
	public int emissions_current_year;
	public int alloc_current_year;
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