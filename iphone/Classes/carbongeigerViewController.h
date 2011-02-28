//
//  iGeigerViewController.h
//  iGeiger
//
//  Created by Patrick Craston on 20/11/2010.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>

@interface carbongeigerViewController : UIViewController <MKMapViewDelegate, CLLocationManagerDelegate> {
	CLLocationManager *locationManager;
	MKMapView *mapView;
	IBOutlet UILabel *locationLabel;
	IBOutlet UILabel *orientationLabel;	
	NSMutableData *responseData;
    NSMutableArray *mapAnnotations;
}

@property (nonatomic, retain) IBOutlet MKMapView *mapView;
@property (retain, nonatomic) CLLocationManager *locationManager;

@property (nonatomic, retain) NSMutableArray *mapAnnotations;

@end

