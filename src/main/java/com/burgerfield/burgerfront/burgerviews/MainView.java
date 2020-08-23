package com.burgerfield.burgerfront.burgerviews;

import com.burgerfield.burgerJSON.BurgerParser;
import com.burgerfield.burgerJSON.BurgerRecognizer;
import com.burgerfield.burgerfront.LeafletMap;
import com.burgerfield.burgerfront.MapLocation;
import com.burgerfield.burgerfront.MapLocationService;
import com.burgerfield.objects.burgerphoto.Checkin;
import com.burgerfield.objects.burgervenue.Venue;
import com.burgerfield.objects.burgerphoto.Item;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
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

    private static final String BURGER_QUERY = "4bf58dd8d48988d16c941735";
    private static final String AMERICAN_RESTAURANT_QUERY = "4bf58dd8d48988d14e941735";
    private static final String FOOD_TRUCK_QUERY = "4bf58dd8d48988d1cb941735";
    private static final String ALL_RESTAURANT_QUERY = "4d4b7105d754a06374d81259";


    private static final String DARK_BACKGROUND_COLOR = "#ffffff";
    private static final String LIGHT_BACKGROUND_COLOR = "#0d0d0d";

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
        burgerParser.getBurgerspots(BURGER_QUERY, isSetToTallinn);
        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();

        // Create the map and add it to this view
        setUpMap();

        MainHeader mainHeader = new MainHeader();
        mainHeader.setUpHeader(this, isSetToTallinn);
        mainHeader.setPadding(true);
        mainHeader.setWidth("100%");
        mainHeader.setHeight("10%");

        // Instantiate layouts
        content = new VerticalLayout();
        center = new HorizontalLayout();
        HorizontalLayout footer = new HorizontalLayout();

        // Configure layouts
        venueNavbar = new VenueNavbar(service);

        refreshWithNewData(mainHeader);

        center.setWidth("100%");
        center.setPadding(true);
        center.setSpacing(true);

        content.setWidth("100%");
        footer.setWidth("100%");
        footer.setHeight("10%");

        // Compose layout
        center.setFlexGrow(1, venueNavbar);
        workspace.add(mainHeader, center, footer);
        showIntro();
        add(workspace);

        Button visualStyleSwitchButton = new Button("Switch to dark mode");
        Button tallinnButton = new Button("Try tallinn?");
        HorizontalLayout footerSpacer = new HorizontalLayout();
        HorizontalLayout footerMiddleSpacer = new HorizontalLayout();
        footerSpacer.setWidth("1%");
        footerMiddleSpacer.setWidth("80%");
        footer.add(footerSpacer, visualStyleSwitchButton, footerMiddleSpacer, tallinnButton);

        setUpDarkLightSwitchButton(visualStyleSwitchButton, footer, workspace, mainHeader);
        setUpTallinnButton(tallinnButton, mainHeader);
        setVisualStyle(light, footer, workspace, venueNavbar, mainHeader);
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

    private void setUpMap() {
        map = new LeafletMap();
        map.setWidthFull();
        map.addMarkerClickListener(e -> {
            map.panToLocation(e.getMarker());
            generateVenuePopUp(getVenueByName(e.getMarker().getName()));
        });
        map.addMarkersAndZoom(service.getAll());
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

    private void showTallinnText() {
        H3 title = new H3("In case you've found your way to Tallinn...");
        Span subtitle = new Span("There's even more burger spots here!");
        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, ok);
        titleLayout.setPadding(false);

        Dialog introDialog = new Dialog(titleLayout);
        introDialog.open();
        ok.addClickListener(e -> introDialog.close());
    }


    void generateVenuePopUp(Venue burgerSpot) {

        H3 title = new H3(burgerSpot.getName());

        HorizontalLayout imageRow = new HorizontalLayout();
        List<String> imageLinks = new ArrayList<>();
        List<Item> images = burgerParser.getBurgerImageData(burgerSpot.getId());

        if (images != null) {
            for (Item image : images) {
                Checkin zeroCheckin = new Checkin();
                zeroCheckin.setCreatedAt(0);
                if (image.getCheckin() == null) image.setCheckin(zeroCheckin);
            }

            images.sort((o1, o2) -> o2.getCheckin().getCreatedAt().compareTo(o1.getCheckin().getCreatedAt()));
            addImages(images, imageLinks, imageRow);
        } else {
            Notification notification = new Notification("Faulty JSON, moving on...", 3000);
            add(notification);
            notification.open();
        }

        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        Span subtitle = new Span("No new burgers reported...");
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
            String burgerImageLink = burgerRecognizer.postImages(imageLinks);
            System.out.println("burgerimageLink: " + burgerImageLink);
            if (burgerImageLink.contains("https")) {
                Image image = new Image();
                image.setSrc(burgerImageLink);
                imageRow.add(image);
            } else {
                Span subtitle = new Span("No new burgers reported...");
                imageRow.add(subtitle);
            }
        }

    }

    public void refreshWithNewData(MainHeader mainHeader) {
        boolean showBurgers = mainHeader.showBurgers;
        boolean showRestaurants = mainHeader.showRestaurants;
        venueNavbar.removeAll();
        center.removeAll();
        content.removeAll();
        map.removeMarkers();
        venueNavbar = new VenueNavbar(service);
        burgerSpots = new ArrayList<>();

        if (showBurgers && showRestaurants) {
            List<Venue> allSpots = new ArrayList<>();
            allSpots.addAll(burgerParser.getBurgerspots(BURGER_QUERY, isSetToTallinn));
            allSpots.addAll(burgerParser.getBurgerspots(AMERICAN_RESTAURANT_QUERY, isSetToTallinn));
            burgerSpots = allSpots;
        }
        if (showBurgers && !showRestaurants) burgerSpots = burgerParser.getBurgerspots(BURGER_QUERY, isSetToTallinn);
        if (showRestaurants && !showBurgers)
            burgerSpots = burgerParser.getBurgerspots(AMERICAN_RESTAURANT_QUERY, isSetToTallinn);

        // if (!isSetToTallinn) map.setMiddle();

        venueNavbar.setUpNavBar(map, burgerSpots, this);
        venueNavbar.setMaxWidth("20%");
        venueNavbar.setMaxHeight("700px");
        center.add(venueNavbar, content);
        center.setMaxHeight("65%");
        content.setHeight("700px");
        content.add(map);



    }

    private void setUpTallinnButton(Button tallinnButton, MainHeader mainHeader) {
        tallinnButton.addClickListener(e -> {

            if (!isSetToTallinn) {
                MapLocation tallinnLocation = new MapLocation(59.436962, 24.753574, "Tallinn");
                map.panToLocation(tallinnLocation);
                switchToTallinn(mainHeader);
                tallinnButton.setText("Go back");
            } else {
                MapLocation tartuLocation = new MapLocation(58.378025, 26.728493, "Tallinn");
                switchToTartu(mainHeader);
                map.panToLocation(tartuLocation);
                tallinnButton.setText("Try Tallinn?");

            }
        });
    }

    private void setUpDarkLightSwitchButton(Button darkModeButton, HorizontalLayout footer, VerticalLayout workspace, MainHeader mainHeader) {
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
    }

    private void switchToTallinn(MainHeader mainHeader) {
        isSetToTallinn = true;
        showTallinnText();
        refreshWithNewData(mainHeader);
    }

    private void switchToTartu(MainHeader mainHeader) {
        isSetToTallinn = false;

        refreshWithNewData(mainHeader);
    }

    private void setVisualStyle(boolean light, HorizontalLayout footer, VerticalLayout workspace, VenueNavbar venueNavbar, MainHeader header) {
        ThemeList themeList = UI.getCurrent().getElement().getThemeList();

        if (light) {

            footer.getStyle().set("background-color", DARK_BACKGROUND_COLOR);
            workspace.getStyle().set("background-color", DARK_BACKGROUND_COLOR);
            themeList.set(Material.LIGHT, true);
            themeList.set(Material.DARK, false);
            map.setDarkMode();

        } else {

            footer.getStyle().set("background-color", LIGHT_BACKGROUND_COLOR);
            workspace.getStyle().set("background-color", LIGHT_BACKGROUND_COLOR);
            themeList.set(Material.DARK, true);
            themeList.set(Material.LIGHT, false);
            map.setLightMode();

        }
        workspace.getStyle().set("font-family", "Courier");
        venueNavbar.getElement().getStyle().set("font-family", "Courier");
    }

}


