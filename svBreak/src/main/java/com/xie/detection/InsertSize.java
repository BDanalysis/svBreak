package com.xie.detection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertSize {
    public int AVG;
    public int SD;
    public int seqlength = 0;

    int calc_stats(String sam_file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(sam_file));
        String line = null;
        long sum = 0;
        int i = 0;
        int avg = 0;
        Pattern pattern = Pattern.compile("^[\\d]{1,}M$");
        while ((line = br.readLine()) != null) {
            if (line.startsWith("@")) {
                continue;
            } else {
                String[] temp = line.split("\t");
                int insertsize = Integer.parseInt(temp[8]);
                String cigar = temp[5];
                int q = Integer.parseInt(temp[1]);
                Matcher matcher = pattern.matcher(cigar);
                if (insertsize > 0 && q == 99 && matcher.find()) {
                    if (seqlength == 0)
                        seqlength = temp[9].length();
                    i++;
                    avg += (insertsize - avg) / i;
                }
            }
        }
        br.close();
        return avg;
    }
}