//
//  InstallationAnnotation.h
//  carbongeiger
//
//  Created by Patrick Craston on 03/03/2011.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MKAnnotation.h>


@interface InstallationAnnotation : NSObject <MKAnnotation> {
	CLLocationCoordinate2D coordinate;
	NSString *title;
	NSString *subtitle;
	UIImage *image;
	BOOL overalloc;
	BOOL power;
	BOOL nearest;
}
@property (nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *subtitle;
@property (nonatomic, retain) UIImage *image;
@property (nonatomic) BOOL overalloc;
@property (nonatomic) BOOL power;
@property (nonatomic) BOOL nearest;

@end
