package com.burgerfield.burgerfront.burgerviews;

import com.burgerfield.burgerJSON.BurgerParser;
import com.burgerfield.burgerJSON.BurgerRecognizer;
import com.burgerfield.burgerfront.LeafletMap;
import com.burgerfield.burgerfront.MapLocation;
import com.burgerfield.burgerfront.MapLocationService;
import com.burgerfield.objects.burgervenue.Venue;
import com.burgerfield.objects.burgerphoto.Item;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.material.Material;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.*;
import java.util.*;
import java.util.List;

@Route
public class MainView extends Div {

    private final MapLocationService service;
    BurgerParser burgerParser = new BurgerParser();
    BurgerRecognizer burgerRecognizer = new BurgerRecognizer();
    List<Venue> burgerSpots = new ArrayList<>();

    private static final String BURGER_QUERY = "burger";
    private static final String RESTAURANT_QUERY = "restaurant";
    VenueNavbar venueNavbar;
    LeafletMap map;
    VerticalLayout content;
    HorizontalLayout center;
    boolean light = true;
    boolean isSetToTallinn = false;

    public void setBurgerSpots(List<Venue> burgerSpots) {
        this.burgerSpots = burgerSpots;
    }

    @Autowired
    public MainView(MapLocationService service) {

        this.service = service;
        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();

        // Create the map and add it to this view
        map = new LeafletMap();
        map.setWidthFull();
        map.addMarkerClickListener(e -> {
            map.panToLocation(e.getMarker());
            showPopup(getVenueByName(e.getMarker().getName()));
        });
        map.addMarkersAndZoom(service.getAll());

        MainHeader mainHeader = new MainHeader();
        mainHeader.setUpHeader(this, isSetToTallinn);
        mainHeader.setPadding(true);
        mainHeader.setWidth("100%");
        mainHeader.setHeight("100px");

        // Instantiate layouts
        content = new VerticalLayout();
        center = new HorizontalLayout();
        HorizontalLayout footer = new HorizontalLayout();

        // Configure layouts
        venueNavbar = new VenueNavbar(service);
        refreshWithNewData(true, false);

        center.setWidth("100%");
        center.setPadding(true);
        center.setSpacing(true);

        content.setWidth("100%");
        footer.setWidth("100%");

        // Compose layout
        center.setFlexGrow(1, venueNavbar);
        workspace.add(mainHeader, center, footer);
        showIntro();
        add(workspace);

        Button darkModeButton = new Button("Switch to dark mode");
        Button tallinnButton = new Button("Try tallinn?");
        HorizontalLayout footerSpacer = new HorizontalLayout();
        HorizontalLayout footerMiddleSpacer = new HorizontalLayout();
        footerSpacer.setWidth("1%");
        footerMiddleSpacer.setWidth("80%");
        footer.add(footerSpacer, darkModeButton, footerMiddleSpacer, tallinnButton);

        darkModeButton.addClickListener(e -> {
            if (light) {
                light = false;
                darkModeButton.setText("Switch to light mode");
            } else {
                light = true;
                darkModeButton.setText("Switch to dark mode");
            }
            setVisualStyle(light, footer, workspace, venueNavbar, mainHeader);

        });

        tallinnButton.addClickListener(e -> {

            if (!isSetToTallinn) {
                MapLocation tallinnLocation = new MapLocation(59.436962, 24.753574, "Tallinn");
                map.panToLocation(tallinnLocation);
                switchToTallinn();
                tallinnButton.setText("Go back");
            }
            else {
                MapLocation tartuLocation = new MapLocation(58.378025, 26.728493, "Tallinn");
                switchToTartu();
                map.panToLocation(tartuLocation);
                tallinnButton.setText("Try Tallinn?");

            }
        });
        setVisualStyle(light, footer, workspace, venueNavbar, mainHeader);
    }

    private void switchToTallinn() {
        isSetToTallinn = true;

        refreshWithNewData(true, false);
    }
    private void switchToTartu() {
        isSetToTallinn = false;

        refreshWithNewData(true, false);
    }

    private void setVisualStyle(boolean light, HorizontalLayout footer, VerticalLayout workspace, VenueNavbar venueNavbar, MainHeader header) {
        ThemeList themeList = UI.getCurrent().getElement().getThemeList();

        if (light) {

            footer.getStyle().set("background-color", "#ffffff");
            workspace.getStyle().set("background-color", "#ffffff");
            themeList.set(Material.LIGHT, true);
            themeList.set(Material.DARK, false);
            map.setDarkMode();

        } else {

            footer.getStyle().set("background-color", "#0d0d0d");
            workspace.getStyle().set("background-color", "#0d0d0d");
            themeList.set(Material.DARK, true);
            themeList.set(Material.LIGHT, false);
            map.setLightMode();

        }
        workspace.getStyle().set("font-family", "Courier");
        venueNavbar.getElement().getStyle().set("font-family", "Courier");
    }

    private Venue getVenueByName(String name) {
        for (Venue burgerSpot : burgerSpots) if (burgerSpot.getName().equals(name)) return burgerSpot;
        return null;
    }

    private void showIntro() {
        H3 title = new H3("Welcome to the Burgerfield Tartu");
        Span subtitle = new Span("You can find the newest burgers from your favourite burger spots by...");
        Span subtitleA = new Span("a) Clicking on an icon on the map");
        Span subtitleC = new Span("b) Clicking on the name in the navbar");
        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, subtitleA, subtitleC, ok);
        titleLayout.setPadding(false);

        Dialog introDialog = new Dialog(titleLayout);
        introDialog.open();
        ok.addClickListener(e -> introDialog.close());
    }


    void showPopup(Venue burgerSpot) {

        H3 title = new H3(burgerSpot.getName());

        HorizontalLayout imageRow = new HorizontalLayout();
        List<String> imageLinks = new ArrayList<>();
        List<Item> images = burgerParser.getBurgerImageData(burgerSpot.getId());
        addImages(images, imageLinks, imageRow);

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
            String imageLinkString = testImage.getPrefix() + "300x300" + testImage.getSuffix();
            imageLinks.add(imageLinkString);
        }
        if (imageLinks.size() > 0) {
            try {
                String burgerImageLink = burgerRecognizer.postImages(imageLinks);
                System.out.println("burgerimageLink: " + burgerImageLink);
                if (burgerImageLink.contains("https")) {
                    Image image = new Image();
                    image.setSrc(burgerImageLink);
                    imageRow.add(image);
                } else {
                    Span subtitle = new Span("No new burgers...");
                    imageRow.add(subtitle);
                }
            } catch (URISyntaxException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }

    }

    public void refreshWithNewData(boolean showBurgers, boolean showRestaurants) {
        venueNavbar.removeAll();
        center.removeAll();
        content.removeAll();
        map.removeMarkers();
        venueNavbar = new VenueNavbar(service);
        burgerSpots = new ArrayList<>();

        if (isSetToTallinn) {
            if (showBurgers && showRestaurants) {
                List<Venue> allSpots = new ArrayList<>();
                allSpots.addAll(burgerParser.getTallinnBurgerSpots(BURGER_QUERY));
               // allSpots.addAll(burgerParser.getTallinnBurgerSpots(RESTAURANT_QUERY));
                burgerSpots = allSpots;
            }
            if (showBurgers && !showRestaurants) burgerSpots = burgerParser.getTallinnBurgerSpots(BURGER_QUERY);
            //if (showRestaurants && !showBurgers) burgerSpots = burgerParser.getTallinnBurgerSpots(RESTAURANT_QUERY);


        }
        else {
            if (showBurgers && showRestaurants) {
                List<Venue> allSpots = new ArrayList<>();
                allSpots.addAll(burgerParser.getBurgerspots(BURGER_QUERY));
                allSpots.addAll(burgerParser.getBurgerspots(RESTAURANT_QUERY));
                burgerSpots = allSpots;
            }
            if (showBurgers && !showRestaurants) burgerSpots = burgerParser.getBurgerspots(BURGER_QUERY);
            if (showRestaurants && !showBurgers) burgerSpots = burgerParser.getBurgerspots(RESTAURANT_QUERY);
        }

       // if (!isSetToTallinn) map.setMiddle();
        venueNavbar.setUpNavBar(map, burgerSpots, this);
        venueNavbar.setWidth("365px");
        venueNavbar.setMaxHeight("800px");
        center.add(venueNavbar, content);
        center.setMaxHeight("800px");
        content.setHeight("800px");
        content.add(map);

    }
}


