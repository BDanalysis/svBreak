package com.xie.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class NoMsPositionRealign {
    static String shdir = "/home/xie/eclipse-workspace/ins";
    static String temppath = "/media/xie/0009A639000F3A82/ins/test1/temp";

    ArrayList<SamUnit> nomspositionrealign(String fasta, String unmatchfq1, String unmatchfq2, int pos, int insertsize) throws IOException, InterruptedException {
        File file = mkdir(pos);
        ArrayList<SamUnit> asu = realign(fasta, pos, unmatchfq1, unmatchfq2, insertsize);
        deleteFile(file);
        return asu;
    }

    ArrayList<SamUnit> realign(String fastafile, int pos, String unmatchfq1, String unmatchfq2, int insertsize) throws IOException, InterruptedException {
        BufferedReader br1 = new BufferedReader(new FileReader(fastafile));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(temppath + pos + "/temp.fa"));
        StringBuilder sb = new StringBuilder();
        String line1 = null;
        while ((line1 = br1.readLine()) != null) {
            if (line1.startsWith(">")) {
                bw1.write(line1);
                bw1.newLine();
                continue;
            }
            sb.append(line1);
            if (sb.length() > pos + 1000)
                break;

        }
        br1.close();
        bw1.write(sb.substring(pos - 1000, pos + 1000));
        bw1.newLine();
        bw1.flush();
        bw1.close();
        ProcessBuilder builder = new ProcessBuilder("/bin/chmod", "755", "./bwa_noPositionRealign.sh");
        Process process = builder.start();
        process.waitFor();
        ProcessBuilder pb = new ProcessBuilder("./bwa_noPositionRealign.sh", temppath + pos + "/temp.fa", unmatchfq1, unmatchfq2);
        pb.directory(new File(shdir));
        int runningStatus = 0;
        String s = null;
        ArrayList<SamUnit> asu = new ArrayList<>();
        try {
            Process p = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                if (s.startsWith("@"))
                    continue;
                else {
                    String[] temp = s.split("\t");
                    int flag = Integer.parseInt(temp[1]);
                    if (((flag & 4) == 4) || ((flag & 8) == 8))
                        continue;
                    else {
                        SamUnit samUnit = new SamUnit(s);
                        if (samUnit.state != null && samUnit.state.equals("MS") && samUnit.splitpos >= 995 && samUnit.splitpos <= 1006 && samUnit.isize < insertsize * 2 && samUnit.isize > -insertsize * 2) {
                            int m = samUnit.splitpos - samUnit.pos;
                            samUnit.pos = pos - 1000 + samUnit.pos;
                            samUnit.splitpos = samUnit.pos + m;
                            samUnit.mpos = pos - 1000 + samUnit.mpos;
                            asu.add(samUnit);
                        }
                    }
                }
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
            System.out.println(this.getClass().getName() + " error status: " + runningStatus);
        }
        return asu;

    }

    File mkdir(int pos) {
        File file = new File(temppath + pos);
        if (!file.exists() && !file.isDirectory()) {
            boolean isSuccess = file.mkdir();
            if (!isSuccess) System.out.println("Failed to create directory.");
        }
        return file;
    }

    void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                boolean isSuccess = file.delete();
                if (!isSuccess) System.out.println("Failed to delete file.");
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
                    this.deleteFile(files[i]);
                }
                boolean isSuccess = file.delete();
                if (!isSuccess) System.out.println("Failed to delete file.");
                files = null;
            }
        } else {
            System.out.println("��ɾ�����ļ�������");
        }
        file = null;
    }
}
