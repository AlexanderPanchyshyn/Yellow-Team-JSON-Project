package tojsontests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yellowteam.mapper.JavaJsonMapper;
import org.yellowteam.models.Author;
import org.yellowteam.models.Book;
import org.yellowteam.models.BookShelf;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ToJsonTests {
    JavaJsonMapper mapper = new JavaJsonMapper();

    @Test
    @DisplayName("Two authors created and formatted to Json.")
    void givenTwoAuthorsWhenConvertingUsingMapperToJsonThanGetOutputInJsonFormat() throws IllegalAccessException {
        //Given
        Author author1 = new Author("John Doe", 56, LocalDateTime.of(1953, 10, 20, 10, 5));
        Author author2 = new Author("Rebekka Mayor", 37, LocalDateTime.of(1985, 5, 4, 8, 10));

        //When
        var jsonAuthor = mapper.toJson(author1);
        var jsonAuthor2 = mapper.toJson(author2);

        //Then
        assertThat(jsonAuthor).isEqualTo("{\"name\":\"John Doe\",\"age\":56,\"dateOfBirth\":\"1953-10-20T10:05\"}");
        assertThat(jsonAuthor2).isEqualTo(new JavaJsonMapper().toJson(author2));
        System.out.println(jsonAuthor);
    }

    @Test
    @DisplayName("Three books and two authors created and formatted to Json.")
    void givenThreeBooksWhenConvertingUsingMapperToJsonThanGetOutputInJsonFormat() throws IllegalAccessException {
        //Given
        Author author1 = new Author("Ivan Franko", 56, LocalDateTime.of(1933, 10, 20, 10, 5));
        Author author2 = new Author("Oleg Tichina", 37, LocalDateTime.of(1921, 5, 4, 8, 10));

        Book book1 = new Book("Blade Runner", 2022, Arrays.asList("Marley", "Bob"), true, author1);
        Book book2 = new Book("Bible", 1024, Arrays.asList("God", "Jesus", "Messiah"), false, author2);
        Book book3 = new Book("Mexico", 2022, Arrays.asList("Black", "Jack"), true, author1);

        //When
        var jsonBook = mapper.toJson(book1);
        var jsonBook2 = mapper.toJson(book2);
        var jsonBook3 = mapper.toJson(book3);

        //Then
        assertThat(jsonBook).isEqualTo(new JavaJsonMapper().toJson(book1));
        assertThat(jsonBook2).isEqualTo("{\"title\":\"Bible\",\"year\":1024,\"characters\":[\"God\",\"Jesus\",\"Messiah\"],\"isOriginalEdition\":false,\"author\":{\"name\":\"Oleg Tichina\",\"age\":37,\"dateOfBirth\":\"1921-05-04T08:10\"}}");
        assertThat(jsonBook2).isNotEqualTo(jsonBook3);
        System.out.println(jsonBook);
        System.out.println(jsonBook2);
        System.out.println(jsonBook3);
        System.out.println("-------------------------------------");
    }

    @Test
    @DisplayName("One bookshelf with two books and with two authors created and formatted to Json.")
    void givenTwoBookShelfWhenConvertingUsingMapperToJsonThanGetOutputInJsonFormat() throws IllegalAccessException {
        //Given
        Author author1 = new Author("John Does", 42, LocalDateTime.of(1975, 4, 20, 10, 5));
        Author author2 = new Author("Ivan Skoropadski", 64, LocalDateTime.of(1966, 2, 1, 8, 10));

        Book book1 = new Book("Ognivo", 2022, Arrays.asList("Jack", "Chelsey", "Abraham"), true, author1);
        Book book2 = new Book("Truth", 1024, Arrays.asList("God", "Jesus"), false, author2);

        BookShelf bookShelf = new BookShelf(20, 50, Arrays.asList(book1, book2));

        //When
        var bookshelf = mapper.toJson(bookShelf);

        //Then
        assertThat(bookshelf).isEqualTo("{\"width\":20,\"height\":50,\"books\":[{\"title\":\"Ognivo\",\"year\":2022,\"characters\":[\"Jack\",\"Chelsey\",\"Abraham\"],\"isOriginalEdition\":true,\"author\":{\"name\":\"John Does\",\"age\":42,\"dateOfBirth\":\"1975-04-20T10:05\"}},{\"title\":\"Truth\",\"year\":1024,\"characters\":[\"God\",\"Jesus\"],\"isOriginalEdition\":false,\"author\":{\"name\":\"Ivan Skoropadski\",\"age\":64,\"dateOfBirth\":\"1966-02-01T08:10\"}}]}");
        System.out.println(bookshelf);
        System.out.println("-------------------------------------");
    }

    @Test
    void givenUglyJsonFileWhenUsingPrettifyingFormatterThanReceivePrettyJson() throws IllegalAccessException {
        //Given
        Author author1 = new Author("John Does", 42, LocalDateTime.of(1975, 4, 20, 10, 5));
        Author author2 = new Author("Ivan Skoropadski", 64, LocalDateTime.of(1966, 2, 1, 8, 10));

        Book book1 = new Book("Ognivo", 2022, Arrays.asList("Jack", "Chelsey", "Abraham"), true, author1);
        Book book2 = new Book("Truth", 1024, Arrays.asList("God", "Jesus"), false, author2);

        BookShelf bookShelf = new BookShelf(20, 50, Arrays.asList(book1, book2));

        //When
        var bookshelf = mapper.toJson(book1);
        String pretty = mapper.prettifyJsonToReadableView(mapper.toJson(book1), 2);

        //Then
        System.out.println(pretty);
//        System.out.println(mapper.prettifyJsonToReadableViewOld(mapper.toJson(book1), 2));
    }
}
