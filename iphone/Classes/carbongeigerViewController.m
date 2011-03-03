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

@synthesize mapView, locationManager;

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
	//currently deactivated because always on SanFran in simulator
	mapView.showsUserLocation=NO;
	mapView.mapType = MKMapTypeStandard;
	[mapView setDelegate:self];
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
    [super dealloc];
	[locationManager release];
	[mapView release];
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
	
	double nearestPolluterDistance = 42000000;
	for (int i = 0; i < [installations count]; i++) {
		NSDictionary *installation = [installations objectAtIndex:i];
		NSString *installationLat = [installation objectForKey:@"lat"];
		NSString *installationLon = [installation objectForKey:@"lon"];
		CLLocation *installationLocation = [[CLLocation alloc] initWithLatitude:[installationLat doubleValue] longitude:[installationLon doubleValue]];
		double distance = [installationLocation distanceFromLocation:currentLocation];
		[installationLocation release];
		if (distance < nearestPolluterDistance) {
			nearestPolluterName = [installation objectForKey:@"name"];
			nearestPolluterDistance = distance;
		}
		InstallationAnnotation *installationMarker = [[InstallationAnnotation alloc] initWithDictionary:installation];
		[mapView addAnnotation:installationMarker];
		[installationMarker release];
		//NSLog(@"%@",installation);
	}
	
    NSString *nearestPolluterLabelText = [NSString stringWithFormat: @" %@ is %dm away!",nearestPolluterName,lroundf(nearestPolluterDistance)];
    nearestPolluterLabel.text = [nearestPolluterLabel.text stringByAppendingString:nearestPolluterLabelText];
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id )annotation
{
	MKAnnotationView *customAnnotationView = [[[MKAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:nil] autorelease];
	if ([annotation overalloc] == YES) {
		if ([annotation power] == YES) {
			UIImage *pinImage = [UIImage imageNamed:@"icon_plant_red.png"];
			[customAnnotationView setImage:pinImage];
			[pinImage release];
		} else {	
			UIImage *pinImage = [UIImage imageNamed:@"icon_factory_red.png"];
			[customAnnotationView setImage:pinImage];
			[pinImage release];
		}
	} else {			
		if ([annotation power] == YES) {
			UIImage *pinImage = [UIImage imageNamed:@"icon_plant_green.png"];
			[customAnnotationView setImage:pinImage];
			[pinImage release];
		} else {
			UIImage *pinImage = [UIImage imageNamed:@"icon_factory_green.png"];
			[customAnnotationView setImage:pinImage];
			[pinImage release];
		}
	}	
	customAnnotationView.canShowCallout = YES;
	return customAnnotationView;
}

@end
