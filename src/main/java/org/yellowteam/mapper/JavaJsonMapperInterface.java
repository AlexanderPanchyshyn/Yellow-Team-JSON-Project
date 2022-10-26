package org.yellowteam.mapper;

import java.util.Map;

interface JavaJsonMapperInterface {
    String toJson(Object o) throws IllegalAccessException;
    Map<String, Object> mapFromJson(String json);
}
