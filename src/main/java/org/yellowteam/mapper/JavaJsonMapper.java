package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    private static final Class<?>[] VALUE_TYPES = new Class[]{Number.class, String.class, Character.class, Boolean.class};
    private static final Class<?>[] QUOTATION_VALUES = new Class[]{String.class, Character.class};
    private static final Class<?>[] NOT_QUOTATION_VALUES = new Class[]{Boolean.class, Number.class};

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
        return "\"%s\"".formatted(object.toString());
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
                        .peek(field -> field.setAccessible(true)).map(field -> {
                            try {
                                return "\"" + field.getName() + "\":" + parseJson(field.get(object));
                            } catch (ReflectiveOperationException roe) {
                                throw new RuntimeException(roe);
                            }
                        }).collect(Collectors.joining(",")) +
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