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
#import <AudioToolbox/AudioToolbox.h>
#import <CoreMotion/CoreMotion.h>

//load accelerometer delegate here

@interface carbongeigerViewController : UIViewController <MKMapViewDelegate, CLLocationManagerDelegate, UIAccelerometerDelegate> {
	CLLocationManager *locationManager;
	CMMotionManager *motionManager;
	CMAttitude *referenceAttitude;
	MKMapView *mapView;
	IBOutlet UILabel *nearestPolluterLabel;
//	IBOutlet UILabel *locationLabel;
//	IBOutlet UILabel *orientationLabel;	
	NSMutableData *responseData;
	CLLocation *currentLocation;
	CFURLRef soundFileURLRef;
	SystemSoundID soundFileObject;
	float nearestPolluterBearing;
	double nearestPolluterDistance;
}

@property (nonatomic, retain) IBOutlet MKMapView *mapView;
@property (retain, nonatomic) CLLocationManager *locationManager;
@property (readwrite) CFURLRef soundFileURLRef;
@property (readonly) SystemSoundID soundFileObject;

@end

