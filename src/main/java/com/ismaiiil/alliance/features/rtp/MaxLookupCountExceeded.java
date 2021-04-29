package com.ismaiiil.alliance.features.rtp;


public class MaxLookupCountExceeded extends RuntimeException{

    public MaxLookupCountExceeded() {
        super("Max recursive count has been exceeded");
    }

}
