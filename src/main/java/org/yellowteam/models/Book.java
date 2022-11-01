package org.yellowteam.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
public class Book {
    @JsonElement(name = "title")
    private String title;
    @JsonElement(name = "year")
    private int year;
    @JsonElement(name = "characters")
    private List<String> characters;
    @JsonElement(name = "isOriginalEdition")
    private boolean isOriginalEdition;
    @JsonElement(name = "author")
    private Author author;

}
