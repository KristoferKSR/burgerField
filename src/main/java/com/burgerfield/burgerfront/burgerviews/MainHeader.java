package com.burgerfield.burgerfront.burgerviews;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainHeader extends HorizontalLayout {

    boolean showBurgers;
    boolean showRestaurants;

    public MainHeader() {

    }

    public boolean isShowBurgers() {
        return showBurgers;
    }

    public boolean isShowRestaurants() {
        return showRestaurants;
    }


    void setUpHeader(MainView mainView) {

        HorizontalLayout titleSpacer = new HorizontalLayout();
        HorizontalLayout titleHeader = new HorizontalLayout();
        HorizontalLayout spacingHeader = new HorizontalLayout();
        HorizontalLayout checkBoxHeader = setCheckboxes(mainView);
        titleSpacer.setWidth("1%");
        titleHeader.setWidth("12%");
        spacingHeader.setWidth("69%");
        checkBoxHeader.setWidth("18%");
        H3 mainTitle = new H3("Burgerfield v0.7");


        titleHeader.add(mainTitle);
        add(titleSpacer, titleHeader, spacingHeader, checkBoxHeader);
    }

    HorizontalLayout setCheckboxes(MainView mainView) {

        HorizontalLayout checkBoxHolder = new HorizontalLayout();
        Checkbox burgerSpotCheckbox = new Checkbox();
        burgerSpotCheckbox.setLabel("Burger spots");
        burgerSpotCheckbox.setValue(true);
        checkBoxHolder.add(burgerSpotCheckbox);

        showBurgers = true;
        showRestaurants = false;

        burgerSpotCheckbox.addValueChangeListener(event -> {
            showBurgers = burgerSpotCheckbox.getValue();
            System.out.println(burgerSpotCheckbox.getValue());
            mainView.refreshWithNewData(showBurgers, showRestaurants);
        });

        Checkbox restaurantCheckBox = new Checkbox();
        restaurantCheckBox.setLabel("Restaurants");
        restaurantCheckBox.setValue(false);

        restaurantCheckBox.addValueChangeListener(event -> {
            System.out.println(restaurantCheckBox.getValue());
            showRestaurants = restaurantCheckBox.getValue();
            mainView.refreshWithNewData(showBurgers, showRestaurants);

        });

        checkBoxHolder.add(restaurantCheckBox);

        return checkBoxHolder;
    }



}
