//
//  InstallationAnnotation.m
//  carbongeiger
//
//  Created by Patrick Craston on 28/02/2011.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "InstallationAnnotation.h"


@implementation InstallationAnnotation

@synthesize image;
@synthesize latitude;
@synthesize longitude;

- (CLLocationCoordinate2D)coordinate;
{
    CLLocationCoordinate2D theCoordinate;
    theCoordinate.latitude = 37.786996;
    theCoordinate.longitude = -122.419281;
    return theCoordinate; 
}

- (void)dealloc
{
    [image release];
    [super dealloc];
}

- (NSString *)title
{
    return @"San Francisco";
}

// optional
- (NSString *)subtitle
{
    return @"Founded: June 29, 1776";
}

@end
