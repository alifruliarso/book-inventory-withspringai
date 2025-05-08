package com.galapea.techblog.base.ui.view;

import com.galapea.techblog.base.ui.component.ViewToolbar;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

/**
 * This view shows up when a user navigates to the root ('/') of the
 * application.
 */
@Route
public final class MainView extends Main {

    // TODO Replace with your own main view.

    MainView() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new ViewToolbar("Main"));
        add(new Span());
        var title = new H1("Welcome to the Book Inventory");
        title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT, AlignItems.CENTER);
        Div titleDiv = new Div(title);
        titleDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.MEDIUM);
        add(titleDiv);
    }

    /**
     * Navigates to the main view.
     */
    public static void showMainView() {
        UI.getCurrent().navigate(MainView.class);
    }
}
