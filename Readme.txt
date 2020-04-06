StockOnline is Copyright (C) CSIRO 2001, 2002, 2003, and distributed according to the GNU Lesser General Public License.
See Copyright.txt for full copyright information.

=====================================
Readme for Stockonline benchmark

By Shiping.Chen@csiro.au
=====================================

This readme gives a general introduction to stockonline benchmark.

1. Directories and files:

	build : all class, deployment xml and jars files built with ant.
	doc   : documentation (steps & tips) to set up stockonline database and run stockonline with several J2EE application servers;
		Also some background documentation on StockOnline (see Readme.docs.doc).
	etc   : some properties settings for different J2EE products. 
	lib   : the jar files required to build and run stockonline, such as JDBC and J2EE
	src   : all java source code and deployment scripts

	build.xml  : used by ant
	Readme     : this file
	setenv.bat : batch file to set environment to build and run stockonline on your machines.
                     Note: You must edit and run it before doing anything
	stockonline.properties: a few settings for build.xml. Note: You must edit before running ant



2. Basic steps to build and run the stockonline

	- To set up database, please refer doc/readme.database.txt for details.
	- To build stockonline.jar or stockonline.ear
	- Deploy/install the jar/ear file
	- Start the application server
	- Run client to test

   please refer to doc/readme.[product].txt for details for each specific J2EE product.




Good Luck!



