package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

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

    private Map<String, Object> parse(String json) {
        Map<String, Object> res = new LinkedHashMap<>();

        Pattern stringPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\",?");
        Matcher stringMatcher = stringPattern.matcher(json);

        Pattern numberPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+),?");
        Matcher numberMatcher = numberPattern.matcher(json);

        Pattern booleanPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(true|false),?");
        Matcher booleanMatcher = booleanPattern.matcher(json);

        while (true) {
            if (stringMatcher.find()) {
                res.put(stringMatcher.group(1), stringMatcher.group(2));
            } else if (numberMatcher.find()) {
                var number = parseInt(numberMatcher.group(2));
                res.put(numberMatcher.group(1), number);
            } else if (booleanMatcher.find()) {
                var bool = parseBoolean(booleanMatcher.group(2));
                res.put(booleanMatcher.group(1), bool);
            } else {
                break;
            }
        }

        return res;
    }
}