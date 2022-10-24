package org.yellowteam.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        list.add("\"%s\":\"%s\"".formatted(objName, String.valueOf(obj)));
    }

    private String convertPrimitiveValueToJson(Object element) {
        return "\"%s\"".formatted(String.valueOf(element));
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

    @Override
    public <T> T parse(String json, Class<T> cls) {

        return null;
    }

    public String prettifyJsonToReadableView(String uglyJsonString) {
        StringBuilder jsonPrettifyBuilder = new StringBuilder();
        int indentLevel = 0;
        boolean prettify = false;
        for (char charFromUglyJson : uglyJsonString.toCharArray()) {
            switch (charFromUglyJson) {
                case '"':
                    // switch the prettify status
                    prettify = !prettify;
                    jsonPrettifyBuilder.append(charFromUglyJson);
                    break;
                case ' ':
                    // For space: ignore the space if it is not being quoted.
                    if (prettify) {
                        jsonPrettifyBuilder.append(charFromUglyJson);
                    }
                    break;
                case '{':
                case '[':
                    // Starting a new block: increase the indent level
                    jsonPrettifyBuilder.append(charFromUglyJson);
                    indentLevel++;
                    appendIndentedNewLine(indentLevel, jsonPrettifyBuilder);
                    break;
                case '}':
                case ']':
                    // Ending a new block; decrease the indent level
                    indentLevel--;
                    appendIndentedNewLine(indentLevel, jsonPrettifyBuilder);
                    jsonPrettifyBuilder.append(charFromUglyJson);
                    break;
                case ',':
                    // Ending a json item; create a new line after
                    jsonPrettifyBuilder.append(charFromUglyJson);
                    if (!prettify) {
                        appendIndentedNewLine(indentLevel, jsonPrettifyBuilder);
                    }
                    break;
                default:
                    jsonPrettifyBuilder.append(charFromUglyJson);
            }
        }
        return jsonPrettifyBuilder.toString();
    }

    private static void appendIndentedNewLine(int indentLevel, StringBuilder stringBuilder) {
        stringBuilder.append("\n");
        // Assuming indention using 2 spaces
        stringBuilder.append("  ".repeat(Math.max(0, indentLevel)));
    }

}