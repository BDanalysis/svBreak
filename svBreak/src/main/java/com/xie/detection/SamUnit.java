package com.xie.detection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SamUnit {
    String name;
    int flag;
    String chr;
    int pos;
    String mapq;
    String cigar;
    String mchr;
    int mpos;
    int isize;
    String seq;
    int splitpos;
    String state = null;

    SamUnit(String samline) {
        String[] temp = samline.split("\t");
        name = temp[0];
        flag = Integer.parseInt(temp[1]);
        chr = temp[2];
        pos = Integer.parseInt(temp[3]);
        mapq = temp[4];
        cigar = temp[5];
        mchr = temp[6];
        mpos = Integer.parseInt(temp[7]);
        isize = Integer.parseInt(temp[8]);
        seq = temp[9];
        state = cigarstate(cigar);
        splitpos = calsplitpos(pos, cigar, state);
    }

    public static String cigarstate(String cigar) {
        Pattern pattern1 = Pattern.compile("^[\\d]{1,}M[\\d]{1,}H$");
        Pattern pattern2 = Pattern.compile("^[\\d]{1,}H[\\d]{1,}M$");
        Pattern patternms = Pattern.compile("^[\\d]{1,}M[\\d]{1,}S$");
        Pattern patternsm = Pattern.compile("^[\\d]{1,}S[\\d]{1,}M$");
        Pattern patternmim = Pattern.compile("^[\\d]{1,}M[\\d]{1,}I[\\d]{1,}M$");
        Pattern patternmdm = Pattern.compile("^[\\d]{1,}M[\\d]{1,}D[\\d]{1,}M$");
        String state = null;
        if (pattern1.matcher(cigar).find()) {
            return state = "MH";
        }
        if (pattern2.matcher(cigar).find()) {
            return state = "HM";
        }
        if (patternms.matcher(cigar).find()) {
            return state = "MS";
        }
        if (patternsm.matcher(cigar).find()) {
            return state = "SM";
        }
        if (patternmim.matcher(cigar).find()) {
            return state = "MIM";
        }
        if (patternmdm.matcher(cigar).find()) {
            return state = "MDM";
        }
        return state;
    }

    public static int calsplitpos(int pos, String cigar, String state) {
        if (state == null) return -1;
        if (state.equals("SM")) return pos;
        if (state.equals("MS")) {
            Pattern patternms = Pattern.compile("^([\\d]{1,})M([\\d]{1,})S$");
            Matcher matcher = patternms.matcher(cigar);
            if (matcher.find()) {
                int pianyi = Integer.parseInt(matcher.group(1));
                return pos + pianyi;
            }
        }
        if (state.equals("MIM")) {
            Pattern patternmim = Pattern.compile("^([\\d]{1,})M([\\d]{1,})I([\\d]{1,})M$");
            Matcher matcher = patternmim.matcher(cigar);
            if (matcher.find()) {
                int pianyi = Integer.parseInt(matcher.group(1));
                return pos + pianyi;
            }
        }
        if (state.equals("MDM")) {
            Pattern patternmdm = Pattern.compile("^([\\d]{1,})M([\\d]{1,})D([\\d]{1,})M$");
            Matcher matcher = patternmdm.matcher(cigar);
            if (matcher.find()) {
                int pianyi = Integer.parseInt(matcher.group(1));
                return pos + pianyi;
            }
        }
        return -1;
    }

    public String toString() {
        return name + "\t" + flag + "\t" + chr + "\t" + pos
                + "\t" + mapq + "\t" + cigar + "\t" + mchr + "\t" + mpos + "\t" + isize + "\t" + seq;
    }

}
