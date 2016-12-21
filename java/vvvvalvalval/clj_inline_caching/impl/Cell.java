package vvvvalvalval.clj_inline_caching.impl;

public class Cell {
    static private final Object UNSET = new Object();

    static public boolean isOUnset (Object o) {
        return o == UNSET;
    }

    private Object o;

    public Cell(){
        this.o = UNSET;
    }

    public Object get() {
        return o;
    }

    public void set(Object o) {
        this.o = o;
    }
}
