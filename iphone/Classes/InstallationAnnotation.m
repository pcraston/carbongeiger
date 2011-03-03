//
//  InstallationAnnotation.m
//  carbongeiger
//
//  Created by Patrick Craston on 03/03/2011.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "InstallationAnnotation.h"


@implementation InstallationAnnotation
@synthesize coordinate, title, subtitle, overalloc, power, nearest, image;

- (id) initWithDictionary:(NSDictionary *) dict
{
	self = [super init];
	if (self != nil) {
		coordinate.latitude = [[dict objectForKey:@"lat"] doubleValue];
		coordinate.longitude = [[dict objectForKey:@"lon"] doubleValue];
		self.title = [dict objectForKey:@"name"];
		NSString *company = [dict objectForKey:@"company"];
		NSString *parent_company = [dict objectForKey:@"parent_company"];
		if ([parent_company isEqualToString:@""] || [parent_company isEqualToString: company]) {
			self.subtitle = [NSString stringWithFormat:@"%@",company];	
		} else {
			self.subtitle = [NSString stringWithFormat:@"%@, %@",company,parent_company];	
		}
		if ([[dict objectForKey:@"overalloc"] doubleValue] == 1) {
			self.overalloc = YES;
		} else {
			self.overalloc = NO;
		}
		if ([[dict objectForKey:@"power"] doubleValue] == 1) {
			self.power = YES;
		} else {
			self.power = NO;
		}
		self.nearest = NO;
	}
	return self;
}

@end
