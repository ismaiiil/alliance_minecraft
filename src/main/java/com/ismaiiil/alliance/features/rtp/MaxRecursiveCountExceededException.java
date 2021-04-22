package com.ismaiiil.alliance.features.rtp;


public class MaxRecursiveCountExceededException extends RuntimeException{

    public MaxRecursiveCountExceededException() {
        super("Max recursive count has been exceeded");
    }

}
