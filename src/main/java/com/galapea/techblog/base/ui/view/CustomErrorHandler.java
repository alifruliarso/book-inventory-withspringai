package com.galapea.techblog.base.ui.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.ErrorHandlerUtil;

public class CustomErrorHandler implements ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void error(ErrorEvent errorEvent) {
        log.error("Error occurred", errorEvent.getThrowable());
        boolean redirected = ErrorHandlerUtil.handleErrorByRedirectingToErrorView(errorEvent.getThrowable());

        if (!redirected) {
            if (UI.getCurrent() != null) {
                UI.getCurrent().getPage().executeJs("window.location.href = '/';");
            } else {
                log.error("UI is not available, cannot redirect to error page.");
            }
        }
    }

}
