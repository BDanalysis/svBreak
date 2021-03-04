package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class findMatchH {
    public void findmatchH(String samfile, String outfile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(samfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        ArrayList<String> as = new ArrayList<String>();
        boolean flag = false;
        String line = null;
        Pattern pattern1 = Pattern.compile("^[\\d]{1,}M[\\d]{1,}H$");
        Pattern pattern2 = Pattern.compile("^[\\d]{1,}H[\\d]{1,}M$");
        while ((line = br.readLine()) != null) {
            if (line.startsWith("@")) {
                continue;
            } else {
                String[] temp = line.split("\t");
                String cigar = temp[5];
                String name = temp[0];
                if (as.isEmpty()) {
                    if (pattern1.matcher(cigar).find() || pattern2.matcher(cigar).find())
                        flag = true;
                    as.add(line);
                } else {
                    String pre = as.get(0);
                    String[] temp_pre = pre.split("\t");
                    String name_pre = temp_pre[0];
                    if (name.equals(name_pre)) {
                        if (pattern1.matcher(cigar).find() || pattern2.matcher(cigar).find())
                            flag = true;
                        as.add(line);
                    } else {
                        if (flag) {
                            for (int i = 0; i < as.size(); i++) {
                                bw.write(as.get(i));
                                bw.newLine();
                            }
                            flag = false;
                        }
                        as.clear();
                        if (pattern1.matcher(cigar).find() || pattern2.matcher(cigar).find())
                            flag = true;
                        as.add(line);
                    }
                }
            }

        }
        if (!as.isEmpty()) {
            if (flag) {
                for (int i = 0; i < as.size(); i++) {
                    bw.write(as.get(i));
                    bw.newLine();
                }
            }
        }
        bw.flush();
        br.close();
        bw.close();
    }

}
