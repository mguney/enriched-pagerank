package com.gun3y.pagerank.web.d3;

public class Stat {
    public double value;

    public int id;

    public Stat(int id, int incoming, int outgoing) {
        super();
        this.value = (double) incoming * 0.7 + (double) outgoing * 0.3;
        this.id = id;
    }

}
