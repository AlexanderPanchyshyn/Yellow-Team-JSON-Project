package org.yellowteam.models;

import lombok.*;
import org.yellowteam.annotations.JsonElement;

import java.util.List;

@AllArgsConstructor
@Getter @Builder
public class BookShelf {
    @JsonElement(name = "width")
    private int width;
    @JsonElement(name = "height")
    private int height;
    @JsonElement(name = "books")
    private List<Book> books;
}
