
### Introduction

This project contains code and data initially associated with the paper [RUPEE: Scalable protein structure search using run position encoded residue descriptors](http://ieeexplore.ieee.org/document/8217627/) published in the IEEE International Conference on Bioinformatics and Biomedicine of 2017. 
RUPEE is under continual development.
At this stage, the initial RUPEE paper serves only to describe the common basis for the current RUPEE operating modes, fast and top-aligned, which are described in a paper currently being prepared for submission. 

RUPEE itself is available for use at <https://ayoubresearch.com>.

Below, I will describe how to find your way around the RUPEE repo, directory by directory, in the order required for setting up RUPEE in your own environment. 
It is assumed that you are familiar with RUPEE and have read the paper. 

With respect to software dependencies, Java 8 and an installation of postgreSQL 9.4 or above are required.
The instructions below assume you are operating within a bash shell. 
Typically, this will be under Linux. 
However, Windows 10 does contain a bash shell if you follow some steps to enable it or else you can install Cygwin on earlier versions of Windows. 

If you need additional info or have questions not addressed below, contact me at ronaldayoub@mail.umkc.edu.

### ./

Some files, especially data files, are too numerous or too large to include in the github repo. 
The .gitignore file list the files and directories that have been explicitly excluded from the repo. 

### db/

This directory contains SQL definitions files. 
All files except files prefixed with x\_, y\_, or z\_ contain SQL definitions. 

x\_ files are used for populating tables and should only be run when parsed data files are present.
The x\_ files contain hard-coded references to file locations that should be changed to match your Linux home directory.
Unfortunately, the postgres COPY command does not accept relative directories. 

Once you have created a database with the case-sensitive name 'rupee', run the y_create_all.sql script,
This script will only create SQL objects. 
It will not populate any tables.
Within the __psql__ command line tool provided by postgres, this can be done with the following command:

```
\i y_create_all.sql
```

The z\_ files can be safely ignored. 
These typically contain queries I have found useful during development. 

### rupee-mgr/

This directory contains the Java project for administering RUPEE. 
The project also serves as a library used by the RUPEE web site. 
The root directory contains a Maven pom file used for building the project into the rupee-mgr/target/ directory. 
Before building, you should update the hard-coded values in the Constants.java file in the lib namespace. 

Once successfully built, running the jar with the command line parameter ```-?```, gives the following output: 

```
~/git/rupee/rupee-mgr/target$ java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -?
Usage: RUPEE
     -i,--import <DB_TYPE>
     -h,--hash <DB_TYPE>
     -a,--align <DB_ID_1>,<DB_ID_2>,<ALIGN>
     -t,--tm align <DB_ID_1>,<DB_ID_2>
     -l,--lcs <DB_ID_1>,<DB_ID_2>
     -s,--search <SEARCH_BY><DB_TYPE>,<DB_ID>,<LIMIT>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2><DIFF3><MODE>,<SORT>
     -u,--upload <FILE_PATH>
     -d,--debug
     -?,--help
```

Where 

```
<SEARCH_BY> = DB_ID | UPLOAD_ID
<DB_TYPE>   = SCOP | CATH | ECOD | CHAIN
<ALIGN>     = CE | CECP | FATCAT_RIGID | FATCAT_FLEXIBLE
<MODE>      = FAST | TOP_ALIGNED
<SORT>      = RMSD | TM_SCORE | SIMILARITY
<REP#>      = TRUE | FALSE
<DIFF#>     = TRUE | FALSE
```

The following table briefly describes each command line option.

Option | Description
------ | -----------
-i  | parse pdb files in the data directories and populate \_grams tables
-h  | min-hash grams in the \_grams tables and populate the \_hashes tables
-a  | align structures using a specific alignment algorithm
-t  | use Java rewrite of TM-align to align structures (currently, not used by RUPEE)
-l  | align structures using the LCS algorithm
-s  | search for similar structures 
-u  | upload a pdb file and obtain an internal identifier
-d  | random code for miscellaneous task
-?  | prints the available options

In the case of searching for structures similar to an uploaded structure, the ```-s``` option expects an internal upload id for the DB_ID parameter.

At this stage, you only need to build the rupee-mgr project to proceed. 

### data/

This directory contains all data files and scripts used in parsing the files. 

The following directories, along with brief descriptions, are excluded from the repo. 

Excluded Directory | Description
------------------ | -----------
data/pdb/pdb/      | From /pub/pdb/data/structures/all/pdb at ftp.wwpdb.org
data/pdb/obsolete/ | From /pub/pdb/data/structures/obsolete/pdb at ftp.wwpdb.org
data/scop/pdb/     | parsed pdb files based on scop definitions
data/cath/pdb/     | parsed pdb files based on cath definitions
data/ecod/pdb/     | parsed pdb files based on ecod definitions
data/chain/pdb/    | parsed pdb files containing whole chains
data/upload/       | directory used for temporary storage of uploaded pdb files

First, the data/pdb/ directory has to be populated with files downloaded from the wwpdb FTP site. 
If using FileZilla, you should set the connection timeout to at least 1000 seconds in the File-Edit-Settings dialog. 
Click the local data/pdb directory to select the destination for the files. 
Click the remote /pub/pdb/data/structures/all/pdb directory containing the files you want to download. 
It will take a few minutes to obtain the directory listing. 
Then right-click the remote pdb/ directory and select download. 
This will also create the local pdb/ directory under the data/pdb/ directory. 

Once downloaded, the files can be parsed based on structure definitions to populate the data/scop/, data/cath/, data/ecod/ and data/chain/ directories. 
Each of these directories can be parsed and processed independently. 
Since this can take a significant amount of time, consider starting with one initially, for instance data/scop/. 

Each of the directories, data/scop/, data/cath/, data/ecod/ and data/chain/ follow a similar pattern with some redundant code to keep things simple. 
The do_all.sh bash scripts can be used to parse structure definitions and subsequently parse pdb files based on the structure definitions.
Look at the do_all.sh bash scripts for exact details. 
Sometimes a modified script is checked in with some lines commented out. 
Before an initial run make sure all lines are uncommented. 

The do_all.sh bash script will also execute the above rupee-mgr application in order to import and hash structures once parsing is complete. 

To execute the do_all.sh script, check the parameters required for each script and pass in the parameters based on the structure definitions files you want to process. In the .gitignore file you will find references to these files downloaded from the source sites, i.e. SCOP, CATH, and ECOD.  

As long as you have successfully parsed and processed one of these directories, you can now execute searches with the rupee-mgr application.  

