package org.yellowteam;

import net.rationalminds.LocalDateModel;
import net.rationalminds.Parser;
import org.yellowteam.mapper.JavaJsonMapper;
import org.yellowteam.models.Author;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        JavaJsonMapper mapper = new JavaJsonMapper();
        System.out.println("This is a JavaJsonMapper");
        Author author1 = new Author("John Does", 42,LocalDateTime.of(1975, 4, 20, 10, 5));
        String author = mapper.toJson(author1);
        System.out.println(author); ///gets current LocalDateTime
        /*LocalDateTime dateTime= LocalDateTime.of(1975, 4, 20, 10, 5);*/
       /* String currentString = mapper.toJson(current);*/
        /*String dateTimeString= mapper.toJson(dateTime);*/
        /*System.out.println(currentString);*/
       /* System.out.println(dateTimeString);*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy",Locale.US);
        Parser parser= new Parser();
        List<LocalDateModel> dateModels = parser.parse(author);
        System.out.println(dateModels);
        String newString = String.valueOf(dateModels.get(0).getClass().getSimpleName());
        Locale.setDefault(Locale.FRANCE);
        LocalDate localDateTime = LocalDate.parse(newString, DateTimeFormatter.ofPattern(dateModels.get(0).getConDateFormat(),Locale.US));
        System.out.println(localDateTime);
        System.out.println(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-dd-MM")));
        author = author.substring(0,dateModels.get(0).getStart()-1) + localDateTime.format(DateTimeFormatter.ofPattern("yyyy-dd-MM")) + author.substring(dateModels.get(0).getEnd());
        System.out.println(author);
    }
}