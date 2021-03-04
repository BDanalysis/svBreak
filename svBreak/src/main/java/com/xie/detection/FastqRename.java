package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FastqRename {
    public int fastqrename(String fq1, String fq2, String fq1_out, String fq2_out) throws IOException {
        BufferedReader br1 = new BufferedReader(new FileReader(fq1));
        BufferedReader br2 = new BufferedReader(new FileReader(fq2));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(fq1_out));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(fq2_out));
        String line1 = null;
        String line2 = null;
        int length = 0;
        boolean errorflag = false;
        while ((line1 = br1.readLine()) != null) {
            line2 = br2.readLine();
            bw1.write(line1);
            bw1.newLine();
            bw2.write(line1);
            bw2.newLine();
            for (int i = 0; i < 3; i++) {
                line1 = br1.readLine();
                line2 = br2.readLine();
                if (i == 0) {
                    int length1 = line1.length();
                    int length2 = line2.length();
                    if (length1 != length2)
                        errorflag = true;
                    if (length != 0 && length != length1 && length != length2)
                        errorflag = true;
                    if (length == 0)
                        length = length1;
                }
                bw1.write(line1);
                bw1.newLine();
                bw2.write(line2);
                bw2.newLine();
            }
        }
        bw2.flush();
        bw1.flush();
        br1.close();
        br2.close();
        bw2.close();
        bw1.close();
        if (errorflag)
            return 0;
        else
            return length;
    }
}
