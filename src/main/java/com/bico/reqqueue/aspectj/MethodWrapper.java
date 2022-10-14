package com.bico.reqqueue.aspectj;

import java.lang.reflect.Method;

/**
 * @author Eric Zhao
 */
public class MethodWrapper {

    private final Method method;
    private final boolean present;

    private MethodWrapper(Method method, boolean present) {
        this.method = method;
        this.present = present;
    }

    static MethodWrapper wrap(Method method) {
        if (method == null) {
            return none();
        }
        return new MethodWrapper(method, true);
    }

    static MethodWrapper none() {
        return new MethodWrapper(null, false);
    }

    Method getMethod() {
        return method;
    }

    boolean isPresent() {
        return present;
    }
}

