package com.teamx.tuner;

import java.io.Serializable;

/**
 * Created by Thinh on 18/05/2016.
 * Project: Tuner
 */
public class Tone implements Serializable {
    public String name;
    public String url;
    public boolean on;

    @Override
    public boolean equals(Object o) {
        return o instanceof Tone && ((Tone) o).url.equals(url) && ((Tone) o).name.equals(name);
    }
}
