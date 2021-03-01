package simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public class NewGenerateData {
	public void generateData(String ref,String mutation_txt,String sim_ref) throws Exception{
		BufferedReader br1 = new BufferedReader(new FileReader(ref));
		String line1 = null;
		StringBuilder sb1 = new StringBuilder();
		while((line1 = br1.readLine()) != null){
			if(line1.startsWith(">")){
				continue;
			}else{
				sb1.append(line1);
			}
		}
		BufferedReader br2 = new BufferedReader(new FileReader(mutation_txt));
		String line2 = null;
		int count = 0;
		int nextstart = 0;
		int flag = 0;
		String s = new String();
		while((line2 = br2.readLine()) != null){
			String[] temp = line2.split(" ");
			if(count == 0){
				String s1 = sb1.substring(0, Integer.parseInt(temp[1]));
				if(temp[0].equals("D")){
					 nextstart = Integer.parseInt(temp[1]) + Integer.parseInt(temp[2]);
					 flag = 1;
					 s=s1;
					 //System.out.println(s);
					 
				}else{
					//就是insertion
					 String s2 = temp[3];
					 s = s1+s2;
					 //System.out.println(s);
					 nextstart = Integer.parseInt(temp[1]);
					 flag = 0;
					
				}
				count++;
			}else{
				//s是逐渐拼接成的fa文件，sb1是原始的fa文件
				if(temp[0].equals("D")){
					String s1 = sb1.substring(nextstart,Integer.parseInt(temp[1]));
					s = s+s1;
					nextstart = Integer.parseInt(temp[1]) + Integer.parseInt(temp[2]);
					flag = 1;
					//System.out.println("D");
					//System.out.println(temp[1]);
				}else if(temp[0].equals("I")){
					String s1 = sb1.substring(nextstart,Integer.parseInt(temp[1]));
					String s2 = temp[3];
					nextstart = Integer.parseInt(temp[1]);
					s = s+s1+s2;
					flag = 0;
					//System.out.println("I");
					//System.out.println(temp[1]);
				}else if(temp[0].equals("INV")){
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int length = Integer.parseInt(temp[2]);//inversion
					String s2 = sb1.substring(Integer.parseInt(temp[1]),Integer.parseInt(temp[1])+length);
					String s3 = new StringBuilder(s2).reverse().toString();
					nextstart = Integer.parseInt(temp[1])+length;
					s=s+s1+s3;
					//System.out.println("INV");
					//System.out.println(temp[1]);
				}
				else if(temp[0].equals("CNV")){
					//CNV仿真，结构性变异检测中没有使用
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int length = Integer.parseInt(temp[2]);//
					int time = Integer.parseInt(temp[3]);//
					String s2 = sb1.substring(Integer.parseInt(temp[1]),Integer.parseInt(temp[1])+length);
					String s3 = s2;
					for(int i = 0;i<time-1;i++){
						s3+=s2;
					}
					s = s+s1+s3;
					nextstart = Integer.parseInt(temp[1])+length;
					System.out.println("CNV");
					System.out.println(temp[1]);
					System.out.println(nextstart);
				}else if(temp[0].equals("TRS")){
					//translocation
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int pos1 = Integer.parseInt(temp[1]);
					int pos2 = Integer.parseInt(temp[2]);
					int length1 = Integer.parseInt(temp[3]);
					int length2 = Integer.parseInt(temp[4]);
					String trans1 = sb1.substring(pos1,pos1+length1);
					String trans2 = sb1.substring(pos2,pos2+length2);
					String trans3 = sb1.substring(pos1+length1,pos2);
					//trans1是要交换的第一段，trans2是第二段，trans3是从pos1到pos2
					//sb1 = new StringBuilder(sb1.substring(0, pos1)+trans2+sb1.substring(pos1+length1,pos2)+trans1+sb1.substring(pos2+length2));
					//学长的nextstart是从temp1+length1开始的，所以需要改变母串sb1
					s=s+s1+trans2+trans3+trans1;
					//s=s+s1+trans2+trans1;
					nextstart=Integer.parseInt(temp[2])+length2;
					//System.out.println("TRANS");
					//System.out.println(temp[1]);
				}else if(temp[0].equals("TAN")){
					//tandem duplication 目前都是只复制一次
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int pos = Integer.parseInt(temp[1]);
					int length = Integer.parseInt(temp[2]);
					String tandem = sb1.substring(pos,pos+length);
					//tandem就是要复制的那一段
					s=s+s1+tandem;
					//加一个tandem表示复制一次
					nextstart=Integer.parseInt(temp[1]);
				}else if(temp[0].equals("ITE")){
					//interspersed duplication 散列重复,目前只复制一次
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int pos1 = Integer.parseInt(temp[1]);
					int pos2 = Integer.parseInt(temp[2]);
					int length = Integer.parseInt(temp[3]);
					String inter1 = sb1.substring(pos1,pos1+length);
					String inter2 = sb1.substring(pos1,pos2);
					//inter1就是要复制的那一段，将其复制到以pos2为起点的地方
					s=s+s1+inter2+inter1;
					//加一个inter1表示复制一次
					nextstart=Integer.parseInt(temp[2]);
					//System.out.println("ITE");
					//System.out.println(temp[1]);
				}else if(temp[0].equals("IVE")){
					//inverted duplication 反向复制，目前只复制一次
					String s1 = sb1.substring(nextstart, Integer.parseInt(temp[1]));
					int pos1 = Integer.parseInt(temp[1]);
					int pos2 = Integer.parseInt(temp[2]);
					int length = Integer.parseInt(temp[3]);
					String inver1 = sb1.substring(pos1,pos1+length);
					String inver2 = sb1.substring(pos1,pos2);
					String inver3 = new StringBuilder(inver2).reverse().toString();
					//inver3就是要复制的那一段，且已经被反向
					s=s+s1+inver2+inver3;
					//加一个inver3表示复制一次
					nextstart=Integer.parseInt(temp[2]);
					//System.out.println("IVE");
					//System.out.println(temp[1]);
				}
				count++;
			}
			
		}
		String s3 = sb1.substring(nextstart);
		s = s+s3;
		br2.close();
		br1.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(sim_ref));
		bw.write(">sim_chr21");
		bw.newLine();
		bw.flush();
		for(int i = 0;i<s.length();i+=100){
			if(i+100>s.length()){
				bw.write(s.substring(i));
				bw.flush();
			}else{
				bw.write(s.substring(i, i+100));
				//bw.newLine();substring
				bw.flush();
			}
			
		}
		bw.close();
		System.out.println(count);
	}
	public static void main(String[] args) throws Exception {
		NewGenerateData ngd = new NewGenerateData();
		//ngd.generateData("chr21_random.fa", "new_hytero_mutation2.txt", "new_hytero2.fa");
		ngd.generateData("/home/shaoqiangwang/Lucifer/仿真/all_mutation_original.fa", "/home/shaoqiangwang/Lucifer/仿真/ground_truth.txt", "/home/shaoqiangwang/Lucifer/仿真/sim_all_mutation_10_24.fa");
	}
}
