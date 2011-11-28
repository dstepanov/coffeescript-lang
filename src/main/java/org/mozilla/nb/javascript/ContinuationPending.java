package org.mozilla.nb.javascript;


/**
 * Exception thrown by 
 * {@link org.mozilla.nb.javascript.Context#executeScriptWithContinuations(Script, Scriptable)}
 * and {@link org.mozilla.nb.javascript.Context#callFunctionWithContinuations(Callable, Scriptable, Object[])}
 * when execution encounters a continuation captured by
 * {@link org.mozilla.nb.javascript.Context#captureContinuation()}.
 * Exception will contain the captured state needed to restart the continuation
 * with {@link org.mozilla.nb.javascript.Context#resumeContinuation(Object, Scriptable, Object)}.
 * @author Norris Boyd
 */
public class ContinuationPending extends RuntimeException {
    private static final long serialVersionUID = 4956008116771118856L;
    private NativeContinuation continuationState;
    private Object applicationState;
    
    /**
     * Construct a ContinuationPending exception. Internal call only;
     * users of the API should get continuations created on their behalf by
     * calling {@link org.mozilla.nb.javascript.Context#executeScriptWithContinuations(Script, Scriptable)}
     * and {@link org.mozilla.nb.javascript.Context#callFunctionWithContinuations(Callable, Scriptable, Object[])}
     * @param continuationState Internal Continuation object
     */
    ContinuationPending(NativeContinuation continuationState) {
        this.continuationState = continuationState;
    }
    
    /**
     * Get continuation object. The only
     * use for this object is to be passed to 
     * {@link org.mozilla.nb.javascript.Context#resumeContinuation(Object, Scriptable, Object)}.
     * @return continuation object
     */
    public Object getContinuation() {
        return continuationState;
    }
    
    /**
     * @return internal continuation state
     */
    NativeContinuation getContinuationState() {
        return continuationState;
    }
    
    /**
     * Store an arbitrary object that applications can use to associate
     * their state with the continuation.
     * @param applicationState arbitrary application state
     */
    public void setApplicationState(Object applicationState) {
        this.applicationState = applicationState;
    }

    /**
     * @return arbitrary application state
     */
    public Object getApplicationState() {
        return applicationState;
    }
}
