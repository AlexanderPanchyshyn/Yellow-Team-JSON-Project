package org.yellowteam.mapper;

interface JavaJsonMapperInterface {
    String toJson(Object o) throws IllegalAccessException;
    <T> T parse(String json, Class<T> cls);
}
