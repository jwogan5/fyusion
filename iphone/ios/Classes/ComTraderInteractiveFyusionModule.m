/**
 * ifyusion
 *
 * Created by Your Name
 * Copyright (c) 2018 Your Company. All rights reserved.
 */

#import "ComTraderinteractiveFyusionModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"


@implementation ComTraderinteractiveFyusionModule


#pragma mark Internal

// This is generated for your module, please do not change it
- (id)moduleGUID
{
  return @"6bd3cdbd-b232-42c3-b39d-2e235a9947ca";
}

// This is generated for your module, please do not change it
- (NSString *)moduleId
{
  return @"com.traderinteractive.fyusion";
}

#pragma mark Lifecycle

- (void)startup
{
  // This method is called when the module is first loaded
  // You *must* call the superclass
  [super startup];
  DebugLog(@"[DEBUG] %@ loaded", self);

  [FYAuthManager initializeWithAppID: @"vgjN_pN5Twoz8EKVe69yOJ" appSecret: @"4oFb5XT3X2gr27NU7On5sILcluG3gZrf"];
}

#pragma Public APIs

- (NSString *)getVersion
{
    return @"0.0.1";
}

/********************************************************************************************************************************************************
 ********************************************************************************************************************************************************/

/*
    Create Fyusion Methods
*/
- (void)startSession:(id)args
{
    ENSURE_SINGLE_ARG(args,NSDictionary);
    _sessionId = [TiUtils stringValue:@"id" properties:args];
    
    if ([_sessionId isEqualToString:@"new"])
    {
        NSLog(@"Brand New Fyusion");
        FYSessionViewController *fyuseSession = [[FYSessionViewController alloc] init];
        fyuseSession.sessionDelegate = self;
        fyuseSession.skipPhotos = YES;
        fyuseSession.disableOverlayGuides = YES;
        [[TiApp app] showModalController: fyuseSession animated: YES];
    } else {
        NSLog(@"Master Fyusion Detected");
        FYSessionViewController *fyuseSession = [[FYSessionViewController alloc] initWithSessionIdentifier:_sessionId];
        fyuseSession.sessionDelegate = self;
        fyuseSession.skipPhotos = YES;
        fyuseSession.disableOverlayGuides = YES;
        [[TiApp app] showModalController: fyuseSession animated: YES];
    }
}

- (void)sessionControllerDidDismiss:(FYSessionViewController *)sessionController{
    NSLog(@"Closing Fyusion Camera");
    
    if ([self _hasListeners:@"response"]) {
        [self fireEvent:@"response" withObject:@{ @"message": @"Cancelled Session"}];
    }
}

- (void)sessionController:(FYSessionViewController *)sessionController didSaveSessionWithIdentifier:(NSString *)identifier {
    NSLog(@"%@", identifier);
    
    if ([self _hasListeners:@"response"]) {
        // Get the tag count
        NSInteger tagCount = [FYSessionManager tagCountForSessionWithId:identifier];
        
        // Save the thumbnail to the application data directory
        [FYSessionManager requestDetailImageForSessionId:identifier detailPhoto:FYSessionManager.thumbnailDetailPhoto completion:^(UIImage* photo){
            NSData *imageData = UIImageJPEGRepresentation(photo, 80);
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
            NSString *documentsDirectory = [paths objectAtIndex:0];
            NSString *imagePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpg", identifier]];

            if (![imageData writeToFile:imagePath atomically:NO])
            {
                // Send the fyusion id back to the app
                NSLog(@"Failed to save the thumbnail.");
                [self fireEvent:@"response" withObject:@{ @"message": @"Saved Session", @"localId": identifier, @"tags": @(tagCount).stringValue, @"thumbPath": @"Missing Thumb"}];
            }
            else
            {
                // Send the fyusion id back to the app
                NSLog(@"Saved the thumb successfully.");
                [self fireEvent:@"response" withObject:@{ @"message": @"Saved Session", @"localId": identifier, @"tags": @(tagCount).stringValue, @"thumbPath": identifier}];
            }
                
        }];
    }
}

/********************************************************************************************************************************************************
********************************************************************************************************************************************************/

/*
    Upload Fyusion Methods
 */
- (void)uploadSessionWithId:(id)args
{
    ENSURE_SINGLE_ARG(args,NSDictionary);
    NSString *localId = [TiUtils stringValue:@"id" properties:args];
    _sessionId = localId;

    currentUploadProgress = 0;
    currentUploadProgressAbove = 0;
    
    // Upload the session and then listen for it
    fyuseUploadManager = [FYUploadSessionManager new];
    fyuseUploadManager.delegate = self;
    fyuseUploadManager.disableBackgroundUpload = YES;
    [fyuseUploadManager uploadSessionWithIdentifier:localId];
}


- (void)sessionFinishedUploadingWithUID:(NSString *)uid {
    NSLog(@"Fyusion Upload Successful");
    
    currentUploadProgress = 0;
    currentUploadProgressAbove = 0;
  
    // Get the remote id of the 360 only fyuse. The uid is the sessionId not the fyuse id.
    NSString *remoteId = [FYUploadSessionManager mainFyuseIDForSessionIdentifier:_sessionId];

    if ([self _hasListeners:@"response"]) {
        [self fireEvent:@"response" withObject:@{ @"message": @"Upload Session Success", @"remoteId": remoteId, @"remoteSessionId": uid}];
    }
}

- (void)sessionFailedUploading{
    NSLog(@"Upload Session Failed");
    
    currentUploadProgress = 0;
    currentUploadProgressAbove = 0;
   
    if ([self _hasListeners:@"response"]) {
        [self fireEvent:@"response" withObject:@{ @"message": @"Upload Session Failed"}];
    }
}

- (void)sessionUpdatedUploadProgress:(CGFloat)progress{
    progress = ((progress * 100) / 2) + 50;

    if (progress > currentUploadProgressAbove) {
        NSString *p = [NSString stringWithFormat:@"%.f", progress];
        
        
        if ([p isEqualToString:@"50"] || [p isEqualToString:@"60"] || [p isEqualToString:@"70"] || [p isEqualToString:@"80"] || [p isEqualToString:@"90"])
        {
            if (![p isEqualToString:currentUploadProgress]) {
                currentUploadProgress = p;
                currentUploadProgressAbove = progress + 9;
                if ([self _hasListeners:@"response"]) {
                    [self fireEvent:@"response" withObject:@{ @"message": @"Upload Progress", @"progress":p}];
                }
            }
        }
    }
}

- (void)sessionUpdatedUploadPreparationProgress:(CGFloat)progress{

}

/********************************************************************************************************************************************************
 ********************************************************************************************************************************************************/

/*
   Manage Fyusions
*/
- (void)fetchLocalIds
{
    NSArray *ids = [FYSessionManager allSessionIDs];
    NSLog(@"%@", ids);
    if ([self _hasListeners:@"response"]) {
        [self fireEvent:@"response" withObject:@{ @"message": @"Local Ids", @"ids": ids}];
    }
}

- (void)deleteLocalSessionWithId:(id)args
{
    ENSURE_SINGLE_ARG(args,NSDictionary);
    NSString *localId = [TiUtils stringValue:@"id" properties:args];
    
    // Delete a Local Session
    [FYSessionManager deleteSessionWithId:localId];
}

/*
 View Fyusions
 */
-(void)viewFyuseWithId:(id)args
{
    ENSURE_SINGLE_ARG(args,NSDictionary);
    NSString *fyuseId = [TiUtils stringValue:@"id" properties:args];
    NSString *fyuseLocation = [TiUtils stringValue:@"location" properties:args];
    fyuseView *fv = [fyuseView new];
    fv.view.backgroundColor = [UIColor blackColor];

    if ([fyuseLocation isEqualToString:@"localOnly"]) {
        [FYSessionManager requestMainFyuseForSessionWithIdentifier:fyuseId completion:^(FYFyuse *f) {
            [fv setFyuseForViewing:f];
            [[TiApp app] showModalController: fv animated: NO];
        }];
    } else if ([fyuseLocation isEqualToString:@"localUploaded"]) {
        [FYSessionManager requestMainFyuseForSessionWithIdentifier:fyuseId completion:^(FYFyuse *f) {
            [fv setFyuseForViewing:f];
            [[TiApp app] showModalController: fv animated: NO];
        }];
    } else {
        FYFyuseManager *fymanager = [FYFyuseManager new];
        [fymanager requestFyuseWithUID:fyuseId onSuccess:^(FYFyuse *f) {
            NSLog(@"Fetched a remote fyuse.");
            [fv setFyuseForViewing:f];
            [[TiApp app] showModalController: fv animated: NO];
        } onFailure:^(NSError *error) {
            NSLog(@"Failed to load a fyuse: %@", error);
            if ([self _hasListeners:@"response"]) {
                [self fireEvent:@"response" withObject:@{ @"message": @"Unable to View Session", @"id": fyuseId}];
            }
        }];
    }
}


@end
