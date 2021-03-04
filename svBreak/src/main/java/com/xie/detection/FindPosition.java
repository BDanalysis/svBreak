package com.xie.detection;

import java.io.BufferedReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;


public class FindPosition {
    ArrayList<PositionUnit> findposition(String matchfile, String positionfile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(matchfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(positionfile));
        ArrayList<SamUnit> as = new ArrayList<SamUnit>();
        ArrayList<PositionUnit> ps = new ArrayList<PositionUnit>();
        String line = null;
        String state = null;
        boolean smflag = false;
        boolean msflag = false;
        Pattern patternms = Pattern.compile("^[\\d]{1,}M[\\d]{1,}S$");
        Pattern patternsm = Pattern.compile("^[\\d]{1,}S[\\d]{1,}M$");
        while ((line = br.readLine()) != null) {
            if (line.startsWith("@")) {
                continue;
            } else {
                SamUnit s = new SamUnit(line);
                if (as.isEmpty()) {
                    if (patternsm.matcher(s.cigar).find())
                        smflag = true;
                    if (patternms.matcher(s.cigar).find())
                        msflag = true;
                    as.add(s);
                } else {
                    SamUnit pre = as.get(0);
                    if (s.name.equals(pre.name)) {
                        if (patternsm.matcher(s.cigar).find())
                            smflag = true;
                        if (patternms.matcher(s.cigar).find())
                            msflag = true;
                        as.add(s);
                    } else {
                        if (smflag && msflag) {
                            for (int i = 0; i < as.size(); i++) {
                                SamUnit u = as.get(i);
                                if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                                    if (patternsm.matcher(u.cigar).find()) state = "SM";
                                    else state = "MS";
                                    boolean flag = false;
                                    for (int x = 0; x < ps.size(); x++) {
                                        PositionUnit psu = ps.get(x);
                                        if (psu.comparaposition(u.splitpos)) {
                                            psu.changestate(u.splitpos, state);
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if (!flag && u.splitpos != -1) {
                                        ps.add(new PositionUnit(u.splitpos, state));
                                    }
                                }
                            }
                            int count1 = 0;
                            int count2 = 0;
                            for (int i = 0; i < as.size(); i++) {
                                SamUnit u = as.get(i);
                                if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                                    for (int x = 0; x < ps.size(); x++) {
                                        PositionUnit psu = ps.get(x);
                                        if (psu.comparaposition(u.splitpos)) {
                                            if (psu.ms && psu.sm) {
                                                if ((u.flag & 64) == 64)
                                                    count1++;
                                                else
                                                    count2++;
                                            }

                                        }
                                    }
                                }
                            }
                            if (count1 > 1 || count2 > 1) {
                                for (int i = 0; i < as.size(); i++) {
                                    SamUnit u = as.get(i);
                                    if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                                        for (int x = 0; x < ps.size(); x++) {
                                            PositionUnit psu = ps.get(x);
                                            if (psu.comparaposition(u.splitpos)) {
                                                psu.isnot = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        smflag = false;
                        msflag = false;
                        as.clear();
                        if (patternsm.matcher(s.cigar).find())
                            smflag = true;
                        if (patternms.matcher(s.cigar).find())
                            msflag = true;
                        as.add(s);
                    }
                }
            }

        }
        if (!as.isEmpty()) {
            if (smflag && msflag) {
                for (int i = 0; i < as.size(); i++) {
                    SamUnit u = as.get(i);
                    if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                        if (patternsm.matcher(u.cigar).find()) state = "SM";
                        else state = "MS";
                        boolean flag = false;
                        for (int x = 0; x < ps.size(); x++) {
                            PositionUnit psu = ps.get(x);
                            if (psu.comparaposition(u.splitpos)) {
                                psu.changestate(u.splitpos, state);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag && u.splitpos != -1) {
                            ps.add(new PositionUnit(u.splitpos, state));
                        }

                    }
                }
                int count1 = 0;
                int count2 = 0;
                for (int i = 0; i < as.size(); i++) {
                    SamUnit u = as.get(i);
                    if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                        for (int x = 0; x < ps.size(); x++) {
                            PositionUnit psu = ps.get(x);
                            if (psu.comparaposition(u.splitpos)) {
                                if (psu.ms && psu.sm) {
                                    if ((u.flag & 64) == 64)
                                        count1++;
                                    else
                                        count2++;
                                }
                            }
                        }
                    }
                }
                if (count1 > 1 || count2 > 1) {
                    for (int i = 0; i < as.size(); i++) {
                        SamUnit u = as.get(i);
                        if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find()) {
                            for (int x = 0; x < ps.size(); x++) {
                                PositionUnit psu = ps.get(x);
                                if (psu.comparaposition(u.splitpos)) {
                                    psu.isnot = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(ps);
        for (PositionUnit i : ps) {
            if (!i.isnot && i.ms & i.sm) {
                bw.write(String.valueOf(i.position) + "\t" + "11");
                bw.newLine();
            }
        }
        bw.flush();
        bw.close();
        br.close();
        as = null;
        return ps;
    }
}
