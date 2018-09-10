/**
 * ifyusion
 *
 * Created by Your Name
 * Copyright (c) 2018 Your Company. All rights reserved.
 */

#import "fyuseView.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"

@implementation fyuseView

-(UIInterfaceOrientationMask)supportedInterfaceOrientations{
    return UIInterfaceOrientationMaskLandscapeRight;
}

-(BOOL)shouldAutorotate {
    return NO;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated;
{
    [super viewDidAppear:animated];
    NSLog(@"Lets Display the Fyuse");

    fy = [FYFyuseView new];
    fy.priority = FyusePriorityVisible;
    fy.motionEnabled = YES;
    fy.zoomEnabled = YES;
    fy.supportedInterfaceOrientation = UIInterfaceOrientationMaskLandscapeRight;
    fy.hidden = NO;
    fy.showTags = YES;
    fy.contentMode = UIViewContentModeScaleAspectFit;  //UIViewContentModeScaleFill
    fy.delegate = self;
    fy.preferredResolution = FYFyuseResolutionNormal;
    fy.frame = self.view.frame;
    fy.center = self.view.center;
    fy.fyuse = fyuse;
    
    [self.view addSubview:fy];
    
    UIButton *btnDismiss = [[UIButton alloc] initWithFrame:CGRectMake(20, 20, 30, 30)];
    [btnDismiss setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [btnDismiss setTitle:@"X" forState:UIControlStateNormal];
    [btnDismiss setBackgroundColor:[UIColor whiteColor]];
    [btnDismiss.titleLabel setFont:[UIFont systemFontOfSize:18]];
    btnDismiss.layer.cornerRadius = 15;
    [btnDismiss addTarget:self action:@selector(dismissVC:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btnDismiss];
    
    
    loadingLabel = [[UILabel alloc]initWithFrame:CGRectMake(55, 20, 150, 30)];//Set frame of label in your viewcontroller.
    [loadingLabel setFont: [loadingLabel.font fontWithSize: 18]];
    [loadingLabel setText:@"Loading"];//Set text in label.
    [loadingLabel setTextColor:[UIColor whiteColor]];//Set text color in label.
    [loadingLabel setTextAlignment:NSTextAlignmentLeft];//Set text alignment in label.
    [loadingLabel setNumberOfLines:1];//Set number of lines in label.
    [self.view addSubview:loadingLabel];//Add it to the view of your choice.
}


-(void)dismissVC:(id)sender {
    [self dismissViewControllerAnimated:NO completion:nil];
}

- (void)setFyuseForViewing:(FYFyuse *) theFyuse
{
    NSLog(@"%@", [theFyuse class]);
    fyuse = theFyuse;
}

- (void)fyuseView:(nonnull FYFyuseView *)fyuseView didUpdateProgress:(CGFloat)progress fyuseWithResolution:(FYFyuseResolution)resolution{
    // Hide Loading label when progress reaches 1.0
    if (progress == 1.0) {
        [loadingLabel removeFromSuperview];
    }
}

- (void)fyuseView:(nonnull FYFyuseView *)fyuseView didDisplayFyuseWithResolution:(FYFyuseResolution)resolution{

}

- (void)fyuseView:(nonnull FYFyuseView *)fyuseView didFailWithError:(nullable NSError *)error{
    NSLog(@"Failed to load a fyuse: %@", error);
}

- (void)fyuseViewDidSingleTap:(nonnull FYFyuseView *)fyuseView{
    NSLog(@"Single Tapped the FyuseView");
    
}


- (void)fyuseView:(FYFyuseView *_Nonnull)fyuseView didTapOnTagWithIndex:(NSInteger)index{

}


@end
