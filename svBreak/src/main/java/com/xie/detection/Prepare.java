package com.xie.detection;

import java.io.IOException;

public class Prepare {
    public static void prepare(String fasta, String fq1, String fq2, String tempdir, String chr) throws IOException, IOException {
        FastaToLow ftl = new FastaToLow();
        ftl.tolow(fasta, tempdir + "/low.fa", chr);
        ftl = null;
        FastqRename fr = new FastqRename();
        fr.fastqrename(fq1, fq2, tempdir + "/r1.fq", tempdir + "/r2.fq");
        fr = null;
    }

    public static void main(String[] args) throws Exception {
        String fasta = args[0];
        String fq1 = args[1];
        String fq2 = args[2];
        String tempdir = args[3];
        String chr = args[4];
        FastaToLow ftl = new FastaToLow();
        ftl.tolow(fasta, tempdir + "/low.fa", chr);
        ftl = null;
        FastqRename fr = new FastqRename();
        fr.fastqrename(fq1, fq2, tempdir + "/r1.fq", tempdir + "/r2.fq");
        fr = null;
    }
}
