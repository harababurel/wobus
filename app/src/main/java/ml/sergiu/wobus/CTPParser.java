package ml.sergiu.wobus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.CertPathTrustManagerParameters;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CTPParser {
    private static final String baseURL = "http://ctpcj.ro/index.php/en/timetables/urban-lines/linia-";
    private Map<String, BusLine> busLines;

    public static CTPParser instance = null;

    protected CTPParser() {
        // exists only to defeat instantiation.
        busLines = new HashMap<>();
        AddOrUpdateBusLine(new BusLine("24B", BusLine.BusType.BUS));
        AddOrUpdateBusLine(new BusLine("25", BusLine.BusType.BUS));
        AddOrUpdateBusLine(new BusLine("6", BusLine.BusType.TROLLEYBUS));
    }

    public static CTPParser getInstance() {
        if (instance == null) {
            instance = new CTPParser();
        }
        return instance;
    }

    public void AddOrUpdateBusLine(BusLine busLine) {
        busLines.put(busLine.name, busLine);
    }

    public Optional<BusLine> getBusLine(String name) {
        if (busLines.containsKey(name)) {
            return Optional.of(busLines.get(name));
        }
        return Optional.empty();
    }

    public Collection<BusLine> busLines() {
        return busLines.values();
    }


}
