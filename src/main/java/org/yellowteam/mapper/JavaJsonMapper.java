package org.yellowteam.mapper;

import java.util.ArrayList;
import java.util.List;

public class JavaJsonMapper implements JavaJsonMapperInterface {

    @Override
    public String toJson(Object o) throws IllegalAccessException {
        return convertToJson(o);
    }

    private String convertToJson(Object o) throws IllegalAccessException {
        String result = "{\n";
        List<String> list = new ArrayList<>();
        var fields = o.getClass().getDeclaredFields();

        for (var field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            var val = field.get(o);
            if (val.getClass() == String.class) {
                val = (String) val;
            }
            list.add("\"%s\" : \"%s\"".formatted(name, val));
        }

        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                result += list.get(i) + "\n";
                break;
            } else {
                result += list.get(i) + ",\n";
            }
        }
        return result + "}";
    }

    @Override
    public <T> T parse(String json, Class<T> cls) {
        return null;
    }
}
