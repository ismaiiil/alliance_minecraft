package com.ismaiiil.alliance.Utils;

import lombok.ToString;

public class Tuple<X, Y> {
    public X _1;
    public Y _2;
    public Tuple(X _1, Y _2) {
        this._1 = _1;
        this._2 = _2;
    }
}
