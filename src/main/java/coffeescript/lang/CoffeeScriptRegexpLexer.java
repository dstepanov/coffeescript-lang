// Copyright 2011 Denis Stepanov
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package coffeescript.lang;

import static coffeescript.lang.CoffeeScriptRegexpTokenId.*;

/**
 *
 * @author Denis Stepanov
 */
public class CoffeeScriptRegexpLexer extends CoffeeScriptLexerBase<CoffeeScriptRegexpTokenId> {

    private boolean inEmbedded;

    public CoffeeScriptRegexpLexer(CoffeeScriptLexerInput input) {
        super(input);
    }

    public CoffeeScriptRegexpTokenId getNextToken() {
        if (inEmbedded) {
            try {
                if (balancedInterpolatedString("}")) {
                    if (input.readLength() > 1) {
                        input.backup(1);
                        return token(EMBEDDED);
                    } else if (input.readLength() == 0) {
                        return null;
                    }
                }
                return token(REGEXP);
            } finally {
                inEmbedded = false;
            }
        }
        while (true) {
            int ch = input.read();
            switch (ch) {
                case CoffeeScriptLexerInput.EOF:
                    if (input.readLength() > 0) {
                        return token(REGEXP);
                    } else {
                        return null;
                    }
                case '#':
                    if (inputMatch("{")) {
                        inEmbedded = true;
                        return token(REGEXP);
                    } else {
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(REGEXP);
                        }
                        while (true) {
                            int c = input.read();
                            if (c == '\n' || c == CoffeeScriptLexerInput.EOF) {
                                return token(COMMENT);
                            }
                        }
                    }
            }
        }
    }

    public void setState(Object state) {
        inEmbedded = state instanceof Boolean ? (Boolean) state : false;
    }

    public Object state() {
        return inEmbedded;
    }
}
