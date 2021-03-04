#!/bin/bash
fasta=$1;
fastq1=$2;
fastq2=$3;
output=$4
property=$5;
chr=$6;

java -classpath ../svBreak/target/classes com.xie.detection.MAIN $fasta $fastq1 $fastq2 $output $property $chr
