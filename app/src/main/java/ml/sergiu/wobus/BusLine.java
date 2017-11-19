package ml.sergiu.wobus;

import java.util.List;

public class BusLine {
    public String name;
    public BusType type;
    public String endA, endB;
    List<String> departuresA, departuresB;
    
    public BusLine(String name, BusType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.type.toString() + ")";
    }

    enum BusType {
        BUS, MINIBUS, TRAM, TROLLEYBUS;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}
