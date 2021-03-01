#!/bin/bash
#由于空间不够，该文件夹下只保留了覆盖度为20的试验结果，其他试验结果可以在移动硬盘文件夹/all_mutation试验结果/VRindel各覆盖度下的试验结果/
cd cover20
for((i=0;i<50;i++)){
mkdir $i
cd $i
cp /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据/many_mutation_simulated_data/sim_all_mutation.fa sim_all_mutation.fa
cp /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据/many_mutation_simulated_data/all_mutation_original.fa all_mutation_original.fa
/home/xuxiangyan/文档/program/truedatatest/SInc/./SInC_readGen -C 20 -T 1 -R 100 sim_all_mutation.fa /home/xuxiangyan/文档/program/truedatatest/SInc/100_bp_read_1_profile.txt  /home/xuxiangyan/文档/program/truedatatest/SInc/100_bp_read_2_profile.txt
/home/xuxiangyan/文档/program/truedatatest/bwa-0.7.5a/./bwa index all_mutation_original.fa
/home/xuxiangyan/文档/program/truedatatest/bwa-0.7.5a/./bwa mem all_mutation_original.fa sim_all_mutation.fa_1_300_50_20.0_100.fq sim_all_mutation.fa_2_300_50_20.0_100.fq >all_mutation.sam
java -classpath /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据 filter_all_mutation.Filter all_mutation.sam mim.sam filtered.sam unmapped.sam discordant.txt 100 500 30 mim_readname.lst filtered_readname.lst unmapped_read.lst merged_read_name.lst
java -classpath /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据 all_mutation_extract_read_name.Extract merged_read_name.lst pair_read_name.lst
/home/xuxiangyan/文档/program/truedatatest/seqtk-master/./seqtk subseq sim_all_mutation.fa_1_300_50_20.0_100.fq pair_read_name.lst >out1.fq
/home/xuxiangyan/文档/program/truedatatest/seqtk-master/./seqtk subseq sim_all_mutation.fa_2_300_50_20.0_100.fq pair_read_name.lst >out2.fq
java -classpath /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据 all_mutation_ref_upper2lower.Upper2Lower all_mutation_original.fa lower.fa
cp lower.fa test1.fa
da="test"
db="1"
dc=${da}${db}
dd="out1_"
#ee="out2_"
ddc=${dd}${db}
#ddd=${ee}${db}
count=1
flag=100
while (test -e ${dc}".fa")
do
	/home/xuxiangyan/文档/program/truedatatest/bwa-0.7.5a/./bwa index ${dc}".fa"
	/home/xuxiangyan/文档/program/truedatatest/bwa-0.7.5a/./bwa mem ${dc}".fa" "out1.fq" "out2.fq">${dc}".sam"
	#java -classpath /home/xuxiangyan/文档/program/truedatatest/版本3 trueprocess_fastq.NewProcess_fastq ${dc}".sam" "choosed_unit.txt" "chr21"
	java -classpath /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据 allMutation_longInsertion_v2.Test $da $db "chr21"
	let db=$db+1
	echo $db
	dc=${da}${db}
	#ddc=${dd}${db}
	#ddd=${ee}${db}
	if [[ $count -gt $flag ]]
	then
		#echo "hello"			
		break
	fi
	let count=$count+1
	rm -f temp_fqname.lst
done
let db=$db-1
dc=${da}${db}
#java -classpath /home/xuxiangyan/文档/program/truedatatest output_all.NewOutputAll  "all_mutation_original.fa" ${dc}".fa" "insertion_result.txt" "deletion_result.txt"
java -classpath /home/xuxiangyan/文档/program/truedatatest/加入所有变异的仿真数据 all_mutation_output.MutationOutput ${dc}".fa" "insertion_result.txt"
cp test1_i_pos_seq.txt first_filter.txt
java -classpath /home/xuxiangyan/文档/program/truedatatest/版本3 filter.Fileter "all_mutation_original.fa" "first_filter.txt" "insertion_result.txt" "final_insertion_result.txt"
#rm -f final_del_result.txt
#rm -f final_del_result1.txt
rm -f deletion_result.txt
rm -f test*
rm -f out*
rm -f temp.sam
rm -f temp_insertion_result.txt
rm -f first_filter.txt
rm -f insertion_result.txt
rm -f discordant.txt
rm -f single.fq
rm -f filtered.sam
rm -f filtered_readname.lst
rm -f merged_read_name.lst
rm -f mim.sam
rm -f mim_readname.lst
rm -f unmapped.sam
rm -f unmapped_read.lst
rm -f all_mutation_original.fa.*
rm -f all_mutation.sam
rm -f lower.fa
cd ..
}
cd ..
