package com.burgerfield.burgerfront.burgerviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class SiteFooter extends HorizontalLayout {

    Button visualStyleSwitchButton;
    Button tallinnButton;

    public Button getVisualStyleSwitchButton() {
        return visualStyleSwitchButton;
    }

    public Button getTallinnButton() {
        return tallinnButton;
    }

    public SiteFooter() {
        visualStyleSwitchButton = new Button("Switch to dark mode");
        tallinnButton = new Button("Try tallinn?");
        tallinnButton.getStyle().set("font-family", "Courier");
        visualStyleSwitchButton.getStyle().set("font-family", "Courier");

        HorizontalLayout footerSpacer = new HorizontalLayout();
        footerSpacer.setWidth("76%");
        add(visualStyleSwitchButton, footerSpacer, tallinnButton);
    }



}
