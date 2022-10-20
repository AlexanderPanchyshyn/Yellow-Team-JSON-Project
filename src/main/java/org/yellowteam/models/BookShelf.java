package org.yellowteam.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter @Builder
public class BookShelf {
    private int width;
    private int height;
    private List<Book> books;
}
