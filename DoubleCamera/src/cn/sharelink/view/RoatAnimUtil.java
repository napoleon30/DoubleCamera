package cn.sharelink.view;

import android.view.View;
import android.view.animation.RotateAnimation;

public class RoatAnimUtil {  
	//旋出的动画，无延迟时间  
    public static void startAnimationOut(View view) {  
        startAnimationOut(view, 0);  
  
    }  
    //旋入的动画，无延迟时间  
    public static void startAnimationIn(View view) {  
        startAnimationIn(view, 0);  
    }  
  //旋出的动画  
    //delay为动画延迟的时间，单位是毫秒 
    public static void startAnimationOut(View view, long delay) {  
       RotateAnimation animation = new RotateAnimation(45, 0,  
                view.getWidth() / 2, view.getHeight()/2);  
        animation.setDuration(500);  
        animation.setStartOffset(0);  
       animation.setFillAfter(true);  
       view.startAnimation(animation);  
  
    }  
  //旋入的动画  
    //delay为动画延迟的时间，单位是毫秒  
    public static void startAnimationIn(View view, long delay) {  
        RotateAnimation animation = new RotateAnimation(0, 45,  
                view.getWidth() / 2, view.getHeight()/2);  
       animation.setDuration(500);  
        animation.setStartOffset(0);  
        animation.setFillAfter(true);  
        view.startAnimation(animation);  
    }  
  
}  
