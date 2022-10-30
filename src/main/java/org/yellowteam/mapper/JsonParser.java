package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JsonParser {
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
            LOCAL_DATE = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})"),
            LOCAL_DATE_TIME = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})"),
            LOCAL_DATE_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:(\\d{4})-(\\d{2})-(\\d{2})"),
            LOCAL_DATE_TIME_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})"),
            ARRAY_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\["),
            OBJECT_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{");


    private final String json;
    private final Map<String, Object> result = new LinkedHashMap<>();
    private final Matcher matcher;
    private int cursor = 0;
    private static int innerCursor = 0;
    private final List<Object> array = new ArrayList<>();

    private int getInnerCursor() {
        return innerCursor;
    }

    private static void setInnerCursor(int cursor) {
        JsonParser.innerCursor = cursor;
    }
    JsonParser(String json) {
        this.json = json;
        matcher = WHITESPACE.matcher(json);
    }

    interface State extends Supplier<State> {
    }

    Map<String, Object> parse() {
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

        } else if (tryAdvance(LOCAL_DATE_TIME)) {

            result.put(key, LocalDateTime.of(Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5))));

        } else if (tryAdvance(LOCAL_DATE)) {

            result.put(key, LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))));

        } else if (tryAdvance(STRING)) {

            result.put(key, matcher.group(1));

        } else if (tryAdvance(DECIMAL)) {

            result.put(key, Double.valueOf(matcher.group()));

        } else if (tryAdvance(INTEGER)) {

            result.put(key, Integer.valueOf(matcher.group()));

        } else if (tryAdvance(BOOLEAN)) {

            result.put(key, Boolean.valueOf(matcher.group()));

        } else {

            throw new IllegalStateException("Incorrect data type!");

        }
        return null;
    }

    private State consumeArray() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(DECIMAL)) {

            array.add(Double.valueOf(matcher.group()));

        } else if (tryAdvance(INTEGER)) {

            array.add(Integer.valueOf(matcher.group()));

        } else if (tryAdvance(BOOLEAN)) {

            array.add(Boolean.valueOf(matcher.group()));

        } else if (tryAdvance(CHAR)) {

            array.add(matcher.group(1).charAt(0));

        } else if (tryAdvance(STRING)) {

            array.add(matcher.group(1));

        } else if (tryAdvance(LEFT_BRACKET)) {

            array.add(new JsonParser(json.substring(cursor - 1)).parse().get("array"));
            cursor += getInnerCursor();

        } else if (tryAdvance(LEFT_CURLY_BRACKET)) {

            array.add(new JsonParser(json.substring(cursor - 1)).parse());
            cursor += getInnerCursor();

        } else {

            throw new IllegalStateException("Incorrect data type!");

        }

        return this::consumeCommaOrRightBracket;
    }

    private State consumeCommaOrRightBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeArray;
        }
        if (tryAdvance(RIGHT_BRACKET)) {
            result.put("array", array);
            setInnerCursor(cursor - 1);
            return null;
        }
        throw new IllegalStateException("Expecting ',' or ']'");
    }

    private State consumeField() {
        if (tryAdvance(CHAR_FIELD)) {

            result.put(matcher.group(1), matcher.group(2).charAt(0));

        } else if (tryAdvance(LOCAL_DATE_TIME_FIELD)) {

            result.put(matcher.group(1), LocalDateTime.of(
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5)),
                    Integer.parseInt(matcher.group(6))));

        } else if (tryAdvance(LOCAL_DATE_FIELD)) {

            result.put(matcher.group(1), LocalDate.of(
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)))
            );

        } else if (tryAdvance(STRING_FIELD)) {

            result.put(matcher.group(1), matcher.group(2));

        } else if (tryAdvance(DECIMAL_FIELD)) {

            result.put(matcher.group(1), Double.valueOf(matcher.group(2)));

        } else if (tryAdvance(INTEGER_FIELD)) {

            result.put(matcher.group(1), Integer.valueOf(matcher.group(2)));

        } else if (tryAdvance(BOOLEAN_FIELD)) {

            result.put(matcher.group(1), Boolean.valueOf(matcher.group(2)));

        } else if (tryAdvance(ARRAY_FIELD)) {

            result.put(matcher.group(1), new JsonParser(json.substring(cursor - 1)).parse().get("array"));
            cursor += getInnerCursor();

        } else if (tryAdvance(OBJECT_FIELD)) {

            result.put(matcher.group(1), new JsonParser(json.substring(cursor - 1)).parse());
            cursor += getInnerCursor();

        } else {

            throw new IllegalStateException("Incorrect value data type!");

        }

        return this::consumeCommaOrRightCurlyBracket;
    }

    private State consumeCommaOrRightCurlyBracket() {
        tryAdvance(WHITESPACE);
        if (tryAdvance(COMMA)) {
            return this::consumeField;
        }
        if (tryAdvance(RIGHT_CURLY_BRACKET)) {
            setInnerCursor(cursor - 1);
            return null;
        }
        throw new IllegalStateException("Expecting ',' or '}'");
    }
}
