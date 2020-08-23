package com.burgerfield.burgerfront.burgerviews;

import com.burgerfield.burgerJSON.BurgerParser;
import com.burgerfield.burgerJSON.BurgerRecognizer;
import com.burgerfield.burgerfront.LeafletMap;
import com.burgerfield.burgerfront.MapLocation;
import com.burgerfield.burgerfront.MapLocationService;
import com.burgerfield.objects.burgerphoto.Checkin;
import com.burgerfield.objects.burgervenue.Venue;
import com.burgerfield.objects.burgerphoto.Item;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.*;
import com.vaadin.flow.theme.material.Material;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Route
@PageTitle("Burgerfield v1.0")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@BodySize(height = "100vh", width = "100vw")
public class MainView extends Div implements PageConfigurator {

    //Currently the messiest class, needs some refactoring
    private final MapLocationService service;
    BurgerParser burgerParser = new BurgerParser();
    BurgerRecognizer burgerRecognizer = new BurgerRecognizer();
    List<Venue> burgerSpots = new ArrayList<>();

    //Queries for Foursquare, this is the categoryId
    private static final String BURGER_QUERY = "4bf58dd8d48988d16c941735";
    private static final String AMERICAN_RESTAURANT_QUERY = "4bf58dd8d48988d14e941735";
    private static final String FOOD_TRUCK_QUERY = "4bf58dd8d48988d1cb941735";
    private static final String ALL_RESTAURANT_QUERY = "4d4b7105d754a06374d81259";

    private static final String DARK_BACKGROUND_COLOR = "#000000";
    private static final String LIGHT_BACKGROUND_COLOR = "#35b865";
    private static final String DARK_FOREGROUND_COLOR = "#171717";
    private static final String LIGHT_FOREGROUND_COLOR = "#ffffff";

    NavigationBar navigationBar;
    LeafletMap map;
    VerticalLayout mapHolder;
    SiteHeader siteHeader;
    HorizontalLayout center;
    SiteFooter footer;
    VerticalLayout appHolder;
    HorizontalLayout workspace;
    boolean visualStyle = true;
    boolean isSetToTallinn = false;
    private boolean vertical;

    public void setBurgerSpots(List<Venue> burgerSpots) {
        this.burgerSpots = burgerSpots;
    }

    @Autowired
    public MainView(MapLocationService service) {

        this.service = service;
        burgerParser.getBurgerspots(BURGER_QUERY, isSetToTallinn);
        workspace = new HorizontalLayout();
        appHolder = new VerticalLayout();
        // Create the map and add it to this view
        workspace.add(appHolder);
        setUpMap();

        siteHeader = new SiteHeader();
        siteHeader.setUpHeader(this, isSetToTallinn);
        //mainHeader.setPadding(true);
        mapHolder = new VerticalLayout();
        center = new HorizontalLayout();
        footer = new SiteFooter();
        navigationBar = new NavigationBar(service);
        refreshWithNewData(siteHeader);

        appHolder.add(siteHeader, center, footer);
        showIntro();
        add(workspace);


        setUpDarkLightSwitchButton(footer.getVisualStyleSwitchButton());
        setUpTallinnButton(footer.getTallinnButton(), siteHeader);
        // setUpUIVisuals(visualStyle);
    }

    //currently unused, might implement
    public boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

    private void setUpMap() {
        map = new LeafletMap();
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
        H3 title = new H3("Welcome to the Burgerfield app");
        Span subtitle = new Span("You can find the newest burgers from your favourite burger spots by...");
        Span subtitleA = new Span("a) Clicking on an icon on the map");
        Span subtitleB = new Span("b) Clicking on the name in the navbar");
        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, subtitleA, subtitleB, ok);
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
        VerticalLayout imageHolder = new VerticalLayout();
        List<String> imageLinks = new ArrayList<>();
        List<Item> images = burgerParser.getBurgerImageData(burgerSpot.getId());

        if (images != null) {
            for (Item image : images) {
                Checkin zeroCheckin = new Checkin();
                zeroCheckin.setCreatedAt(0);
                if (image.getCheckin() == null) image.setCheckin(zeroCheckin);
            }

            images.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
            setImage(images, imageLinks, imageHolder);
        } else {
            //incase there's an error in the JSON, sometimes happens with some Tallinn spots
            Notification notification = new Notification("Faulty JSON, moving on...", 3000);
            add(notification);
            notification.open();
        }

        Button ok = new Button("OK!", VaadinIcon.CHECK.create());
        Span subtitle = new Span("No new burgers reported...");
        VerticalLayout titleLayout = new VerticalLayout(title, subtitle, ok);

        if (imageLinks.size() > 0) titleLayout = new VerticalLayout(title, imageHolder, ok);

        titleLayout.setPadding(false);
        Dialog popupDialog = new Dialog(titleLayout);
        popupDialog.open();

        ok.addClickListener(e -> popupDialog.close());

    }

    private void setImage(List<Item> images, List<String> imageLinks, VerticalLayout imageHolder) {

        for (Item testImage : images) {
            //constructing the image links for the Qminder API
            String imageLinkString = testImage.getPrefix() + "300x300" + testImage.getSuffix();
            imageLinks.add(imageLinkString);
        }
        if (imageLinks.size() > 0) {
            String burgerImageLink = burgerRecognizer.postImages(imageLinks);
            if (burgerImageLink.contains("https")) {
                Image image = new Image();
                image.setSrc(burgerImageLink);
                String date = burgerRecognizer.getImagePostedDateByLink(images, burgerImageLink);
                Span subtitle = new Span("Latest picture of a burger at this venue:");
                if (date != null) subtitle = new Span("Latest picture of a burger at this venue (" + date + "):");
                imageHolder.add(subtitle, image);
            } else {
                Span subtitle = new Span("No new burgers reported...");
                imageHolder.add(subtitle);
            }
        }

    }



    public void refreshWithNewData(SiteHeader siteHeader) {
        boolean showBurgers = siteHeader.showBurgers;
        boolean showRestaurants = siteHeader.showRestaurants;
        navigationBar.removeAll();
        center.removeAll();
        mapHolder.removeAll();
        map.removeMarkers();
        navigationBar = new NavigationBar(service);
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
        navigationBar.setUpNavBar(map, burgerSpots, this);
        center.add(navigationBar, mapHolder);
        mapHolder.add(map);
        setUpUIVisuals(visualStyle);


    }

    private void setUpTallinnButton(Button tallinnButton, SiteHeader siteHeader) {
        tallinnButton.addClickListener(e -> {

            if (!isSetToTallinn) {
                MapLocation tallinnLocation = new MapLocation(59.436962, 24.753574, "Tallinn");
                map.panToLocation(tallinnLocation);
                switchToTallinn(siteHeader);
                tallinnButton.setText("Go back");
            } else {
                MapLocation tartuLocation = new MapLocation(58.378025, 26.728493, "Tallinn");
                switchToTartu(siteHeader);
                map.panToLocation(tartuLocation);
                tallinnButton.setText("Try Tallinn?");

            }
        });
    }

    private void setUpDarkLightSwitchButton(Button darkModeButton) {
        darkModeButton.addClickListener(e -> {
            if (visualStyle) {
                visualStyle = false;
                darkModeButton.setText("Switch to light mode");
            } else {
                visualStyle = true;
                darkModeButton.setText("Switch to dark mode");
            }
            setUpUIVisuals(visualStyle);

        });
    }

    private void switchToTallinn(SiteHeader siteHeader) {
        isSetToTallinn = true;
        showTallinnText();
        refreshWithNewData(siteHeader);
    }

    private void switchToTartu(SiteHeader siteHeader) {
        isSetToTallinn = false;

        refreshWithNewData(siteHeader);
    }

    private void setUpUIVisuals(boolean light) {
        //Condensed visuals/css into one method
        workspace.setBoxSizing(BoxSizing.BORDER_BOX);
        workspace.setSizeFull();
        workspace.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        workspace.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
                appHolder);

        footer.getStyle().set("padding", "20px");
        footer.getStyle().set("padding-right", "10px");
        footer.getStyle().set("padding-left", "30px");

        siteHeader.getStyle().set("padding", "20px");
        appHolder.setHeightFull();
        siteHeader.setWidthFull();
        footer.setWidthFull();
        workspace.getStyle().set("padding", "50px");
        navigationBar.setWidth("20%");
        mapHolder.setWidth("80%");
        center.setSizeFull();
        center.setMaxHeight("600px");
        map.setSizeFull();
        ThemeList themeList = UI.getCurrent().getElement().getThemeList();


        if (light) {

            workspace.getStyle().set("background-color", LIGHT_BACKGROUND_COLOR);
            appHolder.getStyle().set("background-color", LIGHT_FOREGROUND_COLOR);

            themeList.set(Material.LIGHT, true);
            themeList.set(Material.DARK, false);
            map.setDarkMode();

        } else {

            workspace.getStyle().set("background-color", DARK_BACKGROUND_COLOR);
            appHolder.getStyle().set("background-color", DARK_FOREGROUND_COLOR);

            themeList.set(Material.DARK, true);
            themeList.set(Material.LIGHT, false);
            map.setLightMode();

        }
        workspace.getStyle().set("font-family", "Courier");
        navigationBar.getStyle().set("font-family", "Courier");

    }

    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("rel", "shortcut icon");
        attributes.put("type", "image/png");
        initialPageSettings.addLink("icons/icon.png", attributes);
    }

}


