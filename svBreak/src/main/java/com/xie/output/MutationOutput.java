package com.xie.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class MutationOutput {
	public void output(String ref,String insertion_result) throws Exception{
		BufferedReader br1 = new BufferedReader(new FileReader(ref));
		StringBuilder sb1 = new StringBuilder();
		String line1 = null;
		String chr = "";
		while((line1 = br1.readLine()) != null){
			if(line1.startsWith(">")){
				chr = line1.substring(1);
				continue;
			}else{
				sb1.append(line1);
			}
		}
		br1.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(insertion_result));
		int pos=0;
		for(int i=0;i<sb1.length()-1;i++,pos++) {
			if((sb1.charAt(i)>='a' && sb1.charAt(i)<='z' && sb1.charAt(i+1)>='a' && sb1.charAt(i+1)<='z')||sb1.charAt(i)=='N'||sb1.charAt(i+1)=='N') {
				continue;
				}
			else {
				int j=i+1;
				while(j<sb1.length() && sb1.charAt(j)>='A' && sb1.charAt(j)<='Z')
					j++;
				String line=sb1.substring(i,j);
				int length=j-i-1;
				bw.write(chr+"	"+pos+"	"+length+"	"+sb1.charAt(i)+"	"+line);
				bw.newLine();
				bw.flush();
				i=j-1;
			}
		}
		bw.close();
		
	}

}