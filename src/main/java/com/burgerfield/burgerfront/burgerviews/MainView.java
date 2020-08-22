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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
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

    public void setBurgerSpots(List<Venue> burgerSpots) {
        this.burgerSpots = burgerSpots;
    }

    @Autowired
    public MainView(MapLocationService service) {

        this.service = service;
        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();
        setSizeFull();

        // Create the map and add it to this view
        map = new LeafletMap();
        map.setWidthFull();
        map.addMarkerClickListener(e -> {
            map.panToLocation(e.getMarker());
            showPopup(getVenueByName(e.getMarker().getName()));
        });
        map.addMarkersAndZoom(service.getAll());

        MainHeader mainHeader = new MainHeader();
        mainHeader.setUpHeader(this);
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
        footer.setPadding(true);

        // Compose layout
        center.setFlexGrow(1, venueNavbar);
        workspace.add(mainHeader, center, footer);
        showIntro();
        add(workspace);

        ThemeList themeList = UI.getCurrent().getElement().getThemeList();
        themeList.set(Lumo.LIGHT, true);

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

        if (showBurgers && showRestaurants) {
            List<Venue> allSpots = new ArrayList<>();
            allSpots.addAll(burgerParser.getBurgerspots(BURGER_QUERY));
            allSpots.addAll(burgerParser.getBurgerspots(RESTAURANT_QUERY));
            burgerSpots = allSpots;
        }
        if (showBurgers && !showRestaurants) burgerSpots = burgerParser.getBurgerspots(BURGER_QUERY);
        if (showRestaurants && !showBurgers)  burgerSpots = burgerParser.getBurgerspots(RESTAURANT_QUERY);

        map.setMiddle();
        venueNavbar.setUpNavBar(map, burgerSpots, this);
        venueNavbar.setWidth("300px");
        venueNavbar.setMaxHeight("800px");
        center.add(venueNavbar, content);
        center.setMaxHeight("800px");
        content.setHeight("800px");
        content.add(map);

    }
}


