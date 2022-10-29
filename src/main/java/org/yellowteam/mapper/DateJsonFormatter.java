package org.yellowteam.mapper;

import net.rationalminds.LocalDateModel;
import net.rationalminds.Parser;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateJsonFormatter {
    private DateTimeFormatter dateTimeFormatter;
    private String currentPattern;
    private Parser parser;

    public DateJsonFormatter(String pattern){
        parser = new Parser();
        currentPattern = pattern;
        dateTimeFormatter=DateTimeFormatter.ofPattern(currentPattern, Locale.US);

    }
    protected void changeDatePattern(String pattern){
        currentPattern= pattern;
        dateTimeFormatter = DateTimeFormatter.ofPattern(currentPattern, Locale.US);
    }
    private String dateStringWithFormatter(Object objValue) {
        if (objValue instanceof LocalDate) {
            return ((LocalDate) objValue).format(dateTimeFormatter);
        }
        if (objValue instanceof LocalDateTime) {
            return ((LocalDateTime) objValue).format(dateTimeFormatter);
        }
        if (objValue instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat(currentPattern);
            return formatter.format(objValue);
        }
        return objValue.toString();
    }
    protected String dateWithPattern (Object objectValue)
    {
        try {
            return dateStringWithFormatter(objectValue);
        } catch (Exception e) {
            return "Wrong Pattern";
        }
    }

    protected String changeJsonDateFormatter(String jsonMapper,String pattern){
        changeDatePattern(pattern);
        List<LocalDateModel> dateModels = parser.parse(jsonMapper);
        for ( var dateModel:dateModels
             ) {
            LocalDate localDate = stringToLocaleDate(dateModel);
            String newDatePattern = dateWithPattern(localDate);
            if(newDatePattern.equals("Error")){
                return "Error";
            }
            int positionStartDate =dateModels.get(0).getStart()-1;
            int positionEndDate = dateModels.get(0).getEnd();
            jsonMapper = jsonMapper.substring(0,positionStartDate) + newDatePattern + jsonMapper.substring(positionEndDate);
        }
        return jsonMapper;
    }
    private LocalDate stringToLocaleDate(LocalDateModel dateModel){
        String dateString=dateModel.getOriginalText();
        String datePattern = dateModel.getIdentifiedDateFormat();
        LocalDate localDateTime = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(datePattern,Locale.US));
        return localDateTime;
    }
}
