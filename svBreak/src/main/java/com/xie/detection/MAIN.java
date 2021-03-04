package com.xie.detection;

import com.xie.output.MutationOutput;

import java.io.*;
import java.util.Properties;

public class MAIN {
    public static void main(String[] args) throws Exception {
        int i = 1;
        MAIN ma = new MAIN();
        int seqlength = 0;
        int insertsize = 0;
        String fasta = args[0];
        String fq1 = args[1];
        String fq2 = args[2];
        String output = args[3];
        String properties = args[4];
        String chr = args[5];
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(properties));
            pps.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tempdir = pps.getProperty("tempdir") + "/svBreaktemp";
        String bwa = "bwa";
        String seqtk = "seqtk";
        mkdir(tempdir);
        makeSh(tempdir, bwa, seqtk);
        NoMsPositionRealign.temppath = tempdir + "/temp";
        NoMsPositionRealign.shdir = tempdir;
        FastaToLow ftl = new FastaToLow();
        ftl.tolow(fasta, tempdir + "/low.fa", chr);
        ftl = null;
        FastqRename fr = new FastqRename();
        seqlength = fr.fastqrename(fq1, fq2, tempdir + "/r1.fq", tempdir + "/r2.fq");
        fr = null;
        while (i < 30) {
            if (i == 1) {
                ma.bwash(tempdir + "/low.fa", tempdir + "/r1.fq", tempdir + "/r2.fq", tempdir + "/" + i + "times.sam", tempdir);
                InsertSize insertcal = new InsertSize();
                insertsize = insertcal.calc_stats(tempdir + "/" + i + "times.sam");
                findMatchH fmh = new findMatchH();
                fmh.findmatchH(tempdir + "/" + i + "times.sam", tempdir + "/" + i + "times.findh.sam");
                fmh = null;
                HToS h = new HToS();
                h.htos_samfile(tempdir + "/" + i + "times.findh.sam", tempdir + "/" + i + "times.findhtos.sam", seqlength);
                h.htos_samfile(tempdir + "/" + i + "times.sam", tempdir + "/" + i + "times.htos.sam", seqlength);
                h = null;
                FindPosition fp = new FindPosition();
                fp.findposition(tempdir + "/" + i + "times.findhtos.sam", tempdir + "/" + i + "_pos.txt");
                fp = null;
                FindNewAddSequenceFirstSequential fnds = new FindNewAddSequenceFirstSequential();
                fnds.findnewaddsequence(tempdir + "/" + i + "_pos.txt", tempdir + "/" + i + "times.htos.sam", tempdir + "/" + i + "_pos_add.txt", insertsize, tempdir + "/low.fa");
                fnds = null;
                int next = i + 1;
                ConfirmPosition cp = new ConfirmPosition();
                cp.confirmposition(tempdir + "/" + i + "_pos_add.txt", tempdir + "/" + next + "_pos.txt");
                cp = null;
                FinalAdd fa = new FinalAdd();
                boolean flag = fa.finaladd(tempdir + "/low.fa", tempdir + "/" + i + "_pos_add.txt", tempdir + "/" + next + ".fa");
                DeleteFile.delete(tempdir + "/" + i + "times.sam");
                DeleteFile.delete(tempdir + "/" + i + "times.findh.sam");
                DeleteFile.delete(tempdir + "/" + i + "times.findhtos.sam");
                DeleteFile.delete(tempdir + "/" + i + "times.htos.sam");
                if (!flag) break;
            } else {
                ma.bwash_Y(tempdir + "/" + i + ".fa", tempdir + "/r1.fq", tempdir + "/r2.fq", tempdir + "/" + i + "times.sam", tempdir);
                HToS h = new HToS();
                h.htos_samfile(tempdir + "/" + i + "times.sam", tempdir + "/" + i + "times.htos.sam", seqlength);
                h = null;
                FindUnmatchName fun = new FindUnmatchName();
                fun.findunmatchname(tempdir + "/" + i + "times.sam", tempdir + "/" + i + "_unmatchname.list", insertsize);
                fun = null;
                ExtractUnmatchReads eur = new ExtractUnmatchReads();
                eur.extractunmatchreads(tempdir + "/" + i + "_unmatchname.list", tempdir + "/r1.fq", tempdir + "/" + i + "_r1.fq", tempdir);
                eur.extractunmatchreads(tempdir + "/" + i + "_unmatchname.list", tempdir + "/r2.fq", tempdir + "/" + i + "_r2.fq", tempdir);
                eur = null;
                FindNewAddSequenceSequential fnds = new FindNewAddSequenceSequential();
                fnds.findnewaddsequence(tempdir + "/" + i + "_pos.txt", tempdir + "/" + i + "times.htos.sam", tempdir + "/" + i + "_pos_add.txt", insertsize, tempdir + "/" + i + ".fa", tempdir + "/" + i + "_r1.fq", tempdir + "/" + i + "_r2.fq");
                fnds = null;
                int next = i + 1;
                ConfirmPosition cp = new ConfirmPosition();
                cp.confirmposition(tempdir + "/" + i + "_pos_add.txt", tempdir + "/" + next + "_pos.txt");
                cp = null;
                FinalAdd fa = new FinalAdd();
                boolean flag = fa.finaladd(tempdir + "/" + i + ".fa", tempdir + "/" + i + "_pos_add.txt", tempdir + "/" + next + ".fa");
                DeleteFile.delete(tempdir + "/" + i + "times.sam");
                DeleteFile.delete(tempdir + "/" + i + "times.htos.sam");
                DeleteFile.delete(tempdir + "/" + i + "_r1.fq");
                DeleteFile.delete(tempdir + "/" + i + "_r2.fq");
                if (!flag) break;
            }
            System.out.println("第" + i + "次迭代完成");
            i++;
        }
        System.out.println("Iteration " + i + " complete");
        MutationOutput mo = new MutationOutput();
        mo.output(tempdir + "/" + i + ".fa", output + "/insert_result.txt");
        System.out.println("the result file path:" + output + "/insert_result.txt");
        DeleteFile.deleteDirectory(tempdir);
    }

    public void bwash(String fasta, String fq1, String fq2, String out, String shdir) {
        ProcessBuilder pb = new ProcessBuilder("./bwa.sh", fasta, fq1, fq2, out);
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
            System.out.println(" error status: " + runningStatus);
        }
        s = null;
    }

    public void bwash_Y(String fasta, String fq1, String fq2, String out, String shdir) {
        ProcessBuilder pb = new ProcessBuilder("./bwa_Y.sh", fasta, fq1, fq2, out);
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
            System.out.println(" error status: " + runningStatus);
        }
        s = null;
    }

    private static void mkdir(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            DeleteFile.deleteDirectory(dir);
        }
        if (file.isFile()) {
            DeleteFile.deleteFile(dir);
        }
        if (!file.exists()) {
            boolean isSuccess = file.mkdir();
            if (!isSuccess) System.out.println("Failed to create directory.");
        }
    }

    public static void makeSh(String shdir, String bwa, String seqtk) throws IOException, InterruptedException {
        int core_number = Runtime.getRuntime().availableProcessors();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(shdir + "/bwa_Y.sh"));
        bw1.write("#!/bin/bash");
        bw1.newLine();
        bw1.write("fasta=$1");
        bw1.newLine();
        bw1.write("fq1=$2");
        bw1.newLine();
        bw1.write("fq2=$3");
        bw1.newLine();
        bw1.write("out=$4");
        bw1.newLine();
        bw1.write(bwa + " index $fasta");
        bw1.newLine();
        bw1.write(bwa + " mem -Y -t " + core_number + " $fasta $fq1 $fq2 > $out");
        bw1.newLine();
        bw1.flush();
        bw1.close();
        ProcessBuilder builder = new ProcessBuilder("/bin/chmod", "755", "./bwa_Y.sh");
        builder.directory(new File(shdir));
        Process process = builder.start();
        process.waitFor();

        bw1 = new BufferedWriter(new FileWriter(shdir + "/bwa.sh"));
        bw1.write("#!/bin/bash");
        bw1.newLine();
        bw1.write("fasta=$1");
        bw1.newLine();
        bw1.write("fq1=$2");
        bw1.newLine();
        bw1.write("fq2=$3");
        bw1.newLine();
        bw1.write("out=$4");
        bw1.newLine();
        bw1.write(bwa + " index $fasta");
        bw1.newLine();
        bw1.write(bwa + " mem -t " + core_number + " $fasta $fq1 $fq2 > $out");
        bw1.newLine();
        bw1.flush();
        bw1.close();
        builder = new ProcessBuilder("/bin/chmod", "755", "./bwa.sh");
        builder.directory(new File(shdir));
        process = builder.start();
        process.waitFor();

        bw1 = new BufferedWriter(new FileWriter(shdir + "/bwa_noPositionRealign.sh"));
        bw1.write("#!/bin/bash");
        bw1.newLine();
        bw1.write("fasta=$1");
        bw1.newLine();
        bw1.write("fq1=$2");
        bw1.newLine();
        bw1.write("fq2=$3");
        bw1.newLine();
        bw1.write(bwa + " index $fasta");
        bw1.newLine();
        bw1.write(bwa + " mem  $fasta $fq1 $fq2");
        bw1.newLine();
        bw1.flush();
        bw1.close();
        builder = new ProcessBuilder("/bin/chmod", "755", shdir + "/bwa_noPositionRealign.sh");
        builder.directory(new File(shdir));
        process = builder.start();
        process.waitFor();

        bw1 = new BufferedWriter(new FileWriter(shdir + "/seqtk.sh"));
        bw1.write("#!/bin/bash");
        bw1.newLine();
        bw1.write("fq=$1");
        bw1.newLine();
        bw1.write("namelist=$2");
        bw1.newLine();
        bw1.write("out=$3");
        bw1.newLine();
        bw1.write(seqtk + " subseq $fq $namelist > $out");
        bw1.newLine();
        bw1.flush();
        bw1.close();
        builder = new ProcessBuilder("/bin/chmod", "755", shdir + "/seqtk.sh");
        builder.directory(new File(shdir));
        process = builder.start();
        process.waitFor();
    }

}
