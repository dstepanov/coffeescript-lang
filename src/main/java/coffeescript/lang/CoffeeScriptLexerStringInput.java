package coffeescript.lang;

/**
 * @author Denis Stepanov
 */
public class CoffeeScriptLexerStringInput implements CoffeeScriptLexerInput {

    private final String text;
    private int index, offset, limit, eofMiss;

    public CoffeeScriptLexerStringInput(String text) {
        this.text = text;
        this.limit = text.length();
    }

    public int read() {
        if (index >= limit) {
            eofMiss++;
            return CoffeeScriptLexerInput.EOF;
        }
        int c = text.codePointAt(index++);
        return c;
    }

    public void backup(int count) {
        if (eofMiss > 0) {
            eofMiss -= count;
            count = eofMiss < 0 ? -eofMiss : 0;
        }
        if (count > 0) {
            index -= count;
        }
    }

    public String readText() {
        return text.substring(offset, index);
    }

    public int readLength() {
        return index - offset;
    }

    public void setTokenOffset() {
        offset = index;
    }

    public int getOffset() {
        return offset;
    }

    public CoffeeScriptLexerInput embedded() {
        // Embedded input is limited to the read length of the previous token
        final int limit = index;
        backup(readLength());
        setTokenOffset();
        CoffeeScriptLexerInput embedded = new CoffeeScriptLexerInput() {

            CoffeeScriptLexerStringInput delegate = CoffeeScriptLexerStringInput.this;
            private int index = delegate.index;
            private int eof;

            public int read() {
                if (index >= limit) {
                    eof++;
                    return CoffeeScriptLexerInput.EOF;
                }
                index++;
                return delegate.read();
            }

            public void backup(int count) {
                if (eof > 0) {
                    eof -= count;
                    count = eof < 0 ? -eof : 0;
                }
                if (count > 0) {
                    index -= count;
                    delegate.backup(count);
                }
            }

            public String readText() {
                return text.substring(offset, index);
            }

            public int readLength() {
                return index - offset;
            }

            public void setTokenOffset() {
                delegate.setTokenOffset();
            }

            public int getOffset() {
                return offset;
            }
        };
        return embedded;
    }
}
