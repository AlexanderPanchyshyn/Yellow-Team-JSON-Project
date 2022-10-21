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
            String val = null;
            String name = field.getName();

            if (field.get(o) instanceof List<?> fieldList) {
                val = "";
                for (int i = 0; i < fieldList.size(); i++) {
                    var el = fieldList.get(i);
                    var pos = i == fieldList.size() - 1 ? "" : ",";
                    if (el instanceof String) {
                        val += convertPrimitiveToJson(el) + pos;
                    } else if (el instanceof Object) {
                        val += convertToJson(el) + pos;
                    }
                }
                list.add("\t\"%s\" : [%n\t%s]".formatted(name, val));
                continue;
            }

            val = String.valueOf(field.get(o));
            list.add("\t\"%s\" : \"%s\"".formatted(name, val));
        }

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

    private String convertPrimitiveToJson(Object element) {
        return "\"%s\"".formatted(String.valueOf(element));
    }


    @Override
    public <T> T parse(String json, Class<T> cls) {
        return null;
    }
}
