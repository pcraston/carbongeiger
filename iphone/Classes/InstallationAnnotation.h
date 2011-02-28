//
//  InstallationAnnotation.h
//  carbongeiger
//
//  Created by Patrick Craston on 28/02/2011.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <MapKit/MapKit.h>


@interface InstallationAnnotation : NSObject<MKAnnotation> {
	UIImage *image;
    NSNumber *latitude;
    NSNumber *longitude;
//	NSString *mTitle;
//	NSString *mSubTitle;
}

@property (nonatomic, retain) UIImage *image;
@property (nonatomic, retain) NSNumber *latitude;
@property (nonatomic, retain) NSNumber *longitude;

@end