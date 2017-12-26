package ml.sergiu.wobus;

import java.net.URI;
import java.util.List;

public class TransitLine {
    public String name;
    public URI uri;
    public TransitType type;
    public String endA, endB;
    List<String> departuresA, departuresB;

    public TransitLine(String name, URI uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return this.name;
    }

    enum TransitType {
        BUS, MINIBUS, TRAM, TROLLEYBUS;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}
