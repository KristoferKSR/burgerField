package com.burgerfield.burgerfront.burgerviews;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SiteHeader extends HorizontalLayout {

    boolean showBurgers;
    boolean showRestaurants;
    H3 mainTitle;
    HorizontalLayout titleHeader;

    public void setTitleHeader(HorizontalLayout titleHeader) {
        this.titleHeader = titleHeader;
    }

    public H3 getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(H3 mainTitle) {
        this.mainTitle = mainTitle;
    }

    public SiteHeader() {

    }

    public boolean isShowBurgers() {
        return showBurgers;
    }

    public boolean isShowRestaurants() {
        return showRestaurants;
    }

    void setUpHeader(MainView mainView, boolean isSetToTallinn) {

        HorizontalLayout titleSpacer = new HorizontalLayout();
        titleHeader = new HorizontalLayout();
        HorizontalLayout spacingHeader = new HorizontalLayout();
        HorizontalLayout checkBoxHeader = setCheckboxes(mainView, isSetToTallinn);
        spacingHeader.setWidth("76%");
        setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
                titleHeader);
        setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
                checkBoxHeader);
        //checkBoxHeader.setWidth("20%");


        mainTitle = new H3("Burgerfield v1.0");

        titleHeader.add(mainTitle);
        add(titleSpacer, titleHeader, spacingHeader, checkBoxHeader);
    }

    HorizontalLayout setCheckboxes(MainView mainView, boolean isSetToTallinn) {

        HorizontalLayout checkBoxHolder = new HorizontalLayout();
        Checkbox burgerSpotCheckbox = new Checkbox();
        burgerSpotCheckbox.setLabel("Burger spots");
        burgerSpotCheckbox.setValue(true);
        checkBoxHolder.add(burgerSpotCheckbox);

        showBurgers = true;
        showRestaurants = false;

        burgerSpotCheckbox.addValueChangeListener(event -> {
            showBurgers = burgerSpotCheckbox.getValue();
            mainView.refreshWithNewData(this);
        });

        Checkbox restaurantCheckBox = new Checkbox();
        restaurantCheckBox.setLabel("American restaurants");
        restaurantCheckBox.setValue(false);

        restaurantCheckBox.addValueChangeListener(event -> {
            showRestaurants = restaurantCheckBox.getValue();
            mainView.refreshWithNewData(this);

        });

        checkBoxHolder.add(restaurantCheckBox);

        return checkBoxHolder;
    }


}
