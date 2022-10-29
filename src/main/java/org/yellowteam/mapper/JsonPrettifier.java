package org.yellowteam.mapper;

import java.util.function.IntConsumer;
/**
 * @author Kuznetsov Illia
 * @since 29/10/2022
 * Class JsonPrettifier represents a prettifier which accepting a regular string with json format text inside
 * and making it easier to read.
 */
public class JsonPrettifier {

    /**
     * Method which receiving String as json text file and int which allow to add in string indention (tabulation) level.
     * Method written using state pattern
     */
    public String prettifyJsonToReadableView(String uglyJsonString, int spaceValue) {
        StringBuilder jsonPrettifyBuilder = new StringBuilder();
        consume = ch -> jsonPrettifyBuilder.append((char) ch);
        state = starterBlock;
        tabulation = 0;
        spaces = spaceValue;
        uglyJsonString.codePoints().forEach(ch -> state.accept(ch));
        return jsonPrettifyBuilder.toString();
    }

    IntConsumer
            consume;
    int spaces;
    int tabulation;
    IntConsumer state;
    IntConsumer starterBlock = ch -> {
        if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.objectBlock;
        } else if (ch == '[') {
            processAndIncreasingTabulation(ch);
            this.state = this.arrayBlock;
        } else if (ch == ',') {
            processAndAddingTabulation(ch);
        } else if (ch == ']') {
            processAndDecreasingTabulation(ch);
        } else if (ch == '}') {
            processAndDecreasingTabulation(ch);
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else if (ch == ':') {
            consume.accept(ch);
            consume.accept(' ');
        } else {
            consume.accept(ch);
        }
    };
    IntConsumer objectBlock = ch -> {
        if (ch == '[') {
            processAndIncreasingTabulation(ch);
            this.state = this.arrayBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.starterBlock;
        }
    };
    IntConsumer arrayBlock = ch -> {
        if (ch == '{') {
            processAndIncreasingTabulation(ch);
            this.state = this.objectBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        }
    };
    IntConsumer innerStringBlock = ch -> {
        if (ch == '\\') {
            this.state = this.escapeBlock;
        } else if (ch == '"') {
            consume.accept(ch);
            this.state = this.starterBlock;
        } else {
            consume.accept(ch);
        }
    };
    IntConsumer escapeBlock = ch -> {
        if ("\"\\/bfnrt".indexOf((char) ch) != -1) {
            consume.accept(ch);
            this.state = this.innerStringBlock;
        } else {
            throw new IllegalArgumentException("Unknown state escape: \\" + (char) ch);
        }
    };

    /**
     * Method which adding indention level on a same level when entering a new line.
     */
    private void processAndAddingTabulation(int ch) {
        consume.accept(ch);
        consume.accept('\n');
        for (int i = 0; i < tabulation; i++) {
            consume.accept(' ');
        }
    }

    /**
     * Method which increasing tabulation when entering a new block with data such as new object or array
     */
    private void processAndIncreasingTabulation(int ch) {
        tabulation += spaces;
        processAndAddingTabulation(ch);
    }

    /**
     * Method which decreasing tabulation when exiting a block.
     */
    private void processAndDecreasingTabulation(int ch) {
        consume.accept('\n');
        tabulation -= spaces;
        for (int i = 0; i < tabulation; i++) {
            consume.accept(' ');
        }
        consume.accept(ch);
    }
}
