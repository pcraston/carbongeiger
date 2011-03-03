//
//  InstallationAnnotation.m
//  carbongeiger
//
//  Created by Patrick Craston on 03/03/2011.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "InstallationAnnotation.h"


@implementation InstallationAnnotation
@synthesize coordinate, title, subtitle;

- (id) initWithDictionary:(NSDictionary *) dict
{
	self = [super init];
	if (self != nil) {
		coordinate.latitude = [[dict objectForKey:@"lat"] doubleValue];
		coordinate.longitude = [[dict objectForKey:@"lon"] doubleValue];
		self.title = [dict objectForKey:@"name"];
		self.subtitle = [dict objectForKey:@"company"];
	}
	return self;
}

@end
