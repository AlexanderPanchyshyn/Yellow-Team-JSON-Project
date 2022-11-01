package org.yellowteam.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class Author {
    @JsonElement(name = "name")
    private String name;
    @JsonElement(name = "age")
    private int age;
    @JsonElement(name = "dateOfBirth")
    private LocalDateTime dateOfBirth;
}
