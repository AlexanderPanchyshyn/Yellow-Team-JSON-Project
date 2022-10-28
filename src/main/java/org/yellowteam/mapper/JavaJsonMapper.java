package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    private static final Class<?>[] VALUE_TYPES = new Class[] {Number.class, String.class, Character.class, Boolean.class};
    private static final Class<?>[] QUOTATION_VALUES = new Class[] {String.class, Character.class};
    private static final Class<?>[] NOT_QUOTATION_VALUES = new Class[] {Boolean.class, Number.class};

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
            return parseArray(Arrays.stream(((Object[])object)).toList());
        } else if (isTypeInArray(object.getClass(), VALUE_TYPES)) {
            return parseValues(object);
        } else if (object instanceof LocalDateTime || object instanceof LocalDate) {
            return parseLocalDate(object);
        } else {
            return parseObject(object);
        }
    }

    private String parseLocalDate(Object object) {
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
                        .map(field -> {
                            field.setAccessible(true);
                            return field;
                        }).map(field -> {
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
            return parseLocalDate(value);
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

    public JavaJsonMapper () {
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
            COLON = Pattern.compile(":"),
            STRING = Pattern.compile("\"([^\"]+)\""),
            BOOLEAN = Pattern.compile("true|false"),
            INTEGER = Pattern.compile("-?\\d+"),
            DECIMAL = Pattern.compile("-?(0|[1-9]\\d*)\\.\\d+([eE][-+]?\\d+)?");

    private final String json;
    private final Map<String, Object> result = new LinkedHashMap<>();
    private final Matcher matcher;
    private int cursor = 0;
    private String key = null;
    private Object value = null;

    private List<Object> array = new ArrayList<>();

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
        return this::consumeLeftCurlyBracket;
    }

    private State consumeLeftCurlyBracket() {
        if (tryAdvance(LEFT_CURLY_BRACKET)) {
            return this::consumeKey;
        }
        throw new IllegalStateException("Expecting '{'");
    }

    private State consumeKey() {
        if (tryAdvance(STRING)) {
            key = String.valueOf(matcher.group(1));
        } else {
            key = " ";
        }
        if (value == null) {
            return this::consumeColon;
        } else {
            value = null;
            return this::consumeCommaOrRightCurlyBracket;
        }
    }

    private State consumeColon() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COLON)) {
            return this::consumeValue;
        }
        throw new IllegalStateException("Expecting ':'");
    }

    private State consumeValue() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(STRING)) {
            value = matcher.group(1);
            addToMap();
            return this::consumeCommaOrRightCurlyBracket;
        } else if (tryAdvance(LEFT_BRACKET)) {
            return this::consumeArray;
        } else if (tryAdvance(DECIMAL)) {
            value = Double.valueOf(matcher.group());
            addToMap();
            return this::consumeCommaOrRightCurlyBracket;
        } else if (tryAdvance(INTEGER)) {
            value = Integer.valueOf(matcher.group());
            addToMap();
            return this::consumeCommaOrRightCurlyBracket;
        } else if (tryAdvance(BOOLEAN)) {
            value = Boolean.valueOf(matcher.group());
            addToMap();
            return this::consumeCommaOrRightCurlyBracket;
        } else {
            value = " ";
            addToMap();
            return this::consumeCommaOrRightCurlyBracket;
        }
    }

    private State consumeArray() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(DECIMAL)) {
            array.add(Double.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;
        } else if (tryAdvance(INTEGER)) {
            array.add(Integer.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;
        } else if (tryAdvance(BOOLEAN)) {
            array.add(Boolean.valueOf(matcher.group()));
            return this::consumeCommaOrRightBracket;
        } else if (tryAdvance(STRING)) {
            array.add(matcher.group(1));
            return this::consumeCommaOrRightBracket;
        } else if (tryAdvance(LEFT_BRACKET)) {
            array.add(new ArrayList<>());
            return this::consumeArray;
        } else {
            array.add(" ");
            return this::consumeCommaOrRightBracket;
        }
    }

    private State consumeCommaOrRightBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeArray;
        }
        if (tryAdvance(RIGHT_BRACKET)) {
            value = new ArrayList<>();
            for (Object o : array) {
                ((ArrayList<Object>) value).add(o);
            }
            addToMap();
            array.clear();
            return consumeCommaOrRightCurlyBracket();
        }
        throw new IllegalStateException("Expecting ',' or ']'");
    }

    private void addToMap() {
        result.put(key, value);
        key = null;
        value = null;
    }

    private State consumeCommaOrRightCurlyBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeKey;
        }
        if (tryAdvance(RIGHT_CURLY_BRACKET)) {
            return null;
        }
        throw new IllegalStateException("Expecting ',' or '}'");
    }
}