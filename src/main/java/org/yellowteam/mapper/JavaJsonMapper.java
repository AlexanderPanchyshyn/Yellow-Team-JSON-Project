package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public JavaJsonMapper() {
        this.json = "";
        matcher = null;
    }

    private Map<String, Object> parse(String json) {
        return new JavaJsonMapper(json).parse();
    }

    private static final Pattern
            WHITESPACE = Pattern.compile("\\s+"),
            LEFT_CURLY_BRACKET = Pattern.compile("\\{"),
            RIGHT_CURLY_BRACKET = Pattern.compile("}"),
            LEFT_BRACKET = Pattern.compile("\\["),
            RIGHT_BRACKET = Pattern.compile("]"),
            COMMA = Pattern.compile(","),
            STRING = Pattern.compile("\"([^\"]+)\""),
            CHAR = Pattern.compile("\"(\\w)\""),
            BOOLEAN = Pattern.compile("true|false"),
            NULL = Pattern.compile("null"),
            INTEGER = Pattern.compile("-?\\d+"),
            DECIMAL = Pattern.compile("-?(0|[1-9]\\d*)\\.\\d+([eE][-+]?\\d+)?"),
            STRING_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\""),
            CHAR_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"(\\w)\""),
            BOOLEAN_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(true|false)"),
            INTEGER_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?\\d+)"),
            DECIMAL_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?(0|[1-9]\\d*)\\.\\d+([eE][-+]?\\d+)?)"),
            LOCAL_DATE = Pattern.compile("\"(\\d{4})-(\\d{2})-(\\d{2})\""),
            LOCAL_DATE_TIME = Pattern.compile("\"(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})\""),
            LOCAL_DATE_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\"(\\d{4}-\\d{2}-\\d{2})\""),
            LOCAL_DATE_TIME_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\"");


    private final String json;
    private final Map<String, Object> result = new LinkedHashMap<>();
    private final Matcher matcher;
    private int cursor = 0;
    private final List<Object> array = new ArrayList<>();

    public JavaJsonMapper(String json) {
        this.json = json;
        matcher = WHITESPACE.matcher(json);
    }

    interface State extends Supplier<State> {
    }

    private Map<String, Object> parse() {
        State parseState = this::start;
        while (parseState != null) {
            parseState = parseState.get();
        }
        return result;
    }

    private boolean tryAdvance(Pattern pattern) {
        if (matcher.region(cursor, matcher.regionEnd())
                .usePattern(pattern).lookingAt()
        ) {
            cursor = matcher.end();
            return true;
        }
        return false;
    }

    private State start() {
        tryAdvance(WHITESPACE);
        return this::consumeJson;
    }

    private State consumeJson() {
        if (tryAdvance(NULL)) {
            result.put("null", "null");
            return null;
        } else if (tryAdvance(LEFT_CURLY_BRACKET)) {
            return this::consumeField;
        } else if (tryAdvance(LEFT_BRACKET)) {
            return this::consumeArray;
        } else {
            return this::consumePrimitive;
        }
    }

    private State consumePrimitive() {
        String key = "Primitive value";
        if (tryAdvance(CHAR)) {

            result.put(key, matcher.group(1).charAt(0));
            return null;

        } else if (tryAdvance(LOCAL_DATE_TIME)) {

            result.put(key, LocalDateTime.of(Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5))));
            return null;

        } else if (tryAdvance(LOCAL_DATE)) {

            result.put(key, LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))));
            return null;

        } else if (tryAdvance(STRING)) {

            result.put(key, matcher.group(1));
            return null;

        } else if (tryAdvance(DECIMAL)) {

            result.put(key, Double.valueOf(matcher.group()));
            return null;

        } else if (tryAdvance(INTEGER)) {

            result.put(key, Integer.valueOf(matcher.group()));
            return null;

        } else if (tryAdvance(BOOLEAN)) {

            result.put(key, Boolean.valueOf(matcher.group()));
            return null;

        }
        throw new IllegalStateException("Incorrect data type!");
    }

    private State consumeArray() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(DECIMAL)) {

            array.add(Double.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(LOCAL_DATE_TIME)) {

            array.add(LocalDateTime.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5))
            ));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(LOCAL_DATE)) {

            array.add(LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(INTEGER)) {

            array.add(Integer.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(BOOLEAN)) {

            array.add(Boolean.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(CHAR)) {

            array.add(matcher.group(1).charAt(0));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(STRING)) {

            array.add(matcher.group(1));
            return this::consumeCommaOrRightBracket;

        } else if (tryAdvance(LEFT_BRACKET)) {  //not working

            array.add(new ArrayList<>());
            return this::consumeArray;

        }
        throw new IllegalStateException("Incorrect data type!");
    }

    private State consumeCommaOrRightBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeArray;
        }
        if (tryAdvance(RIGHT_BRACKET)) {
//            value = new ArrayList<>();
//            for (Object o : array) {
//                ((ArrayList<Object>) value).add(o);
//            }
//            addToMap();
//            array.clear();
            result.put("array", array);
            return null;
        }
        throw new IllegalStateException("Expecting ',' or ']'");
    }

    private State consumeField() {
        if (tryAdvance(CHAR_FIELD)) {

            result.put(matcher.group(1), matcher.group(2).charAt(0));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(LOCAL_DATE_TIME_FIELD)) {

            result.put(matcher.group(1), matcher.group(2));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(LOCAL_DATE_FIELD)) {

            result.put(matcher.group(1), matcher.group(2));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(STRING_FIELD)) {

            result.put(matcher.group(1), matcher.group(2));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(DECIMAL_FIELD)) {

            result.put(matcher.group(1), Double.valueOf(matcher.group(2)));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(INTEGER_FIELD)) {

            result.put(matcher.group(1), Integer.valueOf(matcher.group(2)));
            return this::consumeCommaOrRightCurlyBracket;

        } else if (tryAdvance(BOOLEAN_FIELD)) {

            result.put(matcher.group(1), Boolean.valueOf(matcher.group(2)));
            return this::consumeCommaOrRightCurlyBracket;

        }
        throw new IllegalStateException("Incorrect value data type!");
    }

    private State consumeCommaOrRightCurlyBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeField;
        }
        if (tryAdvance(RIGHT_CURLY_BRACKET)) {
            return null;
        }
        throw new IllegalStateException("Expecting ',' or '}'");
    }

    public String prettifyJson(String jsonFile, int indentLevel) {
        return new JsonPrettifier().prettifyJsonToReadableView(jsonFile, indentLevel);
    }
}