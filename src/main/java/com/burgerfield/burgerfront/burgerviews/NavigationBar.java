package com.burgerfield.burgerfront.burgerviews;

import com.burgerfield.burgerfront.LeafletMap;
import com.burgerfield.burgerfront.MapLocation;
import com.burgerfield.burgerfront.MapLocationService;
import com.burgerfield.objects.burgervenue.Venue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class NavigationBar extends VerticalLayout {

    private final MapLocationService service;

    public NavigationBar(MapLocationService service) {
        this.service = service;
        //this.setMaxHeight("800px");
        this.getStyle().set("overflow-y", "auto");

    }

    void setUpNavBar(LeafletMap map, List<Venue> burgerSpots, MainView mainView) {

        for (Venue burgerSpot : burgerSpots) {

            VerticalLayout venueLayout = new VerticalLayout();

            String burgerSpotName = burgerSpot.getName();
            double lat = burgerSpot.getLocation().getLat();
            double lng = burgerSpot.getLocation().getLng();
            MapLocation spot = new MapLocation(lat, lng, burgerSpotName);
            boolean isBurgerSpot = burgerSpot.getCategories().get(0).getName().contains("Burger");
            service.addSpot(spot);
            map.addMarker(spot, isBurgerSpot);
            Button venueButton = new Button(burgerSpot.getName(), new Icon(VaadinIcon.ARROW_RIGHT),
                    event -> {
                        map.panToLocation(spot);
                        mainView.generateVenuePopUp(burgerSpot);
                    }
            );
            venueButton.getStyle().set("font-family", "Courier");
            venueLayout.add(venueButton);
            add(venueLayout);
        }

    }

}
