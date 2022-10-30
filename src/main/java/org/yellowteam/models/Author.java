package org.yellowteam.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class Author {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Age")
    private int age;
    @JsonProperty("DateOfBirth")
    private LocalDateTime dateOfBirth;
}
