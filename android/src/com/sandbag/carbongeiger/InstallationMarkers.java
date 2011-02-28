package com.sandbag.carbongeiger;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class InstallationMarkers extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> installations = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public InstallationMarkers(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
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
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = installations.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
}

