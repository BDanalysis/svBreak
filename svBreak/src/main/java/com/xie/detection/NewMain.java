package com.xie.detection;

import com.xie.output.MutationOutput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class NewMain {
		public static void main(String[] args) throws Exception {

			int i=Integer.valueOf(args[2]);
			int seqlength=Integer.valueOf(args[3]);
			int insertsize=500;
			int insertsize_sd=50;
			String tempdir=args[0];
			String properties=args[1];
			Properties pps = new Properties();
			boolean flag=true;
			boolean deletefile=true;
			try {
					InputStream in = new BufferedInputStream (new FileInputStream(properties));  
					pps.load(in);
			    }catch (IOException e) {
			    	e.printStackTrace();
			        }
			NoMsPositionRealign.temppath=pps.getProperty("tempdir");
			NoMsPositionRealign.shdir=pps.getProperty("shdir");
			if(i==1) {
					findMatchH fmh=new findMatchH();
					fmh.findmatchH(tempdir+"/"+i+"times.sam", tempdir+"/"+i+"times.findh.sam");
					fmh=null;
					HToS h=new HToS();
					h.htos_samfile(tempdir+"/"+i+"times.findh.sam", tempdir+"/"+i+"times.findhtos.sam", seqlength);
					h.htos_samfile(tempdir+"/"+i+"times.sam", tempdir+"/"+i+"times.htos.sam", seqlength);
					h=null;
					FindPosition fp=new FindPosition();
					fp.findposition(tempdir+"/"+i+"times.findhtos.sam", tempdir+"/"+i+"_pos.txt");
					fp=null;
					FindUnmatchName fun=new FindUnmatchName();
					fun.findunmatchname(tempdir+"/"+i+"times.sam", tempdir+"/"+i+"_unmatchname.list", insertsize);
					fun=null;
					ExtractUnmatchReads eur=new ExtractUnmatchReads();
					eur.extractunmatchreads(tempdir+"/"+i+"_unmatchname.list", tempdir+"/r1.fq", tempdir+"/"+i+"_r1.fq", pps.getProperty("shdir"));
					eur.extractunmatchreads(tempdir+"/"+i+"_unmatchname.list", tempdir+"/r2.fq", tempdir+"/"+i+"_r2.fq", pps.getProperty("shdir"));
					eur=null;
					FindNewAddSequenceFirst fnds=new FindNewAddSequenceFirst();
					fnds.findnewaddsequence(tempdir+"/"+i+"_pos.txt", tempdir+"/"+i+"times.htos.sam", tempdir+"/"+i+"_pos_add.txt", insertsize, insertsize_sd, tempdir+"/low.fa", tempdir+"/"+i+"_r1.fq", tempdir+"/"+i+"_r2.fq");
					fnds=null;
					int next=i+1;
					ConfirmPosition cp=new ConfirmPosition();
					cp.confirmposition(tempdir+"/"+i+"_pos_add.txt", tempdir+"/"+next+"_pos.txt");
					cp=null;
					FinalAdd fa=new FinalAdd();
					flag=fa.finaladd(tempdir+"/low.fa", tempdir+"/"+i+"_pos_add.txt", tempdir+"/"+next+".fa");
					if(deletefile) {
					DeleteFile.delete(tempdir+"/"+i+"times.sam");
					DeleteFile.delete(tempdir+"/"+i+"times.findh.sam");
					DeleteFile.delete(tempdir+"/"+i+"times.findhtos.sam");
					DeleteFile.delete(tempdir+"/"+i+"times.htos.sam");
					DeleteFile.delete(tempdir+"/"+i+"_r1.fq");
					DeleteFile.delete(tempdir+"/"+i+"_r2.fq");
					}

				}
				else {
					HToS h=new HToS();
					h.htos_samfile(tempdir+"/"+i+"times.sam", tempdir+"/"+i+"times.htos.sam", seqlength);
					h=null;
					FindUnmatchName fun=new FindUnmatchName();
					fun.findunmatchname(tempdir+"/"+i+"times.sam", tempdir+"/"+i+"_unmatchname.list", insertsize);
					fun=null;
					ExtractUnmatchReads eur=new ExtractUnmatchReads();
					eur.extractunmatchreads(tempdir+"/"+i+"_unmatchname.list", tempdir+"/r1.fq", tempdir+"/"+i+"_r1.fq", pps.getProperty("shdir"));
					eur.extractunmatchreads(tempdir+"/"+i+"_unmatchname.list", tempdir+"/r2.fq", tempdir+"/"+i+"_r2.fq", pps.getProperty("shdir"));
					eur=null;
					FindNewAddSequence fnds=new FindNewAddSequence();
					fnds.findnewaddsequence(tempdir+"/"+i+"_pos.txt", tempdir+"/"+i+"times.htos.sam", tempdir+"/"+i+"_pos_add.txt", insertsize, insertsize_sd,tempdir+"/"+i+".fa", tempdir+"/"+i+"_r1.fq", tempdir+"/"+i+"_r2.fq");
					fnds=null;
					int next=i+1;
					ConfirmPosition cp=new ConfirmPosition();
					cp.confirmposition(tempdir+"/"+i+"_pos_add.txt", tempdir+"/"+next+"_pos.txt");
					cp=null;
					FinalAdd fa=new FinalAdd();
					flag=fa.finaladd(tempdir+"/"+i+".fa", tempdir+"/"+i+"_pos_add.txt", tempdir+"/"+next+".fa");
					if(deletefile) {
					DeleteFile.delete(tempdir+"/"+i+"times.sam");
					DeleteFile.delete(tempdir+"/"+i+"times.htos.sam");
					DeleteFile.delete(tempdir+"/"+i+"_r1.fq");
					DeleteFile.delete(tempdir+"/"+i+"_r2.fq");
					}
				}
			System.out.println("��"+i+"�ε������");
			i++;
			if(flag==false) {
			MutationOutput mo=new MutationOutput();
			mo.output(tempdir+"/"+i+".fa", tempdir+"/ins.txt");
			System.exit(1);
			}
		
		}

}
