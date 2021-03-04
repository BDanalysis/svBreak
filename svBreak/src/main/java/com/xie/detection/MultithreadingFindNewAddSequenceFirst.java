package com.xie.detection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultithreadingFindNewAddSequenceFirst implements Callable<String> {
    String matchfile;
    int insertsize_avg;
    String fasta;
    String unmatchfq1;
    String unmatchfq2;
    PositionUnit p;

    public MultithreadingFindNewAddSequenceFirst(PositionUnit pu, String matchfile, int insertsize_avg, String fasta, String unmatchfq1, String unmatchfq2) {
        this.matchfile = matchfile;
        this.insertsize_avg = insertsize_avg;
        this.fasta = fasta;
        this.unmatchfq1 = unmatchfq1;
        this.unmatchfq2 = unmatchfq2;
        this.p = pu;
    }

    public String call() throws Exception {
        if (p.ms && p.sm) {
            String line = null;
            BufferedReader br = new BufferedReader(new FileReader(matchfile));
            while ((line = br.readLine()) != null) {
                SamUnit su = new SamUnit(line);
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
                }
            }
            br.close();
        }

        if (p.ms && p.sm) {
            if (p.arr_mdm != null && p.arr_mdm.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_mdm.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2) || (Integer.parseInt(s.mapq) < 20))
                        i.remove();
                }
            }

            if (p.arr_mdm != null && p.arr_mdm.size() > 3) {
                return null;
            }
            if (p.arr_ms != null && p.arr_ms.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_sm.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2) || (Integer.parseInt(s.mapq) < 20))
                        i.remove();
                }
            }
            if (p.arr_sm != null && p.arr_sm.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_ms.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2) || (Integer.parseInt(s.mapq) < 20))
                        i.remove();
                }
            }
            if (p.arr_mim != null && p.arr_mim.size() != 0) {
                for (Iterator<SamUnit> i = p.arr_mim.iterator(); i.hasNext(); ) {
                    SamUnit s = i.next();
                    if ((s.isize > 0 ? s.isize : -s.isize) > (insertsize_avg * 2))
                        i.remove();
                }
            }
            if ((p.arr_ms == null || p.arr_ms.size() == 0) && p.arr_mim != null && p.arr_mim.size() != 0) {
                Collections.sort(p.arr_mim, new comparemim());
                String fs = findsequenceI(p.arr_mim.get(0));
                if (fs != null) {
                    return fs;
                }
            }
            if (p.arr_ms != null && p.arr_ms.size() != 0) {
                Collections.sort(p.arr_ms, new comparems());
            }
            if (p.arr_sm != null && p.arr_sm.size() != 0) {
                Collections.sort(p.arr_sm, new comparesm());
            }
        }
        if (p.arr_ms != null && p.arr_ms.size() != 0 && p.arr_sm != null && p.arr_sm.size() != 0) {
            String fs = findsequence(p.arr_ms.get(0), p.arr_sm.get(0));
            if (fs != null) {
                return fs;
            }
        }

        return null;
    }

    final public String findsequenceI(SamUnit mim) {
        Pattern patternmim = Pattern.compile("^([\\d]{1,})M([\\d]{1,})I([\\d]{1,})M$");
        Matcher matchermim = patternmim.matcher(mim.cigar);
        int m1 = 0;
        int i = 0;
        if (matchermim.find()) {
            m1 = Integer.parseInt(matchermim.group(1));
            i = Integer.parseInt(matchermim.group(2));
        } else return null;
        int pos = mim.splitpos - 1;
        String s = mim.seq.substring(m1, m1 + i);
        return String.valueOf(pos) + "\t" + s;
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
        String sub = sm.seq.substring(pianyi).substring(0, 6);
        if (ms.splitpos == sm.splitpos) {
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
        return pos + "\t" + s;
    }

}


