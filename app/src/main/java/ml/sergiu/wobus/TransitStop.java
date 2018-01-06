package ml.sergiu.wobus;

import com.google.maps.model.LatLng;

import java.io.Serializable;

public class TransitStop implements Serializable {
    public String name;
    public LatLng coords;

    public TransitStop(String name, LatLng coords) {
        this.name = name;
        this.coords = coords;
    }


//    @Override
//    public String toString() {
//        return this.name;
//    }
}
