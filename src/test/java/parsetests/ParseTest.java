package parsetests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yellowteam.mapper.JavaJsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ParseTest {
    JavaJsonMapper mapper = new JavaJsonMapper();

    @Test
    @DisplayName("Parsing Primitives")
    void toPrimitives() {
        String json = "null";
        String json1 = "\"A\"";
        String json2 = "\"String\"";
        String json3 = "25";
        String json4 = "25.65";
        String json5 = "true";
        String json6 = "1953-10-20";
        String json7 = "2020-09-10T08:01";

        var obj = mapper.mapFromJson(json);
        var obj1 = mapper.mapFromJson(json1);
        var obj2 = mapper.mapFromJson(json2);
        var obj3 = mapper.mapFromJson(json3);
        var obj4 = mapper.mapFromJson(json4);
        var obj5 = mapper.mapFromJson(json5);
        var obj6 = mapper.mapFromJson(json6);
        var obj7 = mapper.mapFromJson(json7);

        assertThat(obj).isEqualTo(Map.of("null", "null"));
        assertThat(obj1).isEqualTo(Map.of("Primitive value", 'A'));
        assertThat(obj2).isEqualTo(Map.of("Primitive value", "String"));
        assertThat(obj3).isEqualTo(Map.of("Primitive value", 25));
        assertThat(obj4).isEqualTo(Map.of("Primitive value", 25.65));
        assertThat(obj5).isEqualTo(Map.of("Primitive value", true));
        assertThat(obj6).isEqualTo(Map.of("Primitive value", LocalDate.of(1953, 10, 20)));
        assertThat(obj7).isEqualTo(Map.of("Primitive value", LocalDateTime.parse(json7)));
    }

    @Test
    @DisplayName("Parsing to Array")
    void toArray() {
        String json = "[false, 65.3, \"word\"]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(false, 65.3, "word")));
    }

    @Test
    @DisplayName("Parsing to String Object")
    void toStringObject() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\"}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson"));
    }

    @Test
    @DisplayName("Parsing to String and Number Objects")
    void toStringAndNumber() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25));
    }

    @Test
    @DisplayName("Parsing to String, Number and Boolean Objects")
    void toStringAndNumberAndBoolean() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"married\":false,\"hobby\":\"chess\"}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25, "married", false, "hobby", "chess"));
    }

//    @Test
//    @DisplayName("Parsing to String, Number, Boolean and Array Objects")
//    void jsonToArray() {
//        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"sex\":\"M\",\"data\":[16,false,3.14,\"word\"]}";
//
//        var obj = mapper.mapFromJson(json);
//
//        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25, "sex", 'M', "data", Arrays.asList(16, false, 3.14, "word")));
//    }
}