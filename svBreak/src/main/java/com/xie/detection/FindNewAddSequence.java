package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FindNewAddSequence {
    void findnewaddsequence(String positionfile, String matchfile, String pos_seq_file, int insertsize_avg, int insertsize_sd, String fasta, String unmatchfq1, String unmatchfq2) throws IOException {
        BufferedReader br_pos = new BufferedReader(new FileReader(positionfile));

        BufferedWriter bw = new BufferedWriter(new FileWriter(pos_seq_file));
        ArrayList<ArrayList<PositionUnit>> ps = new ArrayList<ArrayList<PositionUnit>>();
        String line_pos = null;
        int count = 0;
        while ((line_pos = br_pos.readLine()) != null) {
            String[] temp = line_pos.split("\t");
            PositionUnit p = new PositionUnit(Integer.parseInt(temp[0]), temp[1]);
            if (count == 0) {
                ArrayList<PositionUnit> p1 = new ArrayList<PositionUnit>();
                p1.add(p);
                ps.add(p1);
                count++;
            } else {
                ps.get(ps.size() - 1).add(p);
                count++;
                if (count == 20)
                    count = 0;
            }
        }
        br_pos.close();
        int core_number = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(core_number * 4);
        ArrayList<Future<String>> arrf = new ArrayList<Future<String>>();
        for (ArrayList<PositionUnit> ap : ps) {
            for (PositionUnit p : ap) {
                Callable<String> c = new MultithreadingFindNewAddSequence(p, matchfile, insertsize_avg, fasta, unmatchfq1, unmatchfq2);
                arrf.add(pool.submit(c));
            }
            for (int i = 0; i < ap.size(); i++) {
                while (true) {
                    if (arrf.get(i).isDone() && !arrf.get(i).isCancelled()) {
                        String s;
                        try {
                            s = arrf.get(i).get();
                            if (s != null) {
                                bw.write(s);
                                bw.newLine();
                            }
                        } catch (InterruptedException | ExecutionException e) {

                            e.printStackTrace();
                        }
                        break;
                    } else
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                }
            }
            arrf.clear();
        }
        bw.flush();
        bw.close();
        pool.shutdown();
    }


}


