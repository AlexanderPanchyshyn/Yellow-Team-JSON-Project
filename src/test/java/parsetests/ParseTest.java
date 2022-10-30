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
        String json6 = "\"1953-10-20\"";
        String json7 = "\"2020-09-10T08:01\"";

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
        assertThat(obj7).isEqualTo(Map.of("Primitive value", LocalDateTime.of(2020, 9, 10, 8, 1)));
    }

    @Test
    @DisplayName("Parsing to Array")
    void toArray() {
        String json = "[false, 65.3, \"word\"]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(false, 65.3, "word")));
    }

    @Test
    @DisplayName("Parsing Array in Array")
    void toArray2() {
        String json = "[false, 65.3, \"word\",[23,\"hello\",true]]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(false, 65.3, "word", Arrays.asList(23, "hello", true))));
    }

    @Test
    @DisplayName("Parsing Array and Object in Array")
    void toArray3() {
        String json = "[false, 65.3, \"word\",[23,\"hello\",true],{\"name\":\"Alexander\",\"age\":22,\"isHappy\":true}]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(false, 65.3, "word", Arrays.asList(23, "hello", true),
                Map.of("name", "Alexander", "age", 22, "isHappy", true))));
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

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25,
                "married", false, "hobby", "chess"));
    }

    @Test
    @DisplayName("Parsing to String, Number, Boolean and Array Objects")
    void jsonToArrayObject() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"sex\":\"M\"," +
                "\"data\":[16,false,3.14,\"word\"]}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25,
                "sex", 'M', "data", Arrays.asList(16, false, 3.14, "word")));
    }

    @Test
    @DisplayName("Parsing all possible Objects")
    void jsonToObjectWithArraysAndObjects() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"sex\":\"M\"," +
                "\"data\":[16,false,3.14,\"word\"],\"friend\":{\"name\":\"Alexander\",\"age\":22," +
                "\"isHappy\":true,\"luckyNumbers\":[16,3.14]}}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25,
                "sex", 'M', "data", Arrays.asList(16, false, 3.14, "word"),
                "friend", Map.of("name", "Alexander", "age", 22, "isHappy", true,
                        "luckyNumbers", Arrays.asList(16, 3.14))));
    }

    @Test
    @DisplayName("Parsing LocalDate And LocalDate in array")
    void jsonLocalDateAndLocalDateTimeParseToArray() {
        String json = "[\"1953-10-20\" , \"2020-09-10T08:01\"]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(
                LocalDate.of(1953, 10, 20),
                LocalDateTime.of(2020, 9, 10, 8, 1))));
    }

    @Test
    @DisplayName("Parsing LocalDate And LocalDate at array in array")
    void jsonLocalDateAndLocalDateTimeParseToArrayInArray() {

        String json = "[\"1953-10-20\", \"2020-09-10T08:01\",[\"2021-08-15T09:35\",\"1964-11-14\"]]";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("array", Arrays.asList(
                LocalDate.of(1953, 10, 20),
                LocalDateTime.of(2020, 9, 10, 8, 1),
                Arrays.asList(LocalDateTime.of(2021, 8, 15, 9, 35),
                        LocalDate.of(1964, 11, 14)))));
    }

    @Test
    @DisplayName("Parsing LocalDate And LocalDate as object")
    void toStringAndLocalDateAndLocalDateTime() {
        String json = "{\"dateOfBirth\":\"1953-10-20T10:05\",\"creationDate\":\"1964-11-14\"}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("dateOfBirth", LocalDateTime.of(1953, 10, 20, 10, 5),
                "creationDate", LocalDate.of(1964, 11, 14)));
    }
}