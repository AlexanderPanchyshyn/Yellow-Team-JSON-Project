package org.yellowteam.mapper;

import org.yellowteam.models.Book;
import org.yellowteam.models.BookShelf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
            String val = null;
            String name = field.getName();

            if (field.get(o) instanceof List<?> fieldList) {
                val = "";
                for (int i = 0; i < fieldList.size(); i++) {
                    if (i == fieldList.size() - 1) {
                        val += convertToJson(fieldList.get(i));
                        break;
                    } else {
                        val += convertToJson(fieldList.get(i)) + ",";
                    }
                }
                list.add("\"%s\" : [%n%s]".formatted(name, val));
                continue;
            }

            val = String.valueOf(field.get(o));
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
        return result + "}\n";
    }

    @Override
    public <T> T parse(String json, Class<T> cls) {
        return null;
    }
}
