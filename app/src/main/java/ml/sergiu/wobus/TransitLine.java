package ml.sergiu.wobus;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class TransitLine implements Serializable {
    public String name;
    public URI uri;
    private URI mapImageURI;
    public TransitType type;
    public String endA, endB;
    List<String> departuresA, departuresB;

    public TransitLine(String name, URI uri) {
        this.name = name;
        this.uri = uri;
        this.mapImageURI = null;
    }

    @Override
    public String toString() {
        return this.name;
    }

    enum TransitType implements Serializable {
        BUS, MINIBUS, TRAM, TROLLEYBUS;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public Optional<URI> getMapImageURI() {
        if(this.mapImageURI == null) {
            return Optional.empty();
        }
        return Optional.of(mapImageURI);
    }

    public void setMapImageURI(URI x) {
        this.mapImageURI = x;
    }
}
