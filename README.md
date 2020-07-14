
### Introduction

This project contains code and data to accompany the PLoS ONE paper: <br/>[RUPEE: A fast and accurate purely geometric protein structure search](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0213712). 

RUPEE itself is available for use at <https://ayoubresearch.com>.

Below, I describe how to find your way around the RUPEE repo from a user perspective. 
If you're interested in reproducing the results contained in the paper, you should first read everything below and then contact me for further details. 
To avoid confusion for the average user, I have hid everything particular to the evaluation of RUPEE in the eval/ directory.
It is assumed that you are familiar with RUPEE and have read the paper. 

With respect to software dependencies, Java 8 and an installation of postgreSQL 9.4 or above are required.
The instructions below assume you are operating within a bash shell. 
Typically, this will be under Linux. 
However, Windows 10 does contain a bash shell if you follow some steps to enable it or else you can install Cygwin on earlier versions of Windows. 

If you need additional info or have questions not addressed below, contact me at ronaldayoub@mail.umkc.edu.

### Database Installation

For Ubuntu, to install postgres it is sufficient to execute the following command:

```
> sudo apt-get install postgresql
```
If you are not using Ubuntu, I'm sure there is some other equally simple command to execute. 

### Database Creation

To manage postgres, you don't need the 'postgres' user password. 
It is preferable to set up your own user using ```sudo su``` to become the 'postgres' user, assuming you have root permissions. 
For everything below, it is assumed the database name will be 'rupee', the username 'ayoub', and the password 'ayoub'. 
If you wish to change this you have to edit the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file in the rupee-search project before building. 

First, create the user with the following commands:

```
> sudo su postgres
> createuser -s -P ayoub

```
Next, create the database:
```
> sudo su postgres
> createdb -O ayoub rupee

```
Now, locate the ```pg_hba.conf``` file. 
Its location will vary. 
You should add the uncommented line below.
The comments are there to provide context. 
They were already part of the ```pg_hba.conf``` file when I found it. 
This is a necessary step in order to access the database from the Java app using password authentication. 

```
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# password and auth for personal databases
local   rupee       ayoub                   md5
```
Reboot your computer to start the postgres service with the new configuration or do it some other way that doesn't require a reboot. 
At this point, you should now get familiar with the postgres __psql__ command line utility, which is the easiest way to manage a postgres database. 

Navigate to the db/ directory and login to the rupee database by executing the following command:
```
psql rupee
```
Finally, within the __psql__ prompt, execute the following command:
```
\i y_create_all.sql
```

### Maven Build

The simplest scenario is when you have a single directory of pdb files containing single chains. 
First, in the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file, edit the ```DIR_PATH``` constant to point to the local directory  containing the pdb files and edit the ```UPLOAD_PATH``` constant to point to the local directory in which to store uploaded pdb files. 
If you wish to work with the other databases found at <https://ayoubresearch.com>, such as SCOP, CATH, ECOD, and CHAIN, you have to edit the corresponding ```*_PATH``` variables in addition to the ```UPLOAD_PATH``` variable.
Then, build the 3 Java projects in this order:

1. rupee-tm
2. rupee-core
3. rupee-search

To build, from each project's root directory execute the following command:

```
> mvn clean package install

```

### rupee-search Application

Once built, navigate to the rupee-search target directory and issue the following command:
```
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -?
Usage: RUPEE
     -i,--import <DB_TYPE>
     -h,--hash <DB_TYPE>
     -s,--search-dbid <DB_TYPE>,<DB_ID>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE>
     -u,--search-upload <DB_TYPE>,<FILE_PATH>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE>
     -?,--help
```
Where 

```
<DB_TYPE>       = DIR | SCOP | CATH | ECOD | CHAIN
<SEARCH_MODE>   = FAST | TOP_ALIGNED | ALL_ALIGNED
<SEARCH_TYPE>   = FULL_LENGTH | CONTAINED_IN | CONTAINS | RMSD | Q_SCORE | SSAP_SCORE 
<REP#>          = TRUE | FALSE
<DIFF#>         = TRUE | FALSE
```
The following table briefly describes each command line option.

Option | Description
------ | -----------
-i  | parse pdb files in the data directories and populate \*\_grams tables
-h  | min-hash grams in the \*\_grams tables and populate the \*\_hashes tables
-s  | search for similar structures with a db id
-u  | search for similar structures using a file path
-?  | prints the available options

To process the pdb files at ```DIR_PATH```, execute the following commands:
```
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i DIR
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h DIR
```
Ignore the warnings, or alternatively, suppress the warnings using the java option <nobr>```-Dlog4j.configurationFile=log4j2.xml```</nobr>. 
The log4j2.xml file should be in the root of the target directory. 

Once the data is done processing, you can now search. The following command shows an example search:
```
java -jar -Dlog4j.configurationFile=log4j2.xml rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s DIR,d9rubb2,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,TOP_ALIGNED,FULL_LENGTH

```

### Importing SCOP, CATH, ECOD and CHAIN databases

If you're only interested in importing DIR data from the ```DIR_PATH```, you can safely ignore the following. 
However, if you are interested in duplicating the functionality at <https://ayoubresearch.com> or you're interested in duplicating the results in the PLoS ONE paper, you should read on. 

Some files, especially data files, are too numerous or too large to include in the GitHub repo. 
The .gitignore file list the files and directories that have been explicitly excluded from the repo. 

### db/

This directory contains SQL definitions files. 
All files except files prefixed with x\_ contain SQL definitions. 

x\_ files are used for populating tables and should only be run when parsed data files are present.
The x\_ files contain hard-coded references to file locations that should be changed to match your Linux home directory.
Unfortunately, the postgres COPY command does not accept relative directories. 

### data/

This directory contains all data files and scripts used in parsing the files. 

The following directories, along with brief descriptions, are excluded from the repo. 

Excluded Directory | Description
------------------ | -----------
data/pdb/pdb/      | From /pub/pdb/data/structures/all/pdb at ftp.wwpdb.org
data/pdb/obsolete/ | From /pub/pdb/data/structures/obsolete/pdb at ftp.wwpdb.org
data/chain/pdb/    | parsed pdb files containing whole chains
data/scop/pdb/     | parsed pdb files based on scop definitions
data/cath/pdb/     | parsed pdb files based on cath definitions
data/ecod/pdb/     | parsed pdb files based on ecod definitions
data/upload/       | directory used for temporary storage of uploaded pdb files

First, the data/pdb/ directory has to be populated with files downloaded from the wwpdb FTP site. 
If using FileZilla, you should set the connection timeout to at least 1000 seconds in the File-Edit-Settings dialog. 
Click the local data/pdb/ directory to select the destination for the files. 
Click the remote /pub/pdb/data/structures/all/pdb/ directory containing the files you want to download. 
It will take a few minutes to obtain the directory listing. 
Then right-click the remote pdb/ directory and select download. 
This will also create the local pdb/ directory under the data/pdb/ directory. 

To populate the data/pdb/obsolete/ directory, the actions are different from above because the remote files are organized into subdirectories. 
If using FileZilla, go to the Server-Search Remote Files dialog. 
For search conditions, add a filename ends with 'ent.gz' rule and click search and wait a few minutes for the search to complete. 
If the local data/pdb/obsolete/ directory is not already created, then create it now. 
In the Search dialog, select all files to be downloaded using Ctrl-A. 
Right-click and choose download. 
Choose to flatten remote paths and click OK.

__NOTE: DO NOT UNZIP DOWNLOADED FILES__

Once downloaded, the files can be parsed based on structure definitions to populate the data/chain/, data/scop/, data/cath/ and data/ecod/ directories. 
The data/chain/ directory must be processed first.
Then the remaining directories can be parsed and processed independently. 

Each of the directories, data/chain/, data/scop/, data/cath/ and data/ecod/ follow a similar pattern with some redundant code to keep things simple. 
The do_all.sh bash scripts can be used to parse structure definitions and subsequently parse pdb files based on the structure definitions.
Look at the do_all.sh bash scripts for exact details. 
Sometimes a modified script is checked in with some lines commented out. 
Before an initial run make sure all lines are uncommented. 

The do_all.sh bash script will also execute the above rupee-search application in order to import and hash structures once parsing is complete. 

To execute the do_all.sh script, check the parameters required for each script by examining the code and pass in the parameters based on the structure definition files you want to process. In the .gitignore file you will find references to these files downloaded from the source sites, i.e. SCOP, CATH, and ECOD.  

As long as you have successfully parsed and processed one of these directories, you can now execute searches with the rupee-search application.  

