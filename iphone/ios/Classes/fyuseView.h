/**
 * ifyusion
 *
 * Created by Your Name
 * Copyright (c) 2018 Your Company. All rights reserved.
 */

@import FyuseSessionTagging;

@interface fyuseView : UIViewController <FYFyuseViewDelegate>{
    FYFyuseView *fy;
    FYFyuse *fyuse;
    UILabel *loadingLabel;
}

- (void)setFyuseForViewing:(FYFyuse *) theFyuse;

@end
