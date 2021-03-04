package com.xie.detection;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class FindNewAddSequenceSequential {


    void findnewaddsequence(String positionfile, String matchfile, String pos_seq_file, int insertsize_avg, String fasta, String unmatchfq1, String unmatchfq2) throws IOException {
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
        ArrayList<ArrayList<PositionUnit>> pslist = new ArrayList<ArrayList<PositionUnit>>();
        int core_number = Runtime.getRuntime().availableProcessors();
        int count = 0;
        for (PositionUnit p : ps) {
            if (count == 0) {
                ArrayList<PositionUnit> p1 = new ArrayList<PositionUnit>();
                p1.add(p);
                pslist.add(p1);
                count++;
            } else {
                pslist.get(pslist.size() - 1).add(p);
                count++;
                if (count == (core_number))
                    count = 0;
            }
        }
        ps = null;
        ArrayList<Future<String>> arrf = new ArrayList<Future<String>>();
        ExecutorService pool = Executors.newFixedThreadPool(core_number + 1);
        for (ArrayList<PositionUnit> ap : pslist) {
            for (PositionUnit p : ap) {
                Callable<String> c = new MultithreadingFindNewAddSequenceAfterSequential(p, insertsize_avg, fasta, unmatchfq1, unmatchfq2);
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
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                        break;
                    } else
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                }
            }
            arrf.clear();
        }
        pool.shutdown();
        bw.flush();
        bw.close();
    }
}

