package fangzhen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

public class NewGenerateMutationSamll {
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
				int index = random.nextInt(8000)%(8000-7000+1)+7000;
				start=start+index;
				int size = random1.nextInt(150)%(150-30+1)+30;
				int jiequ=random2.nextInt(3000)%(3000-2000+1)+2000;
				String insertion_sequence = sb.substring(start+jiequ, start+jiequ+size);;
				bw.write("I"+" "+start+" "+size+" "+insertion_sequence+" "+(start+jiequ));
				bw.newLine();
				bw.flush();
		}
		bw.close();
		
		
	}
	public static void main(String[] args) throws Exception {
		NewGenerateMutationSamll gmt = new NewGenerateMutationSamll();
		gmt.generateMutationTxt("/media/xie/0009A639000F3A82/ins/genome.fa","/media/xie/0009A639000F3A82/ins/soapindelfangzhen/mutation.txt");
	}
}
