package com.galapea.techblog.bookinventory.ui.view;

import com.galapea.techblog.base.ui.component.ViewToolbar;
import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.service.BookService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("book-list")
@PageTitle("Book List")
@Menu(order = 0, icon = "vaadin:book", title = "Book List")
public class BookListView extends Main {
    private final BookService bookService;
    private final Grid<Book> bookGrid;

    public BookListView(BookService bookService) {
        this.bookService = bookService;
        bookGrid = new Grid<>(Book.class, false);
        bookGrid.setItems(bookService.listBooks());
        bookGrid.setSizeFull();

        TextField titleFieldNew = new TextField();
        titleFieldNew.setPlaceholder("Title");
        TextField authorsFieldNew = new TextField();
        authorsFieldNew.setPlaceholder("Authors");
        TextField publisherFieldNew = new TextField();
        publisherFieldNew.setPlaceholder("Publisher");
        NumberField ratingFieldNew = new NumberField();
        ratingFieldNew.setPlaceholder("Rating");
        TextField genresFieldNew = new TextField();
        genresFieldNew.setPlaceholder("Genres");
        TextField summaryFieldNew = new TextField();
        summaryFieldNew.setPlaceholder("Summary");
        NumberField goodreadsIdFieldNew = new NumberField();
        goodreadsIdFieldNew.setPlaceholder("Goodreads ID");
        Button addBookBtn = new Button("Add Book", event -> {
            try {
                Book newBook = new Book(null, // id will be generated in BookService
                        titleFieldNew.getValue(), authorsFieldNew.getValue(), publisherFieldNew.getValue(),
                        ratingFieldNew.getValue(), genresFieldNew.getValue(), summaryFieldNew.getValue(),
                        goodreadsIdFieldNew.getValue() != null ? goodreadsIdFieldNew.getValue().longValue() : null);
                bookService.createBook(newBook);
                bookGrid.setItems(bookService.listBooks());
                titleFieldNew.clear();
                authorsFieldNew.clear();
                publisherFieldNew.clear();
                ratingFieldNew.clear();
                genresFieldNew.clear();
                summaryFieldNew.clear();
                goodreadsIdFieldNew.clear();
                Notification.show("Book added", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        addBookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout addBookLayout = new HorizontalLayout(titleFieldNew, authorsFieldNew, publisherFieldNew,
                ratingFieldNew, genresFieldNew, summaryFieldNew, goodreadsIdFieldNew, addBookBtn);
        addBookLayout.setWidthFull();
        addBookLayout.setAlignItems(Alignment.BASELINE);

        bookGrid.addComponentColumn(book -> {
            Anchor anchor = new Anchor("/book-detail/" + book.id(), book.title());
            anchor.getStyle().set("cursor", "pointer");
            return anchor;
        }).setHeader("Title").setAutoWidth(false).setFlexGrow(0).setWidth("500px").setFrozen(true);

        bookGrid.addColumn(Book::authors).setHeader("Authors").setAutoWidth(false).setFlexGrow(1).setWidth("200px");
        bookGrid.addColumn(Book::publisher).setHeader("Publisher").setAutoWidth(false).setFlexGrow(1).setWidth("250px");
        bookGrid.addColumn(Book::rating).setHeader("Rating").setTextAlign(ColumnTextAlign.END);
        bookGrid.addColumn(Book::genres).setHeader("Genres").setAutoWidth(false).setFlexGrow(1).setWidth("200px");
        bookGrid.addColumn(Book::summary).setHeader("Summary").setAutoWidth(false).setFlexGrow(1).setWidth("400px");

        bookGrid.addColumn(Book::goodreadsBookId).setHeader("Goodreads ID").setAutoWidth(false);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        add(new ViewToolbar("Book List"));
        // add(addBookLayout);
        add(bookGrid);
    }

    private void generateBookSummary(Book book) {
        bookService.generateSummary(book.id());
        bookGrid.getDataProvider().refreshAll();
    }
}
