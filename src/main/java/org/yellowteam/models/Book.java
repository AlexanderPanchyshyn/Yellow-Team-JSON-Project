package org.yellowteam.models;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter @Builder
public class Book {
    private String title;
    private int year;
//    private List<String> characters;
}
