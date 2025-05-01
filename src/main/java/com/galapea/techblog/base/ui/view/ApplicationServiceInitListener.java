package com.galapea.techblog.base.ui.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(beforeEnterEvent -> {
                if (beforeEnterEvent.isErrorEvent()) {
                    log.info("------< Error navigate to: " + beforeEnterEvent.getLocation().getPath() + " >------");
                    log.info("Forward to MainView...");
                    beforeEnterEvent.forwardTo(MainView.class);
                }
            });
        });

        event.getSource().addSessionInitListener(sessionInitEvent -> {
            sessionInitEvent.getSession().setErrorHandler(new CustomErrorHandler());
        });
    }

}
