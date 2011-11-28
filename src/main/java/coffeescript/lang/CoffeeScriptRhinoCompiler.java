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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 * @author Denis Stepanov
 */
public class CoffeeScriptRhinoCompiler implements CoffeeScriptCompiler {

    private final Map<String, Script> scriptCacheMap = new HashMap<String, Script>(1);
    private static CoffeeScriptRhinoCompiler INSTANCE;

    private CoffeeScriptRhinoCompiler() {
    }

    public static synchronized CoffeeScriptRhinoCompiler get() {
        if (INSTANCE == null) {
            return (INSTANCE = new CoffeeScriptRhinoCompiler());
        }
        return INSTANCE;
    }

    public CompilerResult compile(String code, boolean bare) {
        try {
            return new CompilerResult(compileCode(code, bare));
        } catch (StoppedContextException e) {
            return null; // Canceled
        } catch (JavaScriptException e) {
            if (e.getValue() instanceof IdScriptableObject) {
                IdScriptableObject error = (IdScriptableObject) e.getValue();
                String message = (String) ScriptableObject.getProperty(error, "message");
                Pattern pattern = Pattern.compile("(.*) on line (\\d*)(.*)");
                Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    return new CompilerResult(new Error(Integer.valueOf(matcher.group(2)), matcher.group(1) + matcher.group(3), message));
                }
                return new CompilerResult(new Error(-1, "", message));
            }
            return new CompilerResult(new Error(-1, "", e.getMessage()));
        }
    }

    private String compileCode(String code, boolean bare) {
        Context.enter();
        Context ctx = new StoppableContext();
        try {
            ctx.setInstructionObserverThreshold(1);
            ctx.setOptimizationLevel(-1);
            Scriptable scope = ctx.newObject(ctx.initStandardObjects());
            getScriptFromClasspath("coffeescript/lang/resources/coffee-script.js").exec(ctx, scope);
            scope.put("code", scope, code);
            String options = String.format("{bare: %b}", bare);
            String script = String.format("CoffeeScript.compile(code, %s);", options);
            return (String) getScriptFromString(script).exec(ctx, scope);

        } finally {
            Context.exit();
        }
    }

    private Script getScriptFromClasspath(String url) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(url);
            return getScriptFromReader(url, new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        } // Ignore
        return null;
    }

    private Script getScriptFromReader(String key, Reader reader) {
        synchronized (scriptCacheMap) {
            Script script = scriptCacheMap.get(key);
            if (script == null) {
                Context ctx = Context.enter();
                try {
                    ctx.setOptimizationLevel(-1);
                    script = ctx.compileReader(reader, "", 0, null);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot compile from reader", e);
                } finally {
                    Context.exit();
                }
                scriptCacheMap.put(key, script);
            }
            return script;
        }
    }

    private Script getScriptFromString(String string) {
        synchronized (scriptCacheMap) {
            Script script = scriptCacheMap.get(string);
            if (script == null) {
                Context ctx = Context.enter();
                try {
                    ctx.setOptimizationLevel(-1);
                    script = ctx.compileString(string, "", 0, null);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot compile from string", e);
                } finally {
                    Context.exit();
                }
                scriptCacheMap.put(string, script);
            }
            return script;
        }
    }

    public static class StoppableContext extends Context {

        @Override
        protected void observeInstructionCount(int instructionCount) {
            if (Thread.interrupted()) {
                throw new StoppedContextException();
            }
        }
    }

    public static class StoppedContextException extends RuntimeException {
    }
}
