package org.yellowteam.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class Author {
    private String name;
    private int age;
    private LocalDateTime dateOfBirth;
}
