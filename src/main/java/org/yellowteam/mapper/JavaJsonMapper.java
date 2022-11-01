package org.yellowteam.mapper;

import org.yellowteam.models.JsonElement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    private static final Class<?>[] VALUE_TYPES = new Class[]{Number.class, String.class, Character.class, Boolean.class};
    private static final Class<?>[] QUOTATION_VALUES = new Class[]{String.class, Character.class};
    private static final Class<?>[] NOT_QUOTATION_VALUES = new Class[]{Boolean.class, Number.class};
    private DateJsonFormatter dateFormatter;


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
        } else if (object instanceof LocalDateTime || object instanceof LocalDate) {
            return writeLocalDateToJson(object);
        } else {
            return parseObject(object);
        }
    }

    private String writeLocalDateToJson(Object object) {
        if (isDateFormatterNull()) {
            return "\"%s\"".formatted(object.toString());
        } else {
            String dateWithPattern = dateFormatter.dateWithPattern(object);
            return "\"%s\"".formatted(dateWithPattern);
        }
    }

    public String changeDatePattern(String jsonString, String pattern) {
        if (!isDateFormatterNull()) {
            return dateFormatter.changeJsonDateFormatter(jsonString, pattern);
        } else {
            createDateFormatter(pattern);
            return dateFormatter.changeJsonDateFormatter(jsonString);
        }
    }

    public void withDatePattern(String pattern) {
        if (isDateFormatterNull()) {
            createDateFormatter(pattern);
        } else {
            dateFormatter.changeDatePattern(pattern);
        }
    }

    private boolean isDateFormatterNull() {
        return dateFormatter == null;
    }

    private void createDateFormatter(String pattern) {
        dateFormatter = new DateJsonFormatter(pattern);
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
