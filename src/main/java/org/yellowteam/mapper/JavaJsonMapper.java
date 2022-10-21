package org.yellowteam.mapper;

import java.util.ArrayList;
import java.util.List;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    @Override
    public String toJson(Object o) throws IllegalAccessException {
        return convertToJson(o);
    }

    private String convertToJson(Object o) throws IllegalAccessException {
        String json = "{\n";
        List<String> list = new ArrayList<>();
        var fields = o.getClass().getDeclaredFields();

        for (var field : fields) {
            field.setAccessible(true);
            String name = field.getName();

            if (field.get(o) instanceof List<?> fieldList) {
                convertNonPrimitiveToJson(name, fieldList, list);
            } else {
                convertPrimitiveToJson(name, field.get(o), list);
            }
        }

        json = collectValues(json, list);

        return json;
    }

    private String collectValues(String json, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                json += list.get(i) + "\n";
                break;
            } else {
                json += list.get(i) + ",\n";
            }
        }
        return json + "}\n";
    }

    private void convertNonPrimitiveToJson(String name, List<?> fieldList, List<String> list) throws IllegalAccessException {
        String val = "";
        for (int i = 0; i < fieldList.size(); i++) {
            var el = fieldList.get(i);
            var pos = i == fieldList.size() - 1 ? "" : ",";
            val = typeChecker(el, val, pos);
        }
        list.add("\t\"%s\" : [%n\t%s]".formatted(name, val));
    }

    private String typeChecker(Object el, String val, String pos) throws IllegalAccessException {
        if (el instanceof String ||
                el instanceof Character ||
                el instanceof Number ||
                el instanceof Boolean) {
            val += convertPrimitiveValueToJson(el) + pos;
        } else {
            val += convertToJson(el) + pos;
        }
        return val;
    }

    private void convertPrimitiveToJson(String name, Object obj, List<String> list) {
        list.add("\t\"%s\" : \"%s\"".formatted(name, String.valueOf(obj)));
    }

    private String convertPrimitiveValueToJson(Object element) {
        return "\"%s\"".formatted(String.valueOf(element));
    }


    @Override
    public <T> T parse(String json, Class<T> cls) {
        return null;
    }
}
