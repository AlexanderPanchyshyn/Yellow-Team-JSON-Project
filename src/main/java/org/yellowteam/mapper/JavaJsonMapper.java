package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    @Override
    public String toJson(Object o) throws IllegalAccessException {
        String json = "{";

        //Creating a list, where would be saved all fields and values of incoming object
        List<String> list = new ArrayList<>();

        //Converting to Json all fields of object and saving it to main list
        convertToJson(o, list);

        //Formatting all incoming list values to proper Json way
        json = collectValues(json, list);

        return json;
    }

    private void convertToJson(Object object, List<String> list) throws IllegalAccessException {
        var objFields = object.getClass().getDeclaredFields();

        // Checking what type of field values object has and invoking needed methods
        for (var field : objFields) {
            field.setAccessible(true);
            String objName = field.getName();
            var objValue = field.get(object);

            if (objValue instanceof String ||
                    objValue instanceof Character ||
                    objValue instanceof Number ||
                    objValue instanceof Boolean) {

                convertPrimitiveToJson(objName, objValue, list);

            } else if (objValue instanceof LocalDateTime || objValue instanceof LocalDate) {
                convertDateToJson(objName, objValue, list);
            } else if (objValue instanceof List<?> fieldList) {

                convertArrayToJson(objName, fieldList, list);

            } else {

                convertObjectToJson(objName, objValue, list);

            }
        }
    }

    private void convertDateToJson(String objName, Object objValue, List<String> list) {
        list.add("\"%s\":\"%s\"".formatted(objName, objValue.toString()));
    }

    private String convertDateValueToJson(Object element) {
        return "\"%s\"".formatted(element.toString());
    }


    private void convertObjectToJson(String objName, Object obj, List<String> list) throws IllegalAccessException {
        int namePos = 0;
        int amountOfObjFields = obj.getClass().getDeclaredFields().length;
        String objValues = "";

        list.add("\"%s\":{".formatted(objName));
        convertToJson(obj, list);
        list.add("}");

        // FORMATTING ALL VALUES IN PROPER JSON WAY
        // Finding the position of a name in a list
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i), "\"%s\":{".formatted(objName))) {
                namePos = i;
            }
        }

        // Creating values list from all object fields and deleting them from main list
        for (int i = 0; i < amountOfObjFields; i++) {
            var pos = i == amountOfObjFields - 1 ? "" : ",";

            objValues += list.get(namePos + 1) + pos;
            list.remove(namePos + 1);
        }

        // Changing name item in main list to one common item - name:{ + values + }
        list.set(namePos, list.get(namePos) + objValues + list.get(namePos + 1));
        list.remove(namePos + 1);
    }

    private void convertArrayToJson(String objName, List<?> fieldList, List<String> list) throws IllegalAccessException {
        String value = "";

        for (int i = 0; i < fieldList.size(); i++) {
            var objValue = fieldList.get(i);
            var itemPos = i == fieldList.size() - 1 ? "" : ",";

            value = typeChecker(objValue, value, itemPos);
        }

        list.add("\"%s\":[%s]".formatted(objName, value));
    }

    private void convertPrimitiveToJson(String objName, Object obj, List<String> list) {
        if (obj instanceof Number || obj instanceof Boolean) {
            list.add("\"%s\":%s".formatted(objName, String.valueOf(obj)));
        } else {
            list.add("\"%s\":\"%s\"".formatted(objName, String.valueOf(obj)));
        }
    }

    private String convertPrimitiveValueToJson(Object element) {
        if (element instanceof Number || element instanceof Boolean) {
            return "%s".formatted(String.valueOf(element));
        } else {
            return "\"%s\"".formatted(String.valueOf(element));
        }
    }

    private String typeChecker(Object objValue, String value, String itemPos) throws IllegalAccessException {
        if (objValue instanceof String ||
                objValue instanceof Character ||
                objValue instanceof Number ||
                objValue instanceof Boolean) {

            value += convertPrimitiveValueToJson(objValue) + itemPos;
        } else if (objValue instanceof LocalDate || objValue instanceof LocalDateTime || objValue instanceof Date) {
            value += convertDateValueToJson(objValue) + itemPos;
        } else {
            value += toJson(objValue) + itemPos;
        }
        return value;
    }

    private String collectValues(String json, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            var itemPos = i == list.size() - 1 ? "" : ",";
            json += list.get(i) + itemPos;
        }
        return json + "}";
    }

    public String prettifyJsonToReadableView(String uglyJsonString, int spaceValue) {
        StringBuilder jsonPrettifyBuilder = new StringBuilder();
        consume = ch -> jsonPrettifyBuilder.append((char) ch);
        state = starterBlock;
        tabulation = 0;
        spaces = spaceValue;
        uglyJsonString.codePoints().forEach(ch -> state.accept(ch));
        return jsonPrettifyBuilder.toString();
    }

    IntConsumer
            consume;
    int spaces;
    int tabulation;
    IntConsumer state;
    IntConsumer starterBlock = ch -> {
        if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.objectBlock;
        } else if (ch == '[') {
            processAndIncreasingTabulation(ch);
            this.state = this.arrayBlock;
        } else if (ch == ',') {
            processAndAddingTabulation(ch);
        } else if (ch == ']') {
            processAndDecreasingTabulation(ch);
        } else if (ch == '}') {
            processAndDecreasingTabulation(ch);
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else if (ch == ':') {
            consume.accept(ch);
            consume.accept(' ');
        } else {
            consume.accept(ch);
        }
    };
    IntConsumer objectBlock = ch -> {
        if (ch == '[') {
            processAndIncreasingTabulation(ch);
            this.state = this.arrayBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.starterBlock;
        }
    };
    IntConsumer arrayBlock = ch -> {
        if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.objectBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        }
    };
    IntConsumer innerStringBlock = ch -> {
        if (ch == '\\') {
            this.state = this.escapeBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.starterBlock;
        } else {
            consume.accept(ch);
        }
    };
    IntConsumer escapeBlock = ch -> {
        if ("\"\\/bfnrt".indexOf((char) ch) != -1) {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else {
            throw new IllegalArgumentException("Unknown state escape: \\" + (char) ch);
        }
    };

    private void processAndAddingTabulation(int ch) {
        consume.accept(ch);
        consume.accept('\n');
        for (int i = 0; i < tabulation; i++) {
            consume.accept(' ');
        }
    }

    private void processAndIncreasingTabulation(int ch) {
        tabulation += spaces;
        processAndAddingTabulation(ch);
    }

    private void processAndDecreasingTabulation(int ch) {
        consume.accept('\n');
        tabulation -= spaces;
        for (int i = 0; i < tabulation; i++) {
            consume.accept(' ');
        }
        consume.accept(ch);
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