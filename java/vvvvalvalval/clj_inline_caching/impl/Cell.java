package vvvvalvalval.clj_inline_caching.impl;

public class Cell {
    private Object o;

    public Cell(Object o) {
        this.o = o;
    }

    public Object get() {
        return o;
    }

    public void set(Object o) {
        this.o = o;
    }
}
