
package test

import spock.lang.*
import coffeescript.lang.*
import static coffeescript.lang.CoffeeScriptTokenId.*

class LexerTest extends spock.lang.Specification {

    def "token with number"() {
        expect:
        tokenize("if i > 987.0011222414341") == [
            [IF, "if", 0],
            [WHITESPACE, " ", 2],
            [IDENTIFIER, "i", 3],
            [WHITESPACE, " ", 4],
            [NONUNARY_OP, ">", 5],
            [WHITESPACE, " ", 6],
            [NUMBER, "987.0011222414341", 7]
        ]
    }
    
    def "simple tokens"() {
        expect:
        tokenize(string) == tokens
        
        where:
        string | tokens
//FIX        "-4" | [[NUMBER, "-4", 0]]
        "0x4" | [[NUMBER, "0x4", 0]]
        "0x0b0" | [[NUMBER, "0x0b0", 0]]
        "0x0B1" | [[NUMBER, "0x0B1", 0]]
        "0xE1" | [[NUMBER, "0xE1", 0]]
//FIX        ".25 + .75" | [[NUMBER, ".25", 0], [WHITESPACE, " ", 1], [NONUNARY_OP, "+", 2], [WHITESPACE, " ", 3], [NUMBER, ".75", 4]]
        "@field" | [[FIELD, "@field", 0]]
        "@ field" | [[AT, "@", 0], [WHITESPACE, " ", 1], [IDENTIFIER, "field", 2]]
        "1+2=3" | [[NUMBER, "1", 0], [NONUNARY_OP, "+", 1], [NUMBER, "2", 2], [NONUNARY_OP, "=", 3], [NUMBER, "3", 4]]
        "1.12345" | [[NUMBER, "1.12345", 0]]
        /"Test#{denis}Xyz"/ | [[STRING, /"Test#{denis}Xyz"/, 0]]
        /'Test#{denis}Xyz'/ | [[SIMPLE_STRING, /'Test#{denis}Xyz'/, 0]]
    }
    
    def tokenize(String i) {
        CoffeeScriptLexerInput input = new CoffeeScriptLexerStringInput(i);
        CoffeeScriptLexer lexer = new CoffeeScriptLexer(input);
        def tokens = []
        CoffeeScriptTokenId token;
        while((token = lexer.nextToken()) != null) {
            tokens <<  [token, input.readText(), input.getOffset()]
        }
        println tokens
        return tokens
    }
    
}

