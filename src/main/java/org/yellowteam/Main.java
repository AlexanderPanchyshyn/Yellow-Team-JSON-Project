package org.yellowteam;

import org.yellowteam.mapper.JavaJsonMapper;
import org.yellowteam.models.Book;
import org.yellowteam.models.BookShelf;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        Book book1 = new Book("Blade Runner", 2022, Arrays.asList("Marley", "Bob"), true);
        Book book2 = new Book("Bible", 1024, Arrays.asList("God", "Jesus"), false);
        BookShelf bookShelf = new BookShelf(20, 50, Arrays.asList(book1, book2));
        JavaJsonMapper mapper = new JavaJsonMapper();
        var jsonBook = mapper.toJson(bookShelf);
        System.out.println(jsonBook);
    }
}

