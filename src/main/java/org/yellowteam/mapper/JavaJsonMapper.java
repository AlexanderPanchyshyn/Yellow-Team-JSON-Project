package org.yellowteam.mapper;

import java.util.ArrayList;
import java.util.List;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    @Override
    public String toJson(Object o) throws IllegalAccessException {
        return process(o);
    }

    private String process(Object o) throws IllegalAccessException {
        String json = "{";
        List<String> list = new ArrayList<>();

        convertToJson(o, list);

        json = collectValues(json, list);

        return json;
    }

    private void convertToJson(Object o, List<String> list) throws IllegalAccessException {
        var fields = o.getClass().getDeclaredFields();

        for (var field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            var el = field.get(o);

            if (el instanceof String ||
                    el instanceof Character ||
                    el instanceof Number ||
                    el instanceof Boolean) {
                convertPrimitiveToJson(name, field.get(o), list);
            } else if (field.get(o) instanceof List<?> fieldList) {
                convertNonPrimitiveToJson(name, fieldList, list);
            } else {
                convertObjectToJson(name, el, list);
            }
        }
    }


    private void convertObjectToJson(String objName, Object obj, List<String> list) throws IllegalAccessException {
        list.add("\"%s\":{".formatted(objName));
        convertToJson(obj, list);
//        list.add("}");
    }

    private String collectValues(String json, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            var pos = i == list.size() - 1 ? "" : ",";
            json += list.get(i) + pos;
        }
        return json + "}";
    }

    private void convertNonPrimitiveToJson(String name, List<?> fieldList, List<String> list) throws IllegalAccessException {
        String val = "";
        for (int i = 0; i < fieldList.size(); i++) {
            var el = fieldList.get(i);
            var pos = i == fieldList.size() - 1 ? "" : ",";
            val = typeChecker(el, val, pos);
        }
        list.add("\"%s\":[%s]".formatted(name, val));
    }

    private String typeChecker(Object el, String val, String pos) throws IllegalAccessException {
        if (el instanceof String ||
                el instanceof Character ||
                el instanceof Number ||
                el instanceof Boolean) {
            val += convertPrimitiveValueToJson(el) + pos;
        } else {
            val += process(el) + pos;
        }
        return val;
    }

    private void convertPrimitiveToJson(String name, Object obj, List<String> list) {
        list.add("\"%s\":\"%s\"".formatted(name, String.valueOf(obj)));
    }

    private String convertPrimitiveValueToJson(Object element) {
        return "\"%s\"".formatted(String.valueOf(element));
    }


    @Override
    public <T> T parse(String json, Class<T> cls) {
        return null;
    }
}
