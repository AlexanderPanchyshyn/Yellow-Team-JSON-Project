package org.yellowteam.mapper;

import org.yellowteam.annotations.JsonElement;
import net.rationalminds.LocalDateModel;
import net.rationalminds.Parser;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    private static final Class<?>[] VALUE_TYPES = new Class[]{Number.class, String.class, Character.class, Boolean.class};
    private static final Class<?>[] QUOTATION_VALUES = new Class[]{String.class, Character.class};
    private static final Class<?>[] NOT_QUOTATION_VALUES = new Class[]{Boolean.class, Number.class};
    private DateTimeFormatter dateTimeFormatter;
    private SimpleDateFormat simpleDateFormat;

    public JavaJsonMapper(){
        this("dd-MM-yyyy");
    }
    public JavaJsonMapper(String datePattern){
        try{
            dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
            simpleDateFormat = new SimpleDateFormat(datePattern);
        }catch (Exception e){
            dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        }
    }

    @Override
    public String toJson(Object o) {
        return parseJson(o);
    }

    private String parseJson(Object object) {
        if (Objects.isNull(object)) {
            return "null";
        } else if (Iterable.class.isAssignableFrom(object.getClass())) {
            return parseArray((Iterable<?>) object);
        } else if (object.getClass().isArray()) {
            return parseArray(Arrays.stream(((Object[]) object)).toList());
        } else if (isTypeInArray(object.getClass(), VALUE_TYPES)) {
            return parseValues(object);
        } else if (object instanceof LocalDateTime || object instanceof LocalDate || object instanceof Date) {
            return writeLocalDateToJson(object);
        } else {
            return parseObject(object);
        }
    }
    private String writeLocalDateToJson(Object object) {
            String dateWithPattern = dateWithPattern(object);
            return "\"%s\"".formatted(dateWithPattern);

    }
    public String changeJsonDatePattern(String jsonString){
        Parser parserDate = new Parser();
        List<LocalDateModel> dateModels = parserDate.parse(jsonString);
        for ( var dateModel:dateModels
        ) {
            LocalDate localDate = stringToLocaleDate(dateModel);
            String newDatePattern = dateWithPattern(localDate);
            int positionStartDate =dateModels.get(0).getStart()-1;
            int positionEndDate = dateModels.get(0).getEnd();
            jsonString = jsonString.substring(0,positionStartDate) + newDatePattern + jsonString.substring(positionEndDate);
        }
        return jsonString;
    }
    private LocalDate stringToLocaleDate(LocalDateModel dateModel){
        String dateString=dateModel.getOriginalText();
        String datePattern = dateModel.getIdentifiedDateFormat();
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(datePattern,Locale.US));
    }

   private String dateWithPattern(Object object){
       if (object instanceof LocalDate localDate) {
           return localDate.format(dateTimeFormatter);
       }
       if (object instanceof LocalDateTime localDateTime) {
           return localDateTime.format(dateTimeFormatter);
       }
       if (object instanceof Date date) {
           return simpleDateFormat.format(date);
       }
       return object.toString();

   }
    private <T> String parseArray(Iterable<T> array) {
        return "[" +
                StreamSupport.stream(array.spliterator(), false)
                        .map(this::parseJson)
                        .collect(Collectors.joining(",")) +
                "]";
    }

    private String parseObject(Object object) {
        return "{" +
                Arrays.stream(object.getClass().getDeclaredFields())
                        .map(field -> {
                            field.setAccessible(true);
                            return field;
                        }).map(field -> {
                                    if (field.isAnnotationPresent(JsonElement.class)) {
                                        try {
                                            return "\"" + field.getAnnotation(JsonElement.class).name() + "\":" + parseJson(field.get(object));
                                        } catch (ReflectiveOperationException roe) {
                                            throw new RuntimeException(roe);
                                        }
                                    } else {
                                        try {
                                            return "\"" + field.getName() + "\":" + parseJson(field.get(object));
                                        } catch (ReflectiveOperationException roe) {
                                            throw new RuntimeException(roe);
                                        }
                                    }
                                }
                        ).collect(Collectors.joining(",")) +
                "}";
    }

    private String parseValues(Object value) {
        if (isTypeInArray(value.getClass(), QUOTATION_VALUES)) {
            return "\"" + value + "\"";
        } else if (isTypeInArray(value.getClass(), NOT_QUOTATION_VALUES)) {
            return String.valueOf(value);
        } else if (value instanceof LocalDateTime || value instanceof LocalDate) {
            return writeLocalDateToJson(value);
        } else {
            throw new RuntimeException("Invalid object parsed as value type: %s".formatted(value.getClass()));
        }
    }

    private boolean isTypeInArray(Class<?> mainType, Class<?>[] arrayOfTypes) {
        return Arrays.stream(arrayOfTypes).anyMatch(t -> t.isAssignableFrom(mainType));
    }

    @Override
    public Map<String, Object> mapFromJson(String json) {
        return parse(json);
    }

    private Map<String, Object> parse(String json) {
        return new JsonParser(json).parse();
    }

    public String prettifyJson(String jsonFile, int indentLevel) {
        return new JsonPrettifier().prettifyJsonToReadableView(jsonFile, indentLevel);
    }
}
