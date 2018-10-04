
### Introduction

This project contains code and data initially based on the paper [RUPEE: Scalable protein structure search using run position encoded residue descriptors](http://ieeexplore.ieee.org/document/8217627/) published in the IEEE International Conference on Bioinformatics and Biomedicine of 2017. 
RUPEE is under continual development.
At this stage, the initial RUPEE paper serves only to describe the common basis for the current RUPEE operating modes, fast and regular, which are described in a paper currently being prepared for submission. 

RUPEE itself is available for use at <http://www.ayoubresearch.com>.

Below, I will attempt to describe how to find your way around the RUPEE repo, directory by directory, in the order required for setting up RUPEE in your own environment. 
Some hard-coded variables are present in RUPEE. 
These hard-coded variables will be mentioned in the order they arise and summarized in a section following the directory descriptions. 
As far as software dependencies go, Java 8 and an installation of postgreSQL 9.4 or above are required.
Additionally, the instructions below assume you are operating within a bash shell. 
More commonly, this will be under Linux. 
However, Windows 10 does contain a bash shell as well if you take actions to enable it or else you can use an install of Cygwin on earlier versions of Windows. 

If you need additional info or have questions not addressed below, contact me at ronaldayoub@mail.umkc.edu.

### ./

Some files, especially data files, are too numerous or too large to include in the github repo. 
The .gitignore file provides an indicators of the files and directories that have been explicitly excluded from the repo. 

### db/

The data directory contains SQL definitions files. 
All files except files prefixed with x\_, y\_, or z\_ contain SQL definitions. 

x\_ files are used for populating tables and should only be run when parsed data files are present.
The x\_ files contain hard-coded references to file locations that should be changed to match your Linux home directory.
Unfortunately, the postgres COPY command does not except relative directories. 

Once you have a database for RUPEE set up, run the y_create_all.sql script. 
Within the __psql__ command line tool provided by postgres, this can be done with the following command:

```
\i y_create_all.sql
```

The z\_ files can be safely ignored. 
These typically contain queries I have found useful during development. 

### rupee-mgr/

This directory contain the Java project for administering RUPEE. 
The project also serves as a library used by the RUPEE web site. 
The root directory contains a Maven pom file used for building the project into the rupee-mgr/target/ subdirectory. 

Running the jar with the command line parameter ```-?```, gives the following output: 

```
~/git/rupee/rupee-mgr/target$ java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -?
Usage: RUPEE
     -i,--import <DB_TYPE>
     -h,--hash <DB_TYPE>
     -a,--align <ID_TYPE><DB_ID_1>,<DB_ID_2><ALIGN>
     -t,--tm align <ID_TYPE>,<DB_ID_1>,<DB_ID_2>
     -l,--lcs <ID_TYPE>,<DB_ID_1>,<DB_ID_2>
     -s,--search <SEARCH_BY><ID_TYPE>,<DB_TYPE>,<DB_ID>,<LIMIT>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2><DIFF3><ALIGN>,<SORT>
     -u,--upload <FILE_PATH>
     -d,--debug
     -?,--help
```

### data/

This directory contains all data files and scripts used in parsing the files. 

The following directories along with brief descriptions are excluded from the repo. 

Excluded Directory | Description
------------------ | -----------
data/pdb/pdb/      | From /pub/pdb/data/structures/all at ftp.wwpdb.org
data/pdb/obsolete/ | From /pub/pdb/data/structures/obsolete at ftp.wwpdb.org
data/pdb/index/    | From /pub/pdb/data/derived\_data/index at ftp.wwpdb.org
data/scop/pdb/     | parsed pdb files based on scop definitions
data/cath/pdb/     | parsed pdb files based on cath definitions
data/ecod/pdb/     | parsed pdb files based on ecod definitions
data/chain/pdb/    | parsed pdb files containing whole chains
data/upload/       | directory used for temporary storage of uploaded pdb files

### results/ 

### figures/

### hard-coded variables
