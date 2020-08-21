package com.burgerfield.burgerfront;

import com.burgerfield.burgerJSON.BurgerParser;
import com.burgerfield.burgerJSON.BurgerRecognizer;
import com.burgerfield.objects.burgervenue.Venue;
import com.burgerfield.objects.burgerphoto.Item;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.*;
import java.util.*;
import java.util.List;

@Route
@Theme(Material.class)
public class MainView extends Div {

    private final MapLocationService service;
    BurgerParser burgerParser = new BurgerParser();
    BurgerRecognizer burgerRecognizer = new BurgerRecognizer();
    List<Venue> burgerSpots = new ArrayList<>();

    private static final String BURGER_QUERY = "burger";
    private static final String RESTAURANT_QUERY = "restaurant";


    @Autowired
    public MainView(MapLocationService service) {

        this.service = service;

        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();
        setSizeFull();

        // Create the map and add it to this view
        LeafletMap map = new LeafletMap();
        map.setWidthFull();
        map.addMarkerClickListener(e -> showPopup(getVenueByName(e.getMarker().getName())));
        map.addMarkersAndZoom(service.getAll());

        // Instantiate layouts
        HorizontalLayout header = new HorizontalLayout();
        VerticalLayout navBar = new VerticalLayout();
        VerticalLayout content = new VerticalLayout();
        HorizontalLayout center = new HorizontalLayout();
        HorizontalLayout footer = new HorizontalLayout();


        // Configure layouts
        header.setWidth("100%");
        header.setPadding(true);

        center.setWidth("100%");
        center.setPadding(true);
        center.setSpacing(true);

        content.setWidth("100%");

        footer.setWidth("100%");
        footer.setPadding(true);


        header.add(new H3("Burgerfield v0.65"));
        navBar.add(setUpNavBar(content, map));
        navBar.setWidth("300px");

        content.add(map);
        showIntro();

        // Compose layout
        center.add(navBar, content);
        center.setFlexGrow(1, navBar);
        workspace.add(header, center, footer);
        add(workspace);

    }

    private Venue getVenueByName(String name) {
        for (Venue burgerSpot : burgerSpots) if (burgerSpot.getName().equals(name)) return burgerSpot;
        return null;
    }

    private VerticalLayout setUpNavBar(VerticalLayout content, LeafletMap map) {

        VerticalLayout navBar = new VerticalLayout();
        burgerSpots = burgerParser.getBurgerspots(BURGER_QUERY);

        for (Venue burgerSpot : burgerSpots) {

            VerticalLayout venueLayout = new VerticalLayout();

            String burgerSpotName = burgerSpot.getName();
            double lat = burgerSpot.getLocation().getLat();
            double lng = burgerSpot.getLocation().getLng();
            MapLocation spot = new MapLocation(lat, lng, burgerSpotName);
            service.addSpot(spot);
            map.addMarker(spot);

            Button venueButton = new Button(burgerSpot.getName(), new Icon(VaadinIcon.ARROW_RIGHT),
                    event -> {
                        showPopup(burgerSpot);
                    }
            );

            venueLayout.add(venueButton);
            venueLayout.add(new Label(burgerSpot.getId()));
            venueLayout.add(new Label(burgerSpot.getCategories().get(0).getName()));
            navBar.add(venueLayout);
        }

        return navBar;
    }

    private void showIntro() {
        H3 title = new H3("Welcome to the Burgerfield Tartu");
        Span subtitle = new Span("You can find the newest burgers from your favourite burger spots by...");
        Span subtitleA = new Span("a) Clicking on an icon on the map");
        Span subtitleB = new Span("OR");
        Span subtitleC = new Span("b) Clicking on the name in the navbar");
        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, subtitleA, subtitleC, ok);
        titleLayout.setPadding(false);

        Dialog introDialog = new Dialog(titleLayout);
        introDialog.open();

        ok.addClickListener(e -> introDialog.close());
    }



    private void showPopup(Venue burgerSpot) {

        H3 title = new H3(burgerSpot.getName());

        HorizontalLayout imageRow = new HorizontalLayout();
        List<String> imageLinks = new ArrayList<>();
        List<Item> images = burgerParser.getBurgerImageData(burgerSpot.getId());
        addImages(images, imageLinks, imageRow);

        try {
            burgerRecognizer.postImages(imageLinks);
        } catch (URISyntaxException | JsonProcessingException e) {
            e.printStackTrace();
        }


        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        Span subtitle = new Span("No new burgers...");
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, ok);

        if (imageLinks.size() > 0) titleLayout = new VerticalLayout(title, imageRow, ok);

        titleLayout.setPadding(false);
        Dialog popupDialog = new Dialog(titleLayout);
        popupDialog.open();

        ok.addClickListener(e -> popupDialog.close());

    }

    private void addImages(List<Item> images, List<String> imageLinks, HorizontalLayout imageRow) {
        for (Item testImage : images) {
            String imageLinkString = testImage.getPrefix() + "200x200" + testImage.getSuffix();
            imageLinks.add(imageLinkString);
            Image image = new Image();
            image.setSrc(imageLinkString);
            imageRow.add(image);
        }
    }

    private void mapClicked(LeafletMap.MapClickEvent event) {

        VerticalLayout popupLayout = new VerticalLayout();
        popupLayout.setPadding(false);

        Dialog popup = new Dialog(popupLayout);
        popup.open();

        Span coords = new Span(String.format("You selected the following coordinates: %f %f", event.getLatitude(), event.getLongitude()));

        TextField markerName = new TextField("What is this spot called?");
        markerName.setWidthFull();
        markerName.focus();

    }

}


