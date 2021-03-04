package com.xie.detection;

import com.xie.output.MutationOutput;

import java.io.*;

public class RunAPI {
    public void run(GuiRunUnit gunit) throws Exception {
        int i = 1;
        int insertsize = 0;
        String fasta = gunit.fastafile;
        String fq1 = gunit.fastqfile1;
        String fq2 = gunit.fastqfile2;
        String tempdir = gunit.cache + "/svBreakinstemp";
        String bwa = "bwa";
        String seqtk = "seqtk";
        String chr = gunit.chr;
        mkdir(tempdir);
        makeSh(tempdir, bwa, seqtk);
        NoMsPositionRealign.temppath = tempdir + "/temp";
        NoMsPositionRealign.shdir = tempdir;
        FastaToLow ftl = new FastaToLow();
        ftl.tolow(fasta, tempdir + "/low.fa", chr);
        ftl = null;
        FastqRename fr = new FastqRename();
        int seqlength = fr.fastqrename(fq1, fq2, tempdir + "/r1.fq", tempdir + "/r2.fq");
        fr = null;
        while (i < 30) {
            if (i == 1) {
                bwash(tempdir + "/low.fa", tempdir + "/r1.fq", tempdir + "/r2.fq", tempdir + "/" + i + "times.sam", tempdir);
                InsertSize insertcal = new InsertSize();
                insertsize = insertcal.calc_stats(tempdir + "/" + i + "times.sam");
                insertcal = null;
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
                bwash_Y(tempdir + "/" + i + ".fa", tempdir + "/r1.fq", tempdir + "/r2.fq", tempdir + "/" + i + "times.sam", tempdir);
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
        mo.output(tempdir + "/" + i + ".fa", gunit.output + "/insert_result.txt");
        System.out.println("the result file path:" + gunit.output + "/insert_result.txt");
        DeleteFile.deleteDirectory(tempdir);
    }

    public void bwash(String fasta, String fq1, String fq2, String out, String shdir) throws IOException {
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

    public void bwash_Y(String fasta, String fq1, String fq2, String out, String shdir) throws IOException {
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

    public void makeSh(String shdir, String bwa, String seqtk) throws IOException, InterruptedException {
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

    private void mkdir(String dir) {
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
}
