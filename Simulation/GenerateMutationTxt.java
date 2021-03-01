package simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GenerateMutationTxt {
	public void generateMutationTxt(String insertion_datasource,String mutation_txt) throws Exception{
		ArrayList<String> arr = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(insertion_datasource));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine()) != null){
			if(line.startsWith(">")){
				if(sb.length()!=0){
					arr.add(sb.toString());
					sb.delete(0, sb.length());
				}			
			}else{
				if(line.contains("N")){
					continue;
				}
				sb.append(line);
			}
		}
		br.close();
		sb = new StringBuilder();
		for(int i = 0;i<arr.size();i++){
			sb.append(arr.get(i));
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(mutation_txt));
		Random random = new Random();
        int s = 0;
        int start_jiequ = 10;
		//int s = random.nextInt(8000)%(8000-7000+1) + 7000;
        //random.nextInt(a)  生成一个0到a（不包括a）的数
		for(int i = 1000;i<100000;i+=s){
			char[] type = {'I','D'};
			int type_id = Math.random()>0.5?1:0;
			//insertion和deletion比例 1 ： 1
			char[] deletion_size = {'V','S'};
			char[] insertion_size = {'V','V','V','V','V','S','S','S','S','S'};
			if(type[type_id] == 'I'){
				Random random_1 = new Random();
				int index = random_1.nextInt(10);
				if(insertion_size[index] == 'V'){
					int size = random_1.nextInt(200)%(200-50+1)+50;
					String insertion_sequence = sb.substring(start_jiequ, start_jiequ+size);
					start_jiequ = start_jiequ+size+50;
					bw.write("I"+" "+i+" "+size+" "+insertion_sequence);
					bw.newLine();
					bw.flush();
				}else if(insertion_size[index] == 'S'){
					int size = random_1.nextInt(500)%(500-200+1)+200;
					String insertion_sequence = sb.substring(start_jiequ, start_jiequ+size);
					start_jiequ = start_jiequ+size+50;
					bw.write("I"+" "+i+" "+size+" "+insertion_sequence);
					bw.newLine();
					bw.flush();
				}
			}else{
				Random random_2 = new Random();
				int index = random_2.nextInt(2);
				if(deletion_size[index] == 'V'){
					int size = random_2.nextInt(200)%(200-50+1)+50;
					bw.write("D"+" "+i+" "+size);
					bw.newLine();
					bw.flush();
				}else{
					int size = random_2.nextInt(500)%(500-200+1)+200;
					bw.write("D"+" "+i+" "+size);
					bw.newLine();
					bw.flush();
				}
			}
			//两个相邻的变异之间的间隔s
			s = random.nextInt(2000)%(2000-1900+1) + 1900;
		}
		bw.close();
		
		
	}
	public static void main(String[] args) throws Exception {
		GenerateMutationTxt gmt = new GenerateMutationTxt();
		gmt.generateMutationTxt("/home/shaoqiangwang/Lucifer/仿真/inq_life_micro_database.trU.fa","/home/shaoqiangwang/Lucifer/仿真/ground_truth.txt");
	}
}
