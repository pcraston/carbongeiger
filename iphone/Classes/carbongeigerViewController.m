//
//  iGeigerViewController.m
//  iGeiger
//
//  Created by Patrick Craston on 20/11/2010.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "carbongeigerViewController.h"
#import "JSON.h"
#import "InstallationAnnotation.h"

@implementation carbongeigerViewController

@synthesize mapView, locationManager, soundFileURLRef, soundFileObject;

BOOL firstpoll = TRUE;

/*
// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	self.locationManager = [[CLLocationManager alloc] init]; 
	locationManager.delegate = self; 
	locationManager.desiredAccuracy = kCLLocationAccuracyBest; 
	[locationManager startUpdatingLocation];
	mapView.showsUserLocation = YES;
	mapView.mapType = MKMapTypeStandard;
	[mapView setDelegate:self];
	
	UIAccelerometer *accel = [UIAccelerometer sharedAccelerometer];
	accel.delegate = self;
	accel.updateInterval = 1.0f/60.0f;
	
	NSURL *geigerClick = [[NSBundle mainBundle] URLForResource: @"geigerclick" withExtension: @"wav"];
	self.soundFileURLRef = (CFURLRef) [geigerClick retain];
	AudioServicesCreateSystemSoundID(soundFileURLRef, &soundFileObject);
	
	motionManager = [[CMMotionManager alloc] init];
	if (!motionManager.isDeviceMotionAvailable) {
		//n/a for older than iphone 4, need to use magnetometer in that case!
	}
	motionManager.deviceMotionUpdateInterval = 0.01;
	[motionManager startDeviceMotionUpdates];
	
	CMDeviceMotion *deviceMotion = motionManager.deviceMotion;
	//save the reference frame
	CMAttitude *attitude = deviceMotion.attitude;
	referenceAttitude = nil;
	referenceAttitude = [attitude retain];
}


// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
	self.locationManager = nil;
    self.mapView = nil;
	[super viewDidUnload];
}

- (void)dealloc {
	[locationManager release];
	[motionManager stopDeviceMotionUpdates];
	[motionManager release];
	[mapView release];
	AudioServicesDisposeSystemSoundID(soundFileObject);
	CFRelease(soundFileURLRef);
    [super dealloc];
}

//use accelerometer delegate to listen for changes in motion and update orientation and beep if required
- (void) accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration *)acceleration {
	// check that significant movement
	if ((fabsf(acceleration.x) > 1 || fabsf(acceleration.y) > 1 || fabsf(acceleration.z) > 1) && nearestPolluterBearing && !firstpoll)  {
		//NSLog(@"x %f, x %f, x %f", acceleration.x, acceleration.y, acceleration.z);
		//get orientation/attitude of phone from DeviceMotion
		CMAttitude *attitude = motionManager.deviceMotion.attitude;
		if (referenceAttitude != nil) [attitude multiplyByInverseOfAttitude: referenceAttitude];
		float yaw = attitude.yaw * (180.0/M_PI);
		float phonePolluterOrientation = nearestPolluterBearing - yaw;
		phonePolluterOrientation = fabs(phonePolluterOrientation);
        NSString *orientation = [NSString stringWithFormat: @"Phone-Polluter: %d",lroundf(phonePolluterOrientation)];
        orientationLabel.text = orientation;
		if ((phonePolluterOrientation < 40.0 || phonePolluterOrientation > 320.0) && nearestPolluterDistance < 5000.0) {
			AudioServicesPlaySystemSound(soundFileObject);
			AudioServicesPlaySystemSound (kSystemSoundID_Vibrate);
		}
	}
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    if (firstpoll == TRUE || [newLocation distanceFromLocation:oldLocation] > 1000) {		
		firstpoll = FALSE;
		//zoom to current location
		MKCoordinateRegion zoomRegion;
		zoomRegion.center.latitude = newLocation.coordinate.latitude;
		zoomRegion.center.longitude = newLocation.coordinate.longitude;
		zoomRegion.span.latitudeDelta = 0.05;
		zoomRegion.span.longitudeDelta = 0.05;	
		[self.mapView setRegion:zoomRegion animated:YES];

		currentLocation = [[CLLocation alloc] initWithLatitude:newLocation.coordinate.latitude longitude:newLocation.coordinate.longitude];

		//poll sandbag server to get json file with nearby installations
		NSString *latlon = [NSString stringWithFormat: @"Current Location: %f, %f",currentLocation.coordinate.latitude,currentLocation.coordinate.longitude];
		locationLabel.text = latlon;
		responseData = [[NSMutableData data] retain];
		NSString *urltoload = [NSString stringWithFormat: @"http://www.sandbag.org.uk/maps/installations_geiger/%f_%f.json",currentLocation.coordinate.latitude,currentLocation.coordinate.longitude];
		NSLog(@"Calling %@",urltoload);
		NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:urltoload]];
		[[NSURLConnection alloc] initWithRequest:request delegate:self];
	}
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
	[responseData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
	[responseData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
	NSLog(@"Connection failed: %@", [error description]);
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
	[connection release];
	NSString *responseString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
	[responseData release];
	NSArray *installations = [responseString JSONValue];
	[responseString release];
	
	NSString *nearestPolluterName;
	int nearestPolluterID;
	
	nearestPolluterDistance = 42000000;
	float nearestPolluterLat;
	float nearestPolluterLon;
	for (int i = 0; i < [installations count]; i++) {
		NSDictionary *installation = [installations objectAtIndex:i];
		NSString *installationLat = [installation objectForKey:@"lat"];
		NSString *installationLon = [installation objectForKey:@"lon"];
		CLLocation *installationLocation = [[CLLocation alloc] initWithLatitude:[installationLat doubleValue] longitude:[installationLon doubleValue]];
		double distance = [installationLocation distanceFromLocation:currentLocation];
		[installationLocation release];
		if (distance < nearestPolluterDistance) {
			nearestPolluterName = [installation objectForKey:@"name"];
			nearestPolluterID = [[installation objectForKey:@"id"] doubleValue];
			nearestPolluterDistance = distance;			
			nearestPolluterLat = [[installation objectForKey:@"lat"] doubleValue];
			nearestPolluterLon = [[installation objectForKey:@"lon"] doubleValue];
		}		
	}
	//convert to radians
	nearestPolluterLat = nearestPolluterLat * (M_PI/180.0);
	nearestPolluterLon = nearestPolluterLon * (M_PI/180.0);
	//get orientation between current location and nearest polluter
	float currentLat = (currentLocation.coordinate.latitude / 180.0) * M_PI;
	float currentLon = (currentLocation.coordinate.longitude / 180.0) * M_PI;
	nearestPolluterBearing = atan2(sin(nearestPolluterLon-currentLon)*cos(nearestPolluterLat), 
									cos(currentLat)*sin(nearestPolluterLat)-sin(currentLat)
									*cos(nearestPolluterLat)*cos(nearestPolluterLon-currentLon));
	//convert radians back to degrees
	nearestPolluterBearing = nearestPolluterBearing * (180.0/M_PI);
	//convert to 360 east of north
	if (nearestPolluterBearing < 0) {
		nearestPolluterBearing = 360 + nearestPolluterBearing;
	}
	
	for (int i = 0; i < [installations count]; i++) {
		NSDictionary *installation = [installations objectAtIndex:i];
		InstallationAnnotation *installationMarker = [[InstallationAnnotation alloc] initWithDictionary:installation];
		if ([[installation objectForKey:@"id"] doubleValue] == nearestPolluterID) {
			installationMarker.nearest = YES;			
		}
		[mapView addAnnotation:installationMarker];
		[installationMarker release];
		//NSLog(@"%@",installation);
	}
	
    NSString *nearestPolluterLabelText = [NSString stringWithFormat: @" %@ is %dm away!",nearestPolluterName,lroundf(nearestPolluterDistance)];
    nearestPolluterLabel.text = [nearestPolluterLabel.text stringByAppendingString:nearestPolluterLabelText];
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id )annotation
{
    if ([annotation isKindOfClass:[MKUserLocation class]])
        return nil;

	MKAnnotationView *customAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:nil] autorelease];
	UIImage *pinImage;
	if ([annotation overalloc] == YES) {
		if ([annotation power] == YES) {
			if ([annotation nearest] == YES) {
				pinImage = [UIImage imageNamed:@"icon_plant_red_closest.png"];				
			} else {
				pinImage = [UIImage imageNamed:@"icon_plant_red.png"];
			}
		} else {	
			if ([annotation nearest] == YES) {
				pinImage = [UIImage imageNamed:@"icon_factory_red_closest.png"];				
			} else {
				pinImage = [UIImage imageNamed:@"icon_factory_red.png"];
			}
		}
	} else {			
		if ([annotation power] == YES) {
			if ([annotation nearest] == YES) {
				pinImage = [UIImage imageNamed:@"icon_plant_purple_closest.png"];				
			} else {
				pinImage = [UIImage imageNamed:@"icon_plant_purple.png"];
			}
		} else {
			if ([annotation nearest] == YES) {
				pinImage = [UIImage imageNamed:@"icon_factory_purple_closest.png"];				
			} else {
				pinImage = [UIImage imageNamed:@"icon_factory_purple.png"];
			}
		}
	}	
	[customAnnotationView setImage:pinImage];
	//do we need to release this? currently crashes if uncomment
    //[pinImage release];
	customAnnotationView.canShowCallout = YES;
	return customAnnotationView;
}

@end
