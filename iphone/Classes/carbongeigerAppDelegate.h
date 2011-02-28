//
//  iGeigerAppDelegate.h
//  iGeiger
//
//  Created by Patrick Craston on 20/11/2010.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class carbongeigerViewController;

@interface carbongeigerAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    carbongeigerViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet carbongeigerViewController *viewController;

@end

