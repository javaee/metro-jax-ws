package com.sun.xml.ws.api.pipe;

public class ThreadLocalHelper {
    private static final ThreadLocal<Integer> integerHolder = new ThreadLocal<Integer>();
    
    public static void set(Integer i) {
        integerHolder.set(i);
    }

    public static void unset() {
        integerHolder.remove();
    }

    public static Integer get() {
        return integerHolder.get();
    }
}