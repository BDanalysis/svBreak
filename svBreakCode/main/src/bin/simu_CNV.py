import re

infile_1=open('Data//Stumorseq-1.fa','r')
infile_2=open('Data//Stumorseq-2.fa','r')
infile_3=open('Data//Groundtruth.txt','r')

outfile_1=open('Data//SCtumor_1.fa','w')
outfile_2=open('Data//SCtumor_2.fa','w')
outfile_3=open('Data//SCtumorseq_1.fa','w')r
outfile_4=open('Data//SCtumorseq_2.fa','w')

file_num=[]
start=[]
end=[]
mu_length=[]
type=[]
copy_num=[]


# 保存原始序列
firstline=infile_1.readline()  #保存第一行信息
seq_tumor_1=infile_1.readline().strip()  #下标从0开始
#strip用于除去字符串首尾的字符\n、\t之类的


next(infile_2)
seq_tumor_2=infile_2.readline().strip()  #下标从0开始

# print(firstline[0])


# 保存变异后的序列
seq1=seq_tumor_1
seq2=seq_tumor_2

# end－start＋1＝变异长度
for line in infile_3:
    linestr=line.strip()
    linestrlist=linestr.split('\t')
    file_num.append(int(linestrlist[0]))
    start.append(int(linestrlist[1]))
    end.append(int(linestrlist[2]))
    mu_length.append(int(linestrlist[3]))
    type.append(linestrlist[4])
    copy_num.append(int(linestrlist[5]))

for i in range(len(start)):
    if type[i]=='gain':
        if file_num[i]==1:
            CNV=seq_tumor_1[start[i]:end[i]+1]
            pos=re.search(CNV, seq1).span()[0]
            duplication_num=copy_num[i]-2
            seq1=seq1[:pos]+CNV*duplication_num+seq1[pos:]
        elif file_num[i] == 2:
            CNV = seq_tumor_2[start[i]:end[i]+1]
            pos = re.search(CNV, seq2).span()[0]
            duplication_num = copy_num[i] - 2
            seq2 = seq2[:pos] + CNV * duplication_num + seq2[pos:]
    elif type[i]=='loss':
        if copy_num[i]==1:
            if file_num[i]==1:
                CNV=seq_tumor_1[start[i]:end[i]+1]
                pos=re.search(CNV, seq1).span()[0]
                seq1=seq1[:pos]+seq1[pos+mu_length[i]:]
            elif file_num[i] == 2:
                CNV = seq_tumor_2[start[i]:end[i] + 1]
                pos = re.search(CNV, seq2).span()[0]
                seq2 = seq2[:pos] + seq2[pos+mu_length[i]:]

        # 因为含有SNV变异，所以同一区段，可能变异的序列中也有几个位点不一样
        # 这样做，保证了删除的序列在原始序列中是同一段序列，但是在变异序列中位置不一定相同，这样可以保证拷贝数是０
        elif copy_num[i]==0:
            CNV1 = seq_tumor_1[start[i]:end[i] + 1]
            CNV2 = seq_tumor_2[start[i]:end[i] + 1]
            pos1 = re.search(CNV1, seq1).span()[0]
            pos2 = re.search(CNV2, seq2).span()[0]
            seq1 = seq1[:pos1] + seq1[pos1 + mu_length[i]:]
            seq2 = seq2[:pos2] + seq2[pos2 + mu_length[i]:]


outfile_1.write(firstline)
outfile_2.write(firstline)
outfile_3.write(firstline)
outfile_3.write(seq1)
outfile_4.write(firstline)
outfile_4.write(seq2)
for i in range(0, len(seq1), 50):
    outfile_1.writelines(seq1[i:i + 50]+'\n')
for i in range(0, len(seq2), 50):
    outfile_2.writelines(seq2[i:i + 50]+'\n')

infile_1.close()
infile_2.close()
infile_3.close()
outfile_1.close()
outfile_2.close()
outfile_3.close()
outfile_4.close()
