package com.burgerfield.burgerfront;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;


@Service
@ApplicationScope
public class MapLocationService {

    private final List<MapLocation> spots = new ArrayList<MapLocation>();

    @PostConstruct
    private void init() {

        spots.add(new MapLocation(58.38062, 26.72509, "Hesburger"));

    }

    public List<MapLocation> getAll() {

        return Collections.unmodifiableList(spots);
    }

    public void addSpot(MapLocation spot) {

        // protect concurrent access since MapLocationService is a singleton
        synchronized (spots) {

            spots.add(spot);

            if (spots.size() > 100) {
                spots.remove(0);
            }
        }
    }
}