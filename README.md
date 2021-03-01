# svBreak

##Basic Environment

Linux operation system with python3.x

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

##Extra Python Library

$ pip3 install pysam

$ pip3 install sklearn

$ pip3 install scipy

$ pip3 install tensorflow

$ pip3 install numpy

##Usage of svBreak

[Input]

  We need two pair of FASTQ files and a reference sequence FASTA file.
  
  For example,we could name the FASTA file ExampleFA.fa, name the FASTAQ files ExampleFQ1.fq and ExampleFQ2.fq.
  
[Run]

a.build ExampleFA.fa index:

$ bwa index ExampleFA.fa

b.use bwa software to process the input file into SAM file

$ bwa mem ExampleFA.fa ExampleFQ1.fq ExampleFQ2.fq > ExampleSAM.sam

c.use svBreak software

You can directly run it by run run.sh ExampleSAM.sam ExampleFA.fa ExampleFQ1.fq ExampleFQ2.fq in run directory.

Or you can change input file and run main.py, you will get ExampleOUT.txt file.

[Output]
ExampleOUT.txt has a total of four columns

(1) Reference sequence name

(2) The position where the mutation occurred

(3) Length of mutation

(4) Sequence information with mutation.

[Platform]

SvBreak runs on Linux and Unix system.
