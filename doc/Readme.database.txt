README.Database

By Shiping Chen  Shiping.Chen@csiro.au

This readme provides brief steps (tips) for you to set up database for stockonline benchmark
 
1.Software Required:
	· Oracle v8.1.5 or late 
	·Java JDK 1.3.x or late

2. Create users, tables and sequences:
	· You can create a database user or use existing one.
	· Create the tables using create_table.sql at etc/sql.
	· Create the sequences using create_sequence.sql.

3.Populate the tables and the sequences:
	· Edit setenv.bat and run it.
	· Run: ant
	· If OK, seed the stockonline database using the following command:
	  Java stockonline.database.oracle.Seed jdbc:oracle:thin:@hostname:1521:SID user passwd




Note: The database must be populated for each run for performance testing. 



