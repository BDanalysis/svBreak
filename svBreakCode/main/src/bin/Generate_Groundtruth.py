import re
import numpy as py
import random

outfile=open('Data//Groundtruth.txt','w')
infile=open('Data//normalseq-1.fa','r')

length_list=[10000,20000,40000,50000,100000]

num_1=68
num_2=56
num_4=46
num_5=34
num_10=22
mutation_type=[0,1,3,4,5,6]
# 每种变异长度对应的变异数量，都要为偶数
len_1={0:10,1:18,3:18,4:10,5:6,6:6}
len_2={0:8,1:16,3:14,4:8,5:6,6:4}
len_4={0:6,1:14,3:12,4:6,5:4,6:4}
len_5={0:6,1:10,3:8,4:4,5:4,6:2}
len_10={0:4,1:8,3:4,4:2,5:2,6:2}
length={10000:len_1,20000:len_2,40000:len_4,50000:len_5,100000:len_10}

half_len_1={0:5,1:9,3:9,4:5,5:3,6:3}
half_len_2={0:4,1:8,3:7,4:4,5:3,6:2}
half_len_4={0:3,1:7,3:6,4:3,5:2,6:2}
half_len_5={0:3,1:5,3:4,4:2,5:2,6:1}
half_len_10={0:2,1:4,3:2,4:1,5:1,6:1}
half_length={10000:half_len_1,20000:half_len_2,40000:half_len_4,50000:half_len_5,100000:half_len_10}

gain=[6,5,4,3]
loss=[1,0]

jishu=0
total_base=-1 #记录列表中的下标
total_CNV=226
next(infile)
for line in infile:
    for ch in line:
        total_base += 1
        if ch=='n' or ch=='N':
            jishu=0
            continue
        jishu += 1
        if jishu == 150000 and total_CNV>0:
            jishu=0
            length_rand=random.choice(length_list)
            mutation_rand=random.choice(mutation_type)
            while length[length_rand][mutation_rand]==0:
                length_rand = random.choice(length_list)
                mutation_rand = random.choice(mutation_type)

            if mutation_rand in loss and length[length_rand][mutation_rand]>0:
                if length[length_rand][mutation_rand]>half_length[length_rand][mutation_rand]:
                    outfile.write(str(1) + '\t')
                    outfile.write(str(total_base - length_rand + 1) + '\t')
                    outfile.write(str(total_base) + '\t')
                    outfile.write(str(length_rand) + '\t')
                    outfile.write('loss' + '\t')
                    outfile.write(str(mutation_rand))
                    outfile.write('\n')
                    total_CNV -= 1
                    length[length_rand][mutation_rand] -= 1
                    continue
                else:
                    outfile.write(str(2) + '\t')
                    outfile.write(str(total_base - length_rand + 1) + '\t')
                    outfile.write(str(total_base) + '\t')
                    outfile.write(str(length_rand) + '\t')
                    outfile.write('loss' + '\t')
                    outfile.write(str(mutation_rand))
                    outfile.write('\n')
                    total_CNV -= 1
                    length[length_rand][mutation_rand] -= 1
                    continue
            if mutation_rand in gain and length[length_rand][mutation_rand]>0:
                if length[length_rand][mutation_rand]>half_length[length_rand][mutation_rand]:
                    outfile.write(str(1) + '\t')
                    outfile.write(str(total_base - length_rand + 1) + '\t')
                    outfile.write(str(total_base) + '\t')
                    outfile.write(str(length_rand) + '\t')
                    outfile.write('gain' + '\t')
                    outfile.write(str(mutation_rand))
                    outfile.write('\n')
                    total_CNV -= 1
                    length[length_rand][mutation_rand] -= 1
                    continue
                else:
                    outfile.write(str(2) + '\t')
                    outfile.write(str(total_base - length_rand + 1) + '\t')
                    outfile.write(str(total_base) + '\t')
                    outfile.write(str(length_rand) + '\t')
                    outfile.write('gain' + '\t')
                    outfile.write(str(mutation_rand))
                    outfile.write('\n')
                    total_CNV -= 1
                    length[length_rand][mutation_rand] -= 1
                    continue
# total_CNV=0说明仿真正确
print(total_CNV)
outfile.close()
infile.close()