package com.xie.detection;

import java.util.ArrayList;

public class PositionUnit implements Comparable<PositionUnit> {
    int position;
    boolean sm;
    boolean ms;
    public int ms_max = 0;            //����λ���ڵ�����������δ�ض��ϵ����е���󳤶ȣ�
    public int ms_min = 0;            //����λ���ڵ�����������δ�ض��ϵ����е���С���ȣ�
    public int sm_max = 0;
    public int sm_min = 0;
    boolean isnot = false;            //当其位点与其对应位点均为ms和sm都有时，将其设为true
    public ArrayList<SamUnit> arr_ms = null;    //����λ���ڶ�Ӧ������sam��Ԫ
    public ArrayList<SamUnit> arr_sm = null;
    public ArrayList<SamUnit> arr_mim = null;
    public ArrayList<SamUnit> arr_mdm = null;

    public PositionUnit(int position, String state) {
        this.position = position;
        if (state.equals("MS") || state.equals("10")) {
            this.ms = true;
            this.sm = false;
        } else {
            if (state.equals("SM") || state.equals("01")) {
                this.sm = true;
                this.ms = false;
            } else if (state.equals("11")) {
                this.sm = true;
                this.ms = true;
                arr_ms = new ArrayList<SamUnit>();
                arr_sm = new ArrayList<SamUnit>();
            }
        }
    }

    public PositionUnit(int position) {
        this.position = position;
    }

    boolean comparaposition(int otherposition) {
        if ((this.position - 6) <= otherposition && (this.position + 6) >= otherposition)
            return true;
        else return false;
    }

    void changestate(int position, String state) {
        if (this.ms) {
            if (state.equals("SM") && !this.sm) this.sm = true;
        } else {
            if (state.equals("MS")) {
                this.ms = true;
                this.position = position;
            } else {
                if (state.equals("SM") && !this.sm) this.sm = true;
            }

        }
    }

    @Override
    public int compareTo(PositionUnit o) {
        if (this.position > o.position) return 1;
        else return -1;
    }

    public void clear() {
        this.arr_ms = null;
        this.arr_mdm = null;
        this.arr_sm = null;
    }
}
