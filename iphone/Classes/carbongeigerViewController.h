//
//  iGeigerViewController.h
//  iGeiger
//
//  Created by Patrick Craston on 20/11/2010.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>

@interface carbongeigerViewController : UIViewController {
	MKMapView *mapView;
	IBOutlet UILabel *locationLabel;
	IBOutlet UILabel *orientationLabel;	
	NSMutableData *responseData;
}

@property (nonatomic, retain) IBOutlet MKMapView *mapView;

@end

