package org.yellowteam.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter @Builder
public class Book {
    @JsonProperty("Title")
    private String title;
    @JsonProperty("Year")
    private int year;
    @JsonProperty("Characters")
    private List<String> characters;
    @JsonProperty("IsOriginalEdition")
    private boolean isOriginalEdition;
    @JsonProperty("Author")
    private Author author;
}
