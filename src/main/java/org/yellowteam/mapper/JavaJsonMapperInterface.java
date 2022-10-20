package org.yellowteam.mapper;

interface JavaJsonMapperInterface {
    String toJson(Object o);
    <T> T parse(String json, Class<T> cls);
}
