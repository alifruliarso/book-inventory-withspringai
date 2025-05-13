package com.galapea.techblog.bookinventory.ui.view;

import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.service.BookService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("book-detail")
@PageTitle("Book Detail")
public class BookDetailView extends VerticalLayout implements HasUrlParameter<String> {
    private final BookService bookService;
    private FormLayout content;
    private String bookId;

    private Button fetchGenreBtn;
    private ProgressBar progressBar;
    private NativeLabel progresLabel;
    private Button fetchSummaryBtn;

    public BookDetailView(BookService bookService) {
        this.bookService = bookService;
        Button backButton = new Button("Back", e -> getUI().ifPresent(ui -> ui.navigate("book-list")));
        add(backButton);
        content = new FormLayout();
        content.addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        add(content);

        progresLabel = new NativeLabel();
        progresLabel.setId("pblabel");
        progresLabel.addClassName(LumoUtility.TextColor.SECONDARY);
        progresLabel.setVisible(false);
        add(progresLabel);

        progressBar = new ProgressBar();
        progressBar.getElement().setAttribute("aria-labelledby", "pblabel");
        progressBar.setVisible(false);
        add(progressBar);
        fetchGenreBtn = new Button("Update Genre by AI");
        fetchGenreBtn.setDisableOnClick(true);
        fetchGenreBtn.addClickListener(e -> {
            var ui = UI.getCurrent();
            Book book = getCurrentBook();
            progresLabel.setVisible(true);
            progresLabel.setText("Asking AI for " + book.title() + "...");
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            bookService.asyncGenerateGenre(book.id(), ui.accessLater(this::onJobCompleted, null),
                    ui.accessLater(progressBar::setValue, null), ui.accessLater(this::onJobFailed, null));
        });
        fetchGenreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fetchGenreBtn.getStyle().set("cursor", "pointer");
        add(fetchGenreBtn);
        fetchSummaryBtn = new Button("Update Summary by AI");
        fetchSummaryBtn.setDisableOnClick(true);
        fetchSummaryBtn.addClickListener(e -> {
            var ui = UI.getCurrent();
            Book book = getCurrentBook();
            progresLabel.setVisible(true);
            progresLabel.setText("Asking AI for " + book.title() + "...");
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            bookService.asyncGenerateSummary(book.id(), ui.accessLater(this::onJobCompleted, null),
                    ui.accessLater(progressBar::setValue, null), ui.accessLater(this::onJobFailed, null));
        });
        fetchSummaryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fetchSummaryBtn.getStyle().set("cursor", "pointer");
        add(fetchSummaryBtn);
    }

    private void onJobCompleted(String result) {
        Notification.show("Success ");
        fetchGenreBtn.setEnabled(true);
        progressBar.setVisible(false);
        progressBar.setValue(0);
        progresLabel.setVisible(false);
        setParameter(null, result);
    }

    private void onJobFailed(Exception error) {
        Notification.show("Update failed: " + error.getMessage());
        fetchGenreBtn.setEnabled(true);
        progressBar.setVisible(false);
        progressBar.setValue(0);
        progresLabel.setVisible(false);
    }

    private Book getCurrentBook() {
        if (bookId != null) {
            return bookService.getBook(bookId);
        }
        if (content.getChildren().findFirst().isPresent()) {
            return bookService.listBooks().stream()
                    .filter(b -> b.id() != null
                            && content.getChildren().anyMatch(c -> c.getElement().getText().contains(b.title())))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        content.removeAll();
        Book book = bookService.getBook(parameter);
        if (book == null) {
            content.add(new Text("Book not found."));
        } else {
            bookId = book.id();
            content.addFormItem(new Text(book.title()), "Title");
            content.addFormItem(new Text(book.authors()), "Authors");
            content.addFormItem(new Text(book.publisher()), "Publisher");
            content.addFormItem(new Text(book.rating() != null ? book.rating().toString() : "-"), "Rating");
            content.addFormItem(new Text(book.goodreadsBookId() != null ? book.goodreadsBookId().toString() : "-"),
                    "Goodreads ID");
            content.addFormItem(new Text(book.genres() != null ? book.genres() : "-"), "Genres");
            content.addFormItem(new Text(book.summary() != null ? book.summary() : "-"), "Summary");
            if (book.goodreadsBookId() != null) {
                Anchor goodreadsLink = new Anchor(book.goodreadsUrl(), book.goodreadsUrl());
                goodreadsLink.setTarget("_blank");
                content.addFormItem(goodreadsLink, "Source URL");
            } else {
                content.addFormItem(new Text("-"), "Source URL");
            }
            content.setResponsiveSteps(new ResponsiveStep("0", 1));
        }
    }
}
