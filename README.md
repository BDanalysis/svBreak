# svBreak

##Basic Environment

Linux operation system 

Python3.5.0

Java 11.0.1

##Depend Tools

[Bwa]

a. Download:

wget https://sourceforge.net/projects/bio-bwa/files/bwa-0.7.17.tar.bz2/download ./

b. Unzip the file:

$ tar -xvf bwa-0.7.17.tar.bz2

c. Add Bwa into profile

$ vim .bashrc

$ export Dir_Bwa/bwa-0.7.17:$PATH #Dir_Bwa is the abosulte directory of Bwa

$ source .bashrc

[SAMtools]

a. Download:

wget https://sourceforge.net/projects/samtools/files/samtools/1.7/samtools-1.7.tar.bz2/download ./

b. Unzip the file:

$ tar -xvf samtools-1.7.tar.bz2

c. Add SAMTools into profile:

$ vim .bashrc

$ export Dir_SAMtools/samtools-1.7:$PATH #Dir_SAMtools is the abosulte directory of SAMTools

$ source .bashrc

[SAMtools]

a. Download:

$ git clone https://github.com/lh3/seqtk

b.Install:

$ cd seqtk

$ make

##Extra Python Library

TenSorFlow 1.12.0

Numpy 1.15.4

Scipy 1.2.1

##Usage of svBreak

[Input]

  We need two pair of FASTQ files and a reference sequence FASTA file.
  
  For example,we provide a FASTA file--genome.fa and four FASTAQ files--example1_1.fq, example1_2.fq, example2_1.fq, and example2_2.fq.The first two FASTQ files are one group, the last two FASTQ files are another group.
  
[Run]
  a.You need to modify the svBreak.properties file 
  
    SvBreak.properties is used to save temp data. You should set its content to "tempdir=your/path/to/temp".
    
    For example,you could set its content to "tempdir=xx/xx/xx/temp",/home/shaoqiangwang/ProgramFiles/svBreak/run.

[Output]
ExampleOUT.txt has a total of four columns

(1) Reference sequence name

(2) The position where the mutation occurred

(3) Length of mutation

(4) Sequence information with mutation.

THANK YOU!
