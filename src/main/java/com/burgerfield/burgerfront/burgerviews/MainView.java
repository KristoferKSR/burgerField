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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
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
@PageTitle("Burgerfield v0.95")
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

        content = new VerticalLayout();
        center = new HorizontalLayout();
        HorizontalLayout footer = new HorizontalLayout();
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

    //currently unused, might implement
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
            System.out.println("burgerimageLink: " + burgerImageLink);
            if (burgerImageLink.contains("https")) {
                Image image = new Image();
                image.setSrc(burgerImageLink);
                String date = getImagePostedDateByLink(images, burgerImageLink);
                Span subtitle = new Span("Latest picture of a burger at this venue:");
                if (date != null) subtitle = new Span("Latest picture of a burger at this venue ("+date+"):");
                imageHolder.add(subtitle, image);
            } else {
                Span subtitle = new Span("No new burgers reported...");
                imageHolder.add(subtitle);
            }
        }

    }

    private String getImagePostedDateByLink(List<Item> images, String burgerImageLink) {
        //A slightly wasteful way to check if the image has a date with it
        for (Item image : images) {
            if (image.getCheckin() != null && image.getCreatedAt() > 0 && burgerImageLink.contains(image.getSuffix())) {
                long milliSeconds = image.getCreatedAt() * 1000L;
                System.out.println(milliSeconds);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm", new Locale("ee", "EE"));
                return formatter.format(new Date(milliSeconds));
            }
        } return null;
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

    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("rel", "shortcut icon");
        attributes.put("type", "image/png");
        initialPageSettings.addLink("icons/icon.png", attributes);
    }

}


