//
//  iGeigerViewController.m
//  iGeiger
//
//  Created by Patrick Craston on 20/11/2010.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "carbongeigerViewController.h"
#import "JSON.h"
#import "InstallationAnnotation.h">

@implementation carbongeigerViewController

@synthesize mapView, locationManager, mapAnnotations;

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
	mapView.showsUserLocation=YES;
	
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
    self.mapAnnotations = nil;
    self.mapView = nil;
	[super viewDidUnload];
}


- (void)dealloc {
	[locationManager release];
	[mapView release];
    [mapAnnotations release];
    [super dealloc];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
	NSString *latlon = [NSString stringWithFormat: @"Current Location: %f, %f",newLocation.coordinate.latitude,newLocation.coordinate.longitude];
	locationLabel.text = latlon;
	responseData = [[NSMutableData data] retain];
	NSString *urltoload = [NSString stringWithFormat: @"http://www.sandbag.org.uk/maps/installations_geiger/%f_%f.json",newLocation.coordinate.latitude,newLocation.coordinate.longitude];
	NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:urltoload]];
	[[NSURLConnection alloc] initWithRequest:request delegate:self];
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
	[responseData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
	[responseData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
	//locationLabel.text = [NSString stringWithFormat:@"Connection failed: %@", [error description]];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
	[connection release];
	NSString *responseString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
	[responseData release];
	NSDictionary *installations = [responseString JSONValue];
	
	self.mapAnnotations = [[NSMutableArray alloc] initWithCapacity:[installations count]];
	
	for (int i = 0; i < [installations count]; i++) {
		InstallationAnnotation *installation = [[InstallationAnnotation alloc] init];
		[self.mapAnnotations addObject:[installations objectAtIndex:i]];
		NSLog(@"%@",[mapAnnotations objectAtIndex:i]);
		[installation release];
	}
//		[text appendFormat:@"%@\n", [installations objectAtIndex:i]];
}

@end
