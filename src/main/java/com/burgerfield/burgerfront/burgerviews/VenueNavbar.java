package com.burgerfield.burgerfront.burgerviews;

import com.burgerfield.burgerJSON.BurgerParser;
import com.burgerfield.burgerfront.LeafletMap;
import com.burgerfield.burgerfront.MapLocation;
import com.burgerfield.burgerfront.MapLocationService;
import com.burgerfield.objects.burgervenue.Venue;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class VenueNavbar extends VerticalLayout {

    private final MapLocationService service;

    public VenueNavbar(MapLocationService service) {
        this.service = service;
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
                        mainView.showPopup(burgerSpot);
                    }
            );

            venueLayout.add(venueButton);
            //if (burgerSpot.getLocation() != null) venueLayout.add(new Label(burgerSpot.getLocation().getAddress()));
            //venueLayout.add(new Label(burgerSpot.getCategories().get(0).getName()));
            add(venueLayout);
        }

    }

}
