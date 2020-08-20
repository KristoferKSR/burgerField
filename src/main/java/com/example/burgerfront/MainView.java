package com.example.burgerfront;

import com.example.burgerJSON.BurgerParser;
import com.example.burgerJSON.BurgerRecognition;
import com.example.burgervenue.Venue;
import com.example.burgerphoto.Item;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.material.Material;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.*;
import java.util.*;
import java.util.List;

@Route
@Theme(Material.class)
public class MainView extends Div  {

    private MapLocationService service;
    private LeafletMap map;
    BurgerParser burgerParser = new BurgerParser();
    BurgerRecognition burgerRecognition = new BurgerRecognition();
    List<Venue> burgerSpots = new ArrayList<>();

    @Autowired
    public MainView(MapLocationService service){


        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();

        this.service = service;

        setSizeFull();
        //setPadding(false);
        //setSpacing(false);

      //  showIntro();

        // Create the map and add it to this view
        map = new LeafletMap();
        map.setWidthFull();


        // Register for marker clicks
        map.addMarkerClickListener(e -> showPopup(getVenueByName(e.getMarker().getName())));

        // Register for clicks on the map itself
        //map.addMapClickListener(this::mapClicked);

        // Add all known markers to the map
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
            //expand(center);
        add(workspace);

    }

    private Venue getVenueByName(String name) {
        for (Venue burgerSpot : burgerSpots) {
            if (burgerSpot.getName().equals(name)) return burgerSpot;
        }
        return null;
    }

    private VerticalLayout setUpNavBar(VerticalLayout content, LeafletMap map) {



        VerticalLayout navBar = new VerticalLayout();

        String query = "burger";

        burgerSpots = burgerParser.getBurgerspots(query);

        for (Venue burgerSpot : burgerSpots) {

            VerticalLayout venueLayout = new VerticalLayout();

            MapLocation spot = new MapLocation(burgerSpot.getLocation().getLat(), burgerSpot.getLocation().getLng(), burgerSpot.getName());
            service.addSpot(spot);
            map.addMarker(spot);

            Button venueButton = new Button(burgerSpot.getName(),  new Icon(VaadinIcon.ARROW_RIGHT),

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
        for (Item testImage : images) {
            String imageLinkString = testImage.getPrefix()+"200x200"+testImage.getSuffix();
            imageLinks.add(imageLinkString);
            Image image = new Image();
            image.setSrc(imageLinkString);
            imageRow.add(image);
        }

        try {

            burgerRecognition.postImages(imageLinks);

        } catch (URISyntaxException | JsonProcessingException e) {
            e.printStackTrace();
        }


        Button ok = new Button("OK!", VaadinIcon.CHECK.create());

        if (imageLinks.size() == 0) {
            Span subtitle = new Span("No new burgers...");
            VerticalLayout titleLayout = new VerticalLayout(title, subtitle, ok);
            titleLayout.setPadding(false);

            Dialog popupDialog = new Dialog(titleLayout);
            popupDialog.open();

            ok.addClickListener(e -> popupDialog.close());
        }
        else {
            VerticalLayout titleLayout = new VerticalLayout(title, imageRow, ok);
            titleLayout.setPadding(false);

            Dialog popupDialog = new Dialog(titleLayout);
            popupDialog.open();

            ok.addClickListener(e -> popupDialog.close());
        }


    }

    /**
     * Called when the user clicks the map. Creates a {@link Dialog} for the user to
     * input further data and to save the data.
     */
    private void mapClicked(LeafletMap.MapClickEvent event) {

        // Create a dialog for adding a marker. This is not part of the custom
        // component, just normal Vaadin stuff

        VerticalLayout popupLayout = new VerticalLayout();
        popupLayout.setPadding(false);

        Dialog popup = new Dialog(popupLayout);
        popup.open();

        Span coords = new Span(String.format("You selected the following coordinates: %f %f", event.getLatitude(), event.getLongitude()));

        TextField markerName = new TextField("What is this spot called?");
        markerName.setWidthFull();
        markerName.focus();

    }

    /**
     * Save a new marker in the backend and add it to the map
     */


}
/*
        BurgerParser burgerParser = new BurgerParser();
        BurgerRecognition burgerRecognition = new BurgerRecognition();



        // Have some data
        List<Venue> venues = burgerParser.getBurgerspots();

        // Create a grid bound to the list
        Grid<Venue> grid = new Grid<>();
        grid.setItems(venues);
        grid.addColumn(Venue::getName).setHeader("Name");
        grid.addColumn(Venue::getId)
                .setHeader("Location");

        List<String> imageLinks = new ArrayList<>();

        for (Venue venue : venues) {
            List<Item> testImages = burgerParser.getBurgerImageData(venue.getId());

            for (Item testImage : testImages) {
                String imageLinkString = testImage.getPrefix()+"500x500"+testImage.getSuffix();
                imageLinks.add(imageLinkString);
                Image image = new Image();

                image.setSrc(imageLinkString);
                add(image);
            }
        }





        workspace.add(grid);
       // Image image = new Image();
       // image.setSrc("https://fastly.4sqi.net/img/general/300x500/35080714_ClOv-5jTbZdD-nwf6ca7cF5wy1na4ArkDrDska05E2A.jpg");

        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);


        add(workspace);
        burgerRecognition.postImages(imageLinks);
        List<String> arrayList = new ArrayList<>();
        arrayList.add("https://prod-wolt-venue-images-cdn.wolt.com/577d28a9cb0e7d59ddec3592/0461e09645f2fb45395acd7e9a15f096");
        arrayList.add("https://astri.ee/assets/medias/1565/8641/6274/1/vlnd-burger-menuu-1565864162741-vlnd-burger-0.jpg");
        //burgerRecognition.postImages(arrayList);

        //crawl https://foursquare.com/developers/explore/#req=venues%2F4fddc66ee4b044e822e72fe6%2Fphotos for burx
    }
*/



