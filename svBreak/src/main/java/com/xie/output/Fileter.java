package com.xie.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class Fileter {
	public void filter(String chr,String insertion_result,String final_result) throws Exception{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(chr));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.startsWith(">")){
				continue;
			}
			sb.append(line);
		}
		br.close();
		String line1 = null;
		BufferedReader br2 = new BufferedReader(new FileReader(insertion_result));
		String line2 = null;
		BufferedWriter bw = new BufferedWriter(new FileWriter("temp_insertion_result.txt"));
		while((line2 = br2.readLine()) != null){
			
			String[] tmp = line2.split("\t");
			int len1 = tmp[3].length();
			int len2 = tmp[4].length();
			if(len2 - len1 > 25){
				bw.write(line2);
				bw.newLine();
				bw.flush();
			}else{
					bw.write(line2);
					bw.newLine();
					bw.flush();
			}
		}
		bw.close();
		br2.close();
		BufferedReader br3 = new BufferedReader(new FileReader("temp_insertion_result.txt"));
		String line3 = null;
		ArrayList<Pos_Seq_Unit> arr1 = new ArrayList<>();
		while((line3 = br3.readLine()) != null){
			String[] tmp = line3.split("\t");
			Pos_Seq_Unit psu = new Pos_Seq_Unit();
			psu.setPos(Integer.parseInt(tmp[1]));
			psu.setSeq(tmp[4].substring(1));
			arr1.add(psu);
		}
		br3.close();
		ArrayList<Integer> arr_error = new ArrayList<>();
		for(int i = 1;i<arr1.size();i++){
			Pos_Seq_Unit psu = arr1.get(i);
			if(psu.getSeq().length() > 20){
				String s = psu.getSeq().substring(0,20);
				int before_pos = arr1.get(i-1).getPos();
				if(s.contains(sb.substring(before_pos+2, before_pos+12))){
					arr_error.add(psu.getPos());
				}
			}
		}
		BufferedWriter bw4 = new BufferedWriter(new FileWriter(final_result));
		br3 = new BufferedReader(new FileReader("temp_insertion_result.txt"));
		while((line3 = br3.readLine()) != null){
			String[] tmp = line3.split("	");
			int pos = Integer.parseInt(tmp[1]);
			if(!arr_error.contains(pos)){
				bw4.write(line3);
				bw4.newLine();
				bw4.flush();
			}
		}
		br3.close();
		bw4.close();
		/*for(int i = 0;i<arr_error.size();i++){
			System.out.println(arr_error.get(i));
		}*/
	}
	public boolean contain(ArrayList<Integer> arr,int pos){
		int low = 0;
		int high = arr.size()-1;
		while(low <= high){
			int mid = (low+high)/2;
			if(arr.get(mid)-5 < pos && pos < arr.get(mid) + 5){
				return true;
			}else if(arr.get(mid) > pos){
				high = mid -1;
			}else{
				low = mid +1;
			}
		}
		return false;
	}
	public static void main(String[] args) throws Exception {
		Fileter f = new Fileter();
		//f.filter("chr21_random.fa", "test1_i_pos_seq.txt", "cover_20_insertion.txt", "cover20_final_insertion.txt");
//		f.filter(args[0], args[1], args[2], args[3]);
		/*StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader("chr21_random.fa"));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.startsWith(">")){
				continue;
			}
			sb.append(line);
		}
		br.close();
		System.out.println(sb.substring(15218,15228));*/
	}
}