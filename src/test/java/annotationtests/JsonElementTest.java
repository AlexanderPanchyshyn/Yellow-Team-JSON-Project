package annotationtests;/* Created by Alex on 01.11.2022 */

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.yellowteam.annotations.JsonElement;
import org.yellowteam.mapper.JavaJsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@AllArgsConstructor
class Rookie {
    @JsonElement(name = "Rookie_name")
    String name;
    @JsonElement(name = "Rookie_age")
    int age;
}

public class JsonElementTest {

    JavaJsonMapper mapper = new JavaJsonMapper();

    @Test
    void receivingRegularFieldNameWhenUsingAnnoThanChangeTheFieldNameInJson() {
        //Given
        Rookie rookie = new Rookie("Dumbbell Rookie", 1);

        //When
        var jsonRookie = mapper.toJson(rookie);

        //Then
        assertThat(jsonRookie).isEqualTo("{\"Rookie_name\":\"Dumbbell Rookie\",\"Rookie_age\":1}");
    }
}


