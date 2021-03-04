package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class HToS {

    public void htos_samfile(String samfile, String outfile, int seq_length) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(samfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        ArrayList<SamUnit> as = new ArrayList<SamUnit>();
        String line = null;
        String read1 = null;
        String read2 = null;
        Pattern pattern1 = Pattern.compile("^[\\d]{1,}M[\\d]{1,}H$");
        Pattern pattern2 = Pattern.compile("^[\\d]{1,}H[\\d]{1,}M$");
        Pattern patternms = Pattern.compile("^[\\d]{1,}M[\\d]{1,}S$");
        Pattern patternsm = Pattern.compile("^[\\d]{1,}S[\\d]{1,}M$");
        Pattern patternmim = Pattern.compile("^[\\d]{1,}M[\\d]{1,}I[\\d]{1,}M$");
        Pattern patternmdm = Pattern.compile("^[\\d]{1,}M[\\d]{1,}D[\\d]{1,}M$");
        while ((line = br.readLine()) != null) {
            if (line.startsWith("@")) {
                continue;
            } else {
                SamUnit s = new SamUnit(line);
                if (as.isEmpty()) {
                    if (s.seq.length() == seq_length) {
                        if (((s.flag & 64) == 64) && read1 == null)
                            read1 = s.seq;
                        if (((s.flag & 128) == 128) && read2 == null)
                            read2 = s.seq;
                    }
                    as.add(s);
                } else {
                    SamUnit pre = as.get(0);
                    if (s.name.equals(pre.name)) {
                        if (s.seq.length() == seq_length) {
                            if (((s.flag & 64) == 64) && (read1 == null))
                                read1 = s.seq;
                            if (((s.flag & 128) == 128) && (read2 == null))
                                read2 = s.seq;
                        }
                        as.add(s);
                    } else {
                        for (int i = 0; i < as.size(); i++) {
                            SamUnit u = as.get(i);
                            if (pattern1.matcher(u.cigar).find() || pattern2.matcher(u.cigar).find()) {
                                u.cigar = u.cigar.replace('H', 'S');
                                if (((u.flag & 64) == 64) && read1 != null)
                                    u.seq = read1;
                                if (((u.flag & 128) == 128) && read2 != null)
                                    u.seq = read2;
                            }
                            if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find() || patternmim.matcher(u.cigar).find() || patternmdm.matcher(u.cigar).find()) {
                                bw.write(u.toString());
                                bw.newLine();
                            }
                        }
                        read1 = null;
                        read2 = null;
                        as.clear();
                        if (s.seq.length() == seq_length) {
                            if ((s.flag & 64) == 64)
                                read1 = s.seq;
                            if ((s.flag & 128) == 128)
                                read2 = s.seq;
                        }
                        as.add(s);
                    }
                }
            }

        }
        if (!as.isEmpty()) {
            for (int i = 0; i < as.size(); i++) {
                SamUnit u = as.get(i);
                if (pattern1.matcher(u.cigar).find() || pattern2.matcher(u.cigar).find()) {
                    u.cigar = u.cigar.replace("H", "S");
                    if ((u.flag & 64) == 64 && read1 != null)
                        u.seq = read1;
                    if ((u.flag & 128) == 128 && read2 != null)
                        u.seq = read2;
                }
                if (patternsm.matcher(u.cigar).find() || patternms.matcher(u.cigar).find() || patternmim.matcher(u.cigar).find() || patternmdm.matcher(u.cigar).find()) {
                    bw.write(u.toString());
                    bw.newLine();
                }
            }
        }
        bw.flush();
        bw.close();
        br.close();
        as = null;
    }
}
