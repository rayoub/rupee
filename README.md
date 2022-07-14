
#### The website is currently down and will be back up by next week. 

### Introduction

This project contains code and data to accompany the PLoS ONE paper: <br/>[RUPEE: A fast and accurate purely geometric protein structure search](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0213712). 

RUPEE itself is available for use at <https://ayoubresearch.com>.

Below, I describe how to find your way around the RUPEE repo.
If you're interested in reproducing the results contained in the paper, you should first read everything below and then contact me for further details. 
To avoid confusion for the average user, I have hid everything particular to the evaluation of RUPEE in the eval/ directory.
It is assumed that you are familiar with RUPEE and have read the paper. 

As for software dependencies, Java 8 and an installation of postgreSQL 9.4 or above are required.
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
First, in the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file, edit the ```DIR_PATH``` constant to point to the local directory containing the pdb files you wish to search. 
In the same [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file, edit the ```DATA_PATH``` constant to point to a local directory containing an upload/ subdirectory.
Then, build the 3 Java projects in this order:

1. rupee-tm
2. rupee-core
3. rupee-search

To build, from each project's root directory execute the following command:

```
> mvn clean package install

```

### rupee-search Application

Once built, navigate to the rupee-search/target/ directory and issue the following command:
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
All files except files prefixed with x\_ or y\_ contain SQL definitions. 

x\_ files are used for populating tables and should only be run when parsed data files are present.
Please note, the x\_ files contain __hard-coded references__ to file locations that should be changed to match your Linux home directory.
Unfortunately, the postgres COPY command does not accept relative directories. 

### data/

This directory contains all data files and scripts used in parsing the files. 

The following directories, along with brief descriptions, are excluded from the repo so you must create them. 
Create them now or create them as you go. 

Excluded Directory | Description
------------------ | -----------
data/pdb/pdb/      | From /pub/pdb/data/structures/all/pdb at ftp.wwpdb.org
data/pdb/obsolete/ | From /pub/pdb/data/structures/obsolete/pdb at ftp.wwpdb.org
data/pdb/bundles/  | From /pub/pdb/compatible/pdb_bundle at ftp.wwpdb.org
data/chain/pdb/    | parsed pdb files containing whole chains
data/scop/pdb/     | parsed pdb files based on scop definitions
data/cath/pdb/     | parsed pdb files based on cath definitions
data/ecod/pdb/     | parsed pdb files based on ecod definitions
data/upload/       | directory used for temporary storage of uploaded pdb files

__NOTE: DO NOT EXTRACT THE DOWNLOADED ARCHIVE FILES UNLESS EXPLICITY STATED__

First, the data/pdb/ local directory has to be populated with files downloaded from the wwpdb FTP site. 
If using FileZilla, you should set the connection timeout to at least 1000 seconds in the Edit-Settings dialog. 
Click the data/pdb/ local directory to select the destination for the files. 
Click the /pub/pdb/data/structures/all/pdb/ remote directory containing the files you want to download. 
It will take a few minutes to obtain the directory listing and may even appear non-responsive. 
Once you have obtained the directory listing, right-click the pdb/ remote directory and select download. 
This will also create the pdb/ local directory under the data/pdb/ directory if it hasn't already been created. 

If the data/pdb/obsolete/ local directory is not already created, then create it now. 
To populate the data/pdb/obsolete/ local directory, the actions are different from above because the remote files are organized into subdirectories. 
First, if using FileZilla, select the data/pdb/obsolete/ local directory and select the /pub/pdb/data/structures/obsolete/pdb/ remote directory.
Then, go to the Server-Search Remote Files dialog. 
For search conditions, add a filename ends with 'ent.gz' rule and click search.
It should take about 10 minutes for the search to complete. 
Once the search is complete, in the Search dialog, select all files to be downloaded using Ctrl-A. 
Right-click and choose download. 
Choose to flatten remote paths and click OK.

If the data/pdb/bundles/ local directory is not already created, then create it now. 
Like the obsolete files, the remote files are organized into subdirectories. 
First, if using FileZilla, select the data/pdb/bundles/ local directory and select the /pub/pdb/compatible/pdb_bundle/ remote directory.
Then, go to the Server-Search Remote Files dialog. 
Remove all search conditions that may already be present and click search. 
It should take about 10 minutes for the search to complete. 
Once the search is complete, in the Search dialog, select all files to be downloaded using Ctrl-A. 
Right-click and choose download. 
Choose to flatten remote paths and click OK.

The archive files in the /data/pdb/bundles/ local directory do have to be extracted.
To extract, while in the /data/pdb/bundles/ directory, execute the following commands:
```
> gunzip *.gz
> find . -name "*.tar" -exec tar xvf {} \;
```
I also think the following command will work:
```
> tar xvfz *.gz
```
Once downloaded, the files can be processed to populate the remaining data/pdb/ directory in addition to the data/chain/, data/scop/, data/cath/ and data/ecod/ directories. 
The data/pdb/ directory must be processed first followed by the data/chain/ directory.
Then the remaining directories can be processed independently. 
The following is what to do in each directory with it set as your working directory.

### data/pdb/

If you downloaded the pdb data on the date of MM/DD/YYYY, then your version is "vMM_DD_YYYY" and needs to be passed as the argument to the do_all.sh bash script.
Below is an example:

```
> ./do_all.sh v08_28_2020
```
This script chops bundle files and puts them in the data/pdb/chopped/ directory. 
Next, files are created in the data/pdb/chain/ directory that define the chains present in the pdb files, including bundle files and obsolete files.  
The passed in version is used to name these definition files. 

### data/chain/

Using the same version as above, type the following command:

```
> ./do_all.sh v08_28_2020
```

### data/scop/, data/cath/, and data/ecod/

Simply type in the following command in each directory in any order:

```
> ./do_all.sh
```

### Some tips

You may need to get more familiar with the do_all.sh scripts.
When a script errors out midway through, carefully comment out the completed lines, address the issue, and run again. 
Sometimes a modified do_all.sh script is checked in with some lines commented out. 
Before an initial run make sure all lines are uncommented. 
In some cases, the do_all.sh bash script will execute the above rupee-search application for importing and hashing structures. 

### Conclusion

If you have successfully processed one of the data directories, you can now execute searches with the rupee-search application.  

