package com.xie.detection;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class FinalAdd {
    public String chr_name;

    public boolean finaladd(String ref, String final_pos_seq, String newref) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(final_pos_seq));
        String line = null;
        int count = 0;
        line = br.readLine();
        if (line == null) {
            System.out.println("         ");
            br.close();
            return false;
        }
        br.close();
        BufferedReader br1 = new BufferedReader(new FileReader(final_pos_seq));
        String line1 = null;
        ArrayList<AddSeqUnit> seqarr = new ArrayList<AddSeqUnit>();
        while ((line1 = br1.readLine()) != null) {
            String[] temp = line1.split("\t");
            AddSeqUnit p = new AddSeqUnit(Integer.parseInt(temp[0]), temp[1]);
            seqarr.add(p);
            count++;
        }
        br1.close();
        Collections.sort(seqarr);
        BufferedReader br2 = new BufferedReader(new FileReader(ref));
        StringBuilder sb = new StringBuilder();
        String line2 = null;
        while ((line2 = br2.readLine()) != null) {
            if (line2.startsWith(">")) {
                chr_name = line2.substring(1);
                continue;
            } else {
                sb.append(line2);
            }
        }
        br2.close();
        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < seqarr.size(); i++) {
            if (i == 0) {
                if (seqarr.size() == 1) {
                    String s = sb.substring(0, seqarr.get(i).pos);
                    String add = seqarr.get(i).seq;
                    String s1 = sb.substring(seqarr.get(i).pos);
                    sb1.append(s).append(add).append(s1);
                } else {
                    String s = sb.substring(0, seqarr.get(i).pos);
                    String add = seqarr.get(i).seq;
                    sb1.append(s).append(add);
                }
            } else if (i < seqarr.size() - 1) {
                String s = sb.substring(seqarr.get(i - 1).pos, seqarr.get(i).pos);
                String add = seqarr.get(i).seq;
                sb1.append(s).append(add);
            } else {
                String s = sb.substring(seqarr.get(i - 1).pos, seqarr.get(i).pos);
                String add = seqarr.get(i).seq;
                String s1 = sb.substring(seqarr.get(i).pos);
                sb1.append(s).append(add).append(s1);
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(newref));
        bw.write(">" + chr_name);
        bw.newLine();
        for (int i = 0; i < sb1.length(); i += 80) {
            if (i + 80 > sb1.length()) {
                bw.write(sb1.substring(i));
            } else {
                bw.write(sb1.substring(i, i + 80));
            }

        }
        bw.flush();
        bw.close();
        if (count <= 3) return false;
        return true;
    }


}

