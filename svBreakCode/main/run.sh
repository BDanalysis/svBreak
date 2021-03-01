#!/bin/bash
/home/shaoqiangwang/BREAKPOINT/SInc/SInC_readGen -C 20 -T 1 -R 100 sim_all_mutation_10_24.fa /home/shaoqiangwang/BREAKPOINT/SInc/100_bp_read_1_profile.txt  /home/shaoqiangwang/BREAKPOINT/SInc/100_bp_read_2_profile.txt
#在SInC文件夹下执行
./SInC_readGen -C 30 -T 1 -R 100 sim_all_mutation_10_24.fa 100_bp_read_1_profile.txt 100_bp_read_2_profile.txt
bwa index chr21.fa
bwa mem chr21.fa sim_all_mutation_10_24.fa_1_300_50_30.0_100.fq sim_all_mutation_10_24.fa_2_300_50_30.0_100.fq >all_mutation_10_24.sam
#/home/xuxiangyan/文档/program/truedatatest/seqtk-master/./seqtk subseq sim_all_mutation.fa_1_300_50_20.0_100.fq pair_read_name.lst >out1.fq
#/home/xuxiangyan/文档/program/truedatatest/seqtk-master/./seqtk subseq sim_all_mutation.fa_2_300_50_20.0_100.fq pair_read_name.lst >out2.fq
