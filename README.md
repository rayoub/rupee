
## Introduction

This project contains code and data to accompany the PLoS ONE paper: <br/>[RUPEE: A fast and accurate purely geometric protein structure search](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0213712). 

RUPEE itself is available for use at <https://ayoubresearch.com>.

If you any questions after reading the below, contact me at ronaldayoub@gmail.com.

The instructions below assume you're operating within a bash shell. 
Typically, this will be under Linux.
However, if you're working on a Windows machine, you can use the bash shell that comes with an installation of ***Git for Windows***.
If using ***Git for Windows***, you will have to edit occurrences of the `find` command in a few scripts below in order to hide the Windows native `find` command.
I'll point that out when needed. 

## Database

### Database Installation

RUPEE requires a ***PostgreSQL*** database installation version 9.4 or above. 
To install on Ubuntu, run the following command:

```
> sudo apt-get install postgresql
```
If you're using another flavor of Linux, I'm sure there's another equally simple command to execute. 
For a Windows installation, it's even easier. 
Just download the installer from <https://www.enterprisedb.com/downloads/postgres-postgresql-downloads> and run it.
If prompted for a password for the 'postgres' user, use 'postgres'. 

### Database Creation

These steps may be incomplete or inaccurate since I've switched from running RUPEE on a Linux machine to a Windows machine and I didn't update the documentation as I went through that process. 
I also switched to using the 'postgres' user for everything and didn't document how to do that in Linux, so the below Linux instructions are my best guess.

To keep things simple, I use the 'postgres' user for everything and I set the password to 'postgres'. 
On Windows, you can do this during the installation process. 
For Linux, switch to the 'postgres' user, login to the ***psql*** utility and issue the `\password` command as shown below. 

```
> sudo -u postgres psql
> \password
```
To exit ***psql***, type the `\q` command. 
The ***psql*** utility is used below in both Linux and Windows so it's recommended you familiarize yourself with it.  

Next, create the 'rupee' database with the following commands below. 
```
sudo -u postgres
createdb -O postgres rupee
```
For everything below, it's assumed the database name will be 'rupee', the username 'postgres', and the password 'postgres'. 
If you wish to change this you have to edit the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file in the rupee-search project before building. 

Next, navigate to the ```rupee/db/``` directory and login to the rupee database by executing the following command:
```
psql -U postgres rupee
```
Finally, within the __psql__ prompt, execute the following command to create the database tables and functions.
```
\i y_create_all.sql
```
### Database Configuration

Next, locate the ```pg_hba.conf``` file associated with the ***PostgreSQL*** installation. 
On Windows, it's located in the ```C:\Program Files\PostgreSQL\[Version]\data``` directory.
You should add the uncommented line below.
The comments are there to provide context. 
They should already be part of the ```pg_hba.conf``` file.
This is a necessary step in order to access the database from the Java app using password authentication. 
Overtime, the ```METHOD``` has changed from MD5 to scram-sha-256. 
Go with whatever value is used for the other entries in the ```pg_hba.conf``` file. 

```
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# password and auth for personal databases
local   rupee           postgres                                scram-sha-256
```
On Windows, start and stop the ***PostgreSQL*** service in order for the new configuration to take effect.  
On Linux, reboot your computer or do it some other way that doesn't require a reboot. 

## Maven Build

First, in the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file, edit the ```DATA_PATH``` constant to point to the local directory you plan to use as the root of all the locally stored pdb files. 
The actual pdb files will be stored under subdirectories within this directory. 
Next, build the 3 Java projects in this order:

1. rupee-tm
2. rupee-core
3. rupee-search

To build, from each project's root directory execute the following command:

```
> mvn clean package install
```
To test the build, navigate to the ```rupee-search/target/``` directory and execute the following command:
```
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -?
Usage: RUPEE
     -i,--import <DB_TYPE>
     -h,--hash <DB_TYPE>
     -s,--search-dbid <DB_TYPE>,<DB_ID>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE>
     -u,--search-upload <DB_TYPE>,<FILE_PATH>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE>
     -?,--help
```
where 

```
<DB_TYPE>       = DIR | SCOP | CATH | CHAIN
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
-?  | print the available options

## Single Directory Database

First, in the [Constants.java](rupee-search/src/main/java/edu/umkc/rupee/search/lib/Constants.java) file, edit the ```DATA_PATH``` constant to point to the local directory you plan to use as the root of all the locally stored pdb files. 
The actual pdb files will be stored under subdirectories within this directory. 
Next, create a subdirectory ```dir``` under the ```DATA_PATH``` above. 

To process the pdb files under ```dir```, execute the following commands:
```
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i DIR
> java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h DIR
```
Ignore the warnings, or alternatively, suppress the warnings using the java option <nobr>```-Dlog4j.configurationFile=log4j2.xml```</nobr>. 
The log4j2.xml file should be in the root of the target directory. 

Once the data is done processing, you can now search. 
The following command shows an example search:
```
java -jar -Dlog4j.configurationFile=log4j2.xml rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s DIR,d9rubb2,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,TOP_ALIGNED,FULL_LENGTH
```

## Importing SCOP, CATH and CHAIN Databases

If you're only interested in importing DIR data from the ```dir``` directory, you can safely ignore the following. 
However, if you're interested in duplicating the functionality at <https://ayoubresearch.com>, you should read on. 

Some files, especially data files, are too numerous or too large to include in the GitHub repo. 
The .gitignore file list the files and directories that have been explicitly excluded from the repo. 

### rupee/db/ directory

This directory contains SQL definitions files. 
All files except files prefixed with x\_ or y\_ contain SQL definitions. 

x\_ files are used for populating tables and should only be run when parsed data files are present.
Please note, the x\_ files contain __hard-coded references__ to file locations that should be changed to match your directories.
Unfortunately, the postgres COPY command does not accept relative directories. 
The y\_ are used for creating the database objects, as noted above. 

### rupee/data/ directory

This directory should probably correspond to your ```DATA_PATH``` but it is not required. 
If not, you should mirror the contents of this directory in your ```DATA_PATH``` directory. 
This directory contains all data files and scripts used in parsing the files. 

The following directories, along with brief descriptions, are excluded from the repo so you must create them. 
Create them now or create them as you go. 

Excluded Directory | Description
------------------ | -----------
data/pdb/pdb/      | see ```rysnc``` below
data/pdb/obsolete/ | see ```rysnc``` below
data/pdb/bundles/  | see ```rysnc``` below
data/chain/pdb/    | parsed pdb files containing whole chains
data/scop/pdb/     | parsed pdb files based on scop definitions
data/cath/pdb/     | parsed pdb files based on cath definitions
data/upload/       | directory used for temporary storage of uploaded pdb files

___PLEASE NOTE: DO NOT EXTRACT THE DOWNLOADED ARCHIVE FILES UNLESS EXPLICITY STATED___

The PDB FTP site has been deprecated so you have to use the ```rsync``` command to get the data. 

__While in the data/pdb__ directory run the following commands. 

You must include the final forward slash for the rsync source directories otherwise it will copy the directory itself in addition to the files.

```
rsync -rLpt -v -z --delete --port=33444 rsync.rcsb.org::ftp_data/structures/all/pdb/ ./pdb
rsync -rLpt -v -z --delete --port=33444 rsync.rcsb.org::ftp_data/structures/obsolete/pdb/ ./obsolete
rsync -rLpt -v -z --delete --port=33444 rsync.rcsb.org::ftp/compatible/pdb_bundle/ ./bundles
```

The files in the bundles and the obsolete directories are organized into subfolders. 
Execute the following commands in order move all files in the subfolders into the parent folder. 
These commands take around 5 minutes and do not give any progress indicator. 
```
find ./bundles -mindepth 2 -type f -exec mv -t ./bundles -i '{}' +
find ./obsolete -mindepth 2 -type f -exec mv -t ./obsolete -i '{}' +
```

If you're running in a ***Git for Windows*** bash shell on a Windows computer, the Linux __find__ command is hidden by the native Windows __find__ command.
The above commands should look like this. 

```
/c/git-sdk-64/usr/bin/find ./bundles -mindepth 2 -type f -exec mv -t ./bundles -i '{}' +
/c/git-sdk-64/usr/bin/find ./obsolete -mindepth 2 -type f -exec mv -t ./obsolete -i '{}' +
```

The tar files in the ```/data/pdb/bundles/``` local directory have to be extracted.
To extract, while in the ```/data/pdb/bundles/``` directory, run the following commands. 
Remove the path if running in Linux.

```
/c/git-sdk-64/usr/bin/find . -name "*.gz" -exec tar xvfz {} \;
```

The ***gzip*** files in the /data/pdb/obsolete directory should not be extracted. 

Once downloaded and preprocessed as above, the files can be processed to populate the remaining ```data/pdb/``` directory in addition to the ```data/chain/```, ```data/scop/``` and ```data/cath/``` directories. 
The ```data/pdb/``` directory must be processed first followed by the ```data/chain/``` directory.
Then, the remaining directories can be processed independently. 
The following is what to do in each directory with it set as your working directory.

### data/pdb/ directory

If you downloaded the pdb data on the date of MM/DD/YYYY, then your version is "vMM_DD_YYYY" and needs to be passed as the argument to the ```do_all.sh``` bash script.
This script chops bundle files and puts them in the data/pdb/chopped/ directory. 
This script may take up to 6 hours more or less based on your computer.
Below is an example:

```
> ./do_all.sh v01_15_2025
```
Next, this same script creates files in the ```data/pdb/chain/``` directory that define the chains present in the pdb files, including bundle files and obsolete files.  
The passed in version is used to name these definition files. 

### data/chain/ directory

Using the same version as above, type the following command.
This command takes some time - like a full day.  

___PLEASE NOTE: make sure to update the file reference containing a version number to the version passed into the command in the ```rupee/db/x_chain.sql``` file.___

```
> ./do_all.sh v01_15_2025
```

### data/scop/ directory

Go to the https://scop.berkeley.edu/downloads/ver=2.08 web site and download the files below to the ```data/scop/``` directory.  
The download links themselves will not contain the version information. 
```
dir.cla.scope.2.08-stable.txt
dir.des.scope.2.08-stable.txt
```
While in the ```data/scop/``` directory, for version 2.08, run the following command passing in the version information. There is no 'v' prepended. 

```
> ./do_all.sh 2.08
```

### data/cath/ directory

While in the ```data/cath/``` directory, using ***FileZilla*** or some other FTP app, go to the ```ftp://orengoftp.biochem.ucl.ac.uk``` site. 
Navigate to the ```/cath/releases/all-releases/v4_4_0/cath-classification-data``` directory and download the files below. 

```
data/cath/cath-domain-boundaries-v4_4_0.txt
data/cath/cath-domain-list-v4_4_0.txt
data/cath/cath-names-v4_4_0.txt
```
Next, enter the following command in the bash shell.

```
> ./do_all.sh v4_4_0
```

## Some Tips

You may need to get more familiar with the do_all.sh scripts.
When a script errors out midway through, carefully comment out the completed lines, address the issue, and run again. 
In some cases, the do_all.sh bash script will execute the above rupee-search application for importing and hashing structures. 

## Conclusion

If you have successfully processed one of the data directories, you can now run searches with the rupee-search application.  

