//Extensions to scale UIImage

@interface UIImage (UIImageFunctions)
- (UIImage *) scaleToSize: (CGSize)size;
- (UIImage *) scaleProportionalToSize: (CGSize)size;
@end
@implementation UIImage (UIImageFunctions)

- (UIImage *) scaleToSize: (CGSize)size
{
    if(size.height==self.size.height&&size.width==self.size.width)
        return self;
    // Scalling selected image to targeted size
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(NULL, size.width, size.height, 8, 0, colorSpace, kCGImageAlphaPremultipliedLast);
    CGContextClearRect(context, CGRectMake(0, 0, size.width, size.height));
    
    if(self.imageOrientation == UIImageOrientationRight)
    {
        CGContextRotateCTM(context, -M_PI_2);
        CGContextTranslateCTM(context, -size.height, 0.0f);
        CGContextDrawImage(context, CGRectMake(0, 0, size.height, size.width), self.CGImage);
    }
    else
        CGContextDrawImage(context, CGRectMake(0, 0, size.width, size.height), self.CGImage);
    
    CGImageRef scaledImage=CGBitmapContextCreateImage(context);
    
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context);
    
    UIImage *image = [UIImage imageWithCGImage: scaledImage];
    
    CGImageRelease(scaledImage);
    
    return image;
}

- (UIImage *) scaleProportionalToSize: (CGSize)size1
{
    if(self.size.width>self.size.height)
    {
        NSLog(@"LandScape");
        size1=CGSizeMake((self.size.width/self.size.height)*size1.height,size1.height);
    }
    else
    {
        NSLog(@"Potrait");
        size1=CGSizeMake(size1.width,(self.size.height/self.size.width)*size1.width);
    }
    
    return [self scaleToSize:size1];
}

@end
