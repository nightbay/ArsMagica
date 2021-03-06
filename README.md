# ArsMagica
A playful season by season AM simulation

A set of sample output from a run is available the following [shared directory](https://drive.google.com/drive/folders/1pz3hkxKxRIpuLI3ueD-4OqZbK93Fyc3q?usp=sharing). 
This includes a full database dump of a MySQL database, with one table for magi, one for covenants, one for books and one for actions (which lists every season for every magus from 700 to 1220).
Note that this output, apart from the actions table, is a snapshot every ten years. The shared directory also includes some csv files withe current state of the world in 1220 (with 738 living magi, 123 covenants and 60 thousand books in their libraries).

(Also note that this is non-canon in that there is no House Ex Miscellanea, and House Diedne keeps going with no Schism War.)

There are a couple of ways to try and get this going locally. Option one is to compile from this project, plus the simulation project (hopshackle/Simulation) that it is built on top of. Option two is to use the jar file that is included in  the repository (arsMagica_0_1.jar). The supplied jar file is compiled to be compatible with Java SE 1.7, so should work on Windows or Linux.

To run the simulation, you will need a MySQL database set up (see later), plus a few text files. The most important of these is a properties file that includes details of where to find all the other files. When the simulation is run, the location of the properties file needs to be provided as an argument.

A sample properties file is included in the repository, and a Linux version is below with the minimal set of entries needed:

	#Genetic Properties for hopshackle.simulation
	#Sat Nov 10 17:37:24 GMT 2007
	BaseDirectory=/home/James/AM
	LogDirectory=/home/James/amlogs
	ActionEnumClassFile=/home/James/AM/ActionEnums.txt
	GeneticVariableClassFile=/home/James/AM/GeneticVariables.txt
	LogUsingDate=true
	MagusUniformResearchPreferences=false
	AM.startYear=700
	AM.duration=521
	AM.name=AM1
	DatabaseSchema=arsmagica
	DatabaseUser=root
	DatabasePassword=XXXX
	DatabaseHost=130.211.66.233
	#To use the local machine as db host, use DatabaseHost=

BaseDirectory is the key location to look for all configuration and similar files. LogDirectory is where to write the (very copious) log files that detail the seasonal activity of each and every Magus for the simulation run.
The following files *must* be present in BaseDirectory (examples included in the GitHub repository): 

	CovenantNames.txt
	MagusNames.txt
	BasicSpells.txt
	StartingMagi.txt

All of these are simple ASCII text files that can be edited as you see fit. The list of spells is taken from http://www.atlas-games.com/pdf_storage/SpellIndexbyTechnique.pdf, only using those in the core rulebook. The StartingMagi.txt file initiates the founders of the Order in the starting year. Magus and CovenantNames are just lists that are picked from randomly whenever a new Magus or Covenant is created.

Additionally, the two files for ActionEnums.txt and GeneticVariables.txt must be present in the locations specified in the properties file. (They're not used for Ars Magica, and I may get around to removing the dependency at some point.)

MySQL dependency
----------------
This is currently required, so you'll need to have a running MySQL server and database schema set up. The schema, host, password and user need to be set in the properties file (the last four entries in the example above). If you're running a MySQL database locally, then use "DatabaseHost=".
When the simulation runs it will create four database tables in the specified schema:

	books_???
	covenants_???
	magi_???
	tribunals_???
	
In all cases ??? is the name of the simulation run taken from the AM.name property. The simulation provides a complete dump every 10 years of the state of the Order, so there is one entry in each table for each decade (700, 710, 720 etc.). The year from which the snapshot is taken is the currentYear field. If you want the details of what individual magi are doing on a season by season basis, then this is currently only stored in the magi-specific log files (they should be pretty human-readable) which are put into LogDirectory.

All the database connection stuff is in hopshackle.simulation.ConnectionFactory if you want to change this (the SQL updates etc *should* be simple SQL, so will *probably* work fine using any compatible database connection).

Running the simulation
----------------------
Recommended execution is:

  java -Xms1g -Xmx6g -jar arsMagica_0_1.jar --properties fullPathAndNameOfPropertiesFile

This will reserve sufficient memory for the default simulation run of 520 years from 700 to 1220. Based on my runs, this can occupy up to 1.3GB of RAM at peak usage. The code will work with either Windows or Linux (not tested elsewhere), and the only difference between the two should be changes to the properties file to take account of different file separators.

