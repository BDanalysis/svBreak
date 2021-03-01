import sys
import os

with open ("sim_all_mutation_10_24.fa_3_300_50_30.0_100.fq",'w') as outfile:
#这一行是生成的新文件
    with open('sim_all_mutation_10_24.fa_1_300_50_30.0_100.fq','r') as fr1,open ('sim_all_mutation_10_24.fa_2_300_50_30.0_100.fq','r') as fr2:
	#这一行是输入的两个fastq文件，将fq2的文件开头改成fq1
        while 1:
            
            line1 = fr1.readline()
            line2 = fr2.readline()
            if not line1:
                break
            outfile.write(str(line1))
            for i in range(3):
                line1 = fr1.readline()
                line2 = fr2.readline()
                outfile.write(str(line2))
