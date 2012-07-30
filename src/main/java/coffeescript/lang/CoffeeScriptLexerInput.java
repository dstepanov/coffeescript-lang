package coffeescript.lang;

/**
 * 
 * @author Denis Stepanov
 */
public interface CoffeeScriptLexerInput {

    public static final int EOF = -1;

    int read();

    void backup(int b);

    String readText();

    int readLength();

    void setTokenOffset();
    
}
