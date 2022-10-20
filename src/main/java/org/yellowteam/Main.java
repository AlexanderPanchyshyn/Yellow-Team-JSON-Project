package org.yellowteam;

import com.google.gson.JsonObject;
import org.yellowteam.mapper.JavaJsonMapper;
import org.yellowteam.models.Book;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        Book book = new Book("Blade Runner");
        JavaJsonMapper mapper = new JavaJsonMapper();
//        mapper.writeValue(new File("target/car.json"), book);
        JsonObject jsonObject = new JsonObject();
    }
}