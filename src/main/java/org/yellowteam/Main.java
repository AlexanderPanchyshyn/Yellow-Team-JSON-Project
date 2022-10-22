package org.yellowteam;

import org.yellowteam.mapper.JavaJsonMapper;
import org.yellowteam.models.Author;
import org.yellowteam.models.Book;
import org.yellowteam.models.BookShelf;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        Author author1 = new Author("John Doe", 56);
        Author author2 = new Author("Rebekka Mayor", 37);

        Book book1 = new Book("Blade Runner", 2022, Arrays.asList("Marley", "Bob"), true, author1);
        Book book2 = new Book("Bible", 1024, Arrays.asList("God", "Jesus"), false, author2);

        BookShelf bookShelf = new BookShelf(20, 50, Arrays.asList(book1, book2));

        JavaJsonMapper mapper = new JavaJsonMapper();
        var jsonBook = mapper.toJson(bookShelf);
        System.out.println(bookShelf.getClass().toString());

//        System.out.println(jsonBook);
    }
}

