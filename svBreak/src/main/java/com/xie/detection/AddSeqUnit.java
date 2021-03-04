package com.xie.detection;

public class AddSeqUnit implements Comparable<AddSeqUnit> {
    int pos;
    int seqlength;
    String seq;

    public AddSeqUnit(int pos, String seq) {
        this.pos = pos;
        this.seq = seq;
        this.seqlength = seq.length();
    }

    @Override
    public int compareTo(AddSeqUnit o) {
        if (this.pos > o.pos) return 1;
        return -1;
    }
}
