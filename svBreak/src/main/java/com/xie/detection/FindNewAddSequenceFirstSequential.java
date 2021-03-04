package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindNewAddSequenceFirstSequential {

    void findnewaddsequence(String positionfile, String matchfile, String pos_seq_file, int insertsize_avg, String fasta) throws IOException {
        BufferedReader br_pos = new BufferedReader(new FileReader(positionfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(pos_seq_file));
        ArrayList<PositionUnit> ps = new ArrayList<PositionUnit>();
        String line_pos = null;
        while ((line_pos = br_pos.readLine()) != null) {
            String[] temp = line_pos.split("\t");
            PositionUnit p = new PositionUnit(Integer.parseInt(temp[0]), temp[1]);
            ps.add(p);
        }
        br_pos.close();
        BufferedReader br = new BufferedReader(new FileReader(matchfile));
        String line = null;
        while ((line = br.readLine()) != null) {
            SamUnit su = new SamUnit(line);
            for (PositionUnit p : ps) {
                if (p.position > (su.splitpos + 100)) {
                    break;
                }
                if (p.ms && p.sm) {
                    if (p.comparaposition(su.splitpos)) {
                        if (su.state.equals("MS")) {
                            if (p.arr_ms == null) {
                                p.arr_ms = new ArrayList<SamUnit>();
                                p.arr_ms.add(su);
                            } else p.arr_ms.add(su);
                        } else {
                            if (su.state.equals("SM")) {
                                if (p.arr_sm == null) {
                                    p.arr_sm = new ArrayList<SamUnit>();
                                    p.arr_sm.add(su);
                                } else p.arr_sm.add(su);
                            } else {
                                if (su.state.equals("MIM")) {
                                    if (p.arr_mim == null) {
                                        p.arr_mim = new ArrayList<SamUnit>();
                                        p.arr_mim.add(su);
                                    } else p.arr_mim.add(su);
                                } else {
                                    if (su.state.equals("MDM")) {
                                        if (p.arr_mdm == null) {
                                            p.arr_mdm = new ArrayList<SamUnit>();
                                            p.arr_mdm.add(su);
                                        } else p.arr_mdm.add(su);
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        br.close();
        for (PositionUnit p : ps) {
            if (p.arr_ms != null && p.arr_ms.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_sm.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2) || (Integer.valueOf(s.mapq) < 20))
                        i.remove();
                }
            }
            if (p.arr_sm != null && p.arr_sm.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_ms.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2) || (Integer.valueOf(s.mapq) < 20))
                        i.remove();
                }
            }
            if (p.arr_ms != null && p.arr_ms.size() != 0) {
                Collections.sort(p.arr_ms, new comparems());
            }
            if (p.arr_sm != null && p.arr_sm.size() != 0) {
                Collections.sort(p.arr_sm, new comparesm());
            }
            if (p.arr_ms != null && p.arr_ms.size() != 0 && p.arr_sm != null && p.arr_sm.size() != 0) {
                String fs = findsequence(p.arr_ms.get(0), p.arr_sm.get(0));
                if (fs != null) {
                    bw.write(fs);
                    bw.newLine();
                }
            }
            p.clear();
        }
        bw.flush();
        bw.close();
    }

    final public String findsequence(SamUnit ms, SamUnit sm) {
        Pattern patternsm = Pattern.compile("^([\\d]{1,})S([\\d]{1,})M$");
        int pos = ms.splitpos - 1;
        String s = ms.seq.substring(ms.splitpos - ms.pos);
        Matcher matchersm = patternsm.matcher(sm.cigar);
        int pianyi = 0;
        if (matchersm.find()) {
            pianyi = Integer.parseInt(matchersm.group(1));
        }
        String sub = sm.seq.substring(pianyi).substring(0, 10);
        if (ms.splitpos <= sm.splitpos) {
            if (s.contains(sub) && s.indexOf(sub) != 0) {
                s = s.substring(0, s.indexOf(sub));
            }
        } else if (ms.splitpos > sm.splitpos) {
            int cha = ms.splitpos - sm.splitpos;
            pos = sm.splitpos - 1;
            s = ms.seq.substring(ms.splitpos - ms.pos - cha);
            if (s.contains(sub) && s.indexOf(sub) != 0) {
                s = s.substring(0, s.indexOf(sub));
            }
        } else return null;
        if (s.length() <= 20)
            return null;
        return pos + "\t" + s;
    }
}
