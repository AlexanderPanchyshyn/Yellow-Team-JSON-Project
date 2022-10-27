package parsetests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yellowteam.mapper.JavaJsonMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ParseTest {
    JavaJsonMapper mapper = new JavaJsonMapper();

    @Test
    @DisplayName("Parsing to String Object")
    void test1() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\"}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson"));
    }

    @Test
    @DisplayName("Parsing to String and Number Objects")
    void test2() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25));
    }

    @Test
    @DisplayName("Parsing to String, Number and Boolean Objects")
    void test3() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"married\":false,\"hobby\":\"chess\"}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25, "married", false, "hobby", "chess"));
    }

    @Test
    @DisplayName("Parsing to String, Number, Boolean and Array Objects")
    void jsonToArray() {
        String json = "{\"name\":\"Nick\",\"surName\":\"Johnson\",\"age\":25,\"sex\":\"M\",\"data\":[16,false,3.14,\"word\",[1,2]]}";

        var obj = mapper.mapFromJson(json);

        assertThat(obj).isEqualTo(Map.of("name", "Nick", "surName", "Johnson", "age", 25, "sex", "M", "data", Arrays.asList(16, false, 3.14, "word", Arrays.asList(1, 2))));
    }
}