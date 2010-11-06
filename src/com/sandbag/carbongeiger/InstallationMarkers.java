package com.sandbag.carbongeiger;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class InstallationMarkers extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> installations = new ArrayList<OverlayItem>();
	
	public InstallationMarkers(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	public void addOverlay(OverlayItem overlay) {
		installations.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return installations.get(i);
	}
	
	@Override
	public int size() {
	  return installations.size();
	}

}
