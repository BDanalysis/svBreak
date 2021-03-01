package fangzhen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

public class NewGenerateMutationTxt {
	//mutation_txt:产生出来的变异位置大小记录文件
	BitSet bitSet=new BitSet(1000000);
	public void setNPosBitSet(String nPosFile) throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(nPosFile));
		String line1=null;
		int length=0;
		while((line1 = br1.readLine()) != null) {
			String[] temp=line1.split(" ");
			length+=Integer.valueOf(temp[1])-Integer.valueOf(temp[0]);
			bitSet.set(Integer.valueOf(temp[0]), Integer.valueOf(temp[1]), true);
		}
		br1.close();
	}
	public void generateMutationTxt(String insertion_datasource,String mutation_txt) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(insertion_datasource));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine()) != null){
			if(line.startsWith(">")){
				continue;
			}else{
//				if(line.contains("N")){
//					continue;
//				}
				sb.append(line);
			}
		}
		br.close();
		System.out.println(sb.length());
		BufferedWriter bw = new BufferedWriter(new FileWriter(mutation_txt));
		Random random = new Random();
		Random random1 = new Random();
		Random random2 = new Random();
        int s = 0;
        int start=0;
		//int s = random.nextInt(8000)%(8000-7000+1) + 7000;//生成一个7000到8000的随机数，因为资料显示，每两个insertion或者deletion变异之间的距离是7.2K；
		for(int i=0;i<50;i++){
				int index = random.nextInt(100000)%(100000-50000+1)+50000;
				start=start+index;
				while(bitSet.get(start)==true) {
					index = random.nextInt(100000)%(100000-50000+1)+50000;
					start=start+index;
				}
					int size = random1.nextInt(500)%(500-50+1)+50;
					//int arr_index = random.nextInt(length);
					//String micro_sequence = arr.get(arr_index);
					//int start = random.nextInt(micro_sequence.length()-size);
					//String insertion_sequence = micro_sequence.substring(start, start+size);
					int jiequ=random2.nextInt(20000)%(20000-10000+1)+10000;
					String insertion_sequence = sb.substring(start+jiequ, start+jiequ+size);;
					bw.write("I"+" "+start+" "+size+" "+insertion_sequence+" "+(start+jiequ));
					bw.newLine();
					bw.flush();
		}
		bw.close();
		
		
	}
	public static void main(String[] args) throws Exception {
		NewGenerateMutationTxt gmt = new NewGenerateMutationTxt();
		gmt.setNPosBitSet("/media/xie/000E60FD000E74AA/newsim1014/CNVSim-master/out/chr21_pos_N.txt");
		gmt.generateMutationTxt("/media/xie/000E60FD000E74AA/newsim1014/CNVSim-master/out/chr21.fa","/media/xie/000E60FD000E74AA/newsim1014/CNVSim-master/out/mutation1018.txt");
	}
}
