package com.xie.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExtractUnmatchReads {
    void extractunmatchreads(String namelist, String fq, String extractfq, String shdir) {
        ProcessBuilder pb = new ProcessBuilder("./seqtk.sh", fq, namelist, extractfq);
        pb.directory(new File(shdir));
        int runningStatus = 0;
        String s = null;
        try {
            Process p = pb.start();
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            try {
                runningStatus = p.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
        if (runningStatus != 0) {
            System.out.println(runningStatus);
        }
    }

}
