README.Jboss

By Shiping Chen  Shiping.Chen@csiro.au

This readme provides brief steps (tips) for you to run stockonline benchmark with Jboss.
 
1. Software Required:
   · This distribution, including stockonline all java source code 
     and corresponding deployment xml scripts, such as ejb-jar.xml, 
     jboss.xml and jaws.xml
   · Jboss 2.4.x or late, downloadable from www.jboss.org 
   · Java JDK1.3.x or late, downloadable from http://java.sun.com/j2se 
   · Ant 1.4.x or late, downloadable from http://ant.apache.org 
   · Oracle v8.1.5 or late with a thin JDBC driver

2. Preparations
   · Create the tables required by Stockonline and populate them. 
     Please refer to Readme.database for details.
   · Make a few settings for your environment by editing stockonline.properties 
     and setenv.bat, such as JDK, ant, JDBC and the corresponding j2ee jar files 
     (jboss-j2ee.jar and jboss.jar for this example) etc.
   · Configure the EJB datasource by editing jboss.jcml at %JBOSS_HOME%/conf/default. 
     An example configuration for oracle is listed as follows:

	<!-- StockDB for Stockonline -->
	<mbean code="org.jboss.jdbc.JdbcProvider"name="DefaultDomain:service=JdbcProvider">
	   <attribute name="Drivers">oracle.jdbc.driver.OracleDriver</attribute>
	</mbean>

	<mbean code="org.jboss.jdbc.XADataSourceLoader"name="DefaultDomain:service=XADataSource,name=StockDB">
	   <attribute name="PoolName">StockDB</attribute>
	   <attribute name="DataSourceClass">org.jboss.pool.jdbc.xa.wrapper.XADataSourceImpl</attribute>
	   <attribute name="URL">jdbc:oracle:thin:@localhost:1521:mydb</attribute>
	   <attribute name="JDBCUser">stockonline</attribute>
	   <attribute name="Password">stockonline</attribute>
	   <attribute name="MaxSize">20</attribute>
	   <attribute name="PSCacheSize">1000</attribute>
	   <attribute name="TransactionIsolation">TRANSACTION_READ_COMMITTED</attribute>
	</mbean>

3. Build jar files
   · Go to the directory where build.xml locates.
   · Run: ant
   · If OK, two jar files should be generated at build/lib: 
	- stockonline_ejb_jboss.jar (For server)
	- stockonline_client.jar (For client)

4. Deployment and start the server
   · Copy the stockonline_ejb_jboss.jar to %JBOSS_HOME%/deploy
   · Go to %JBOSS_HOME%/bin
   · Run: run.bat
   · Please make it sure that the JDBC driver jar file on the classpath for your JVM
   · If OK, you should see something like:
	…
	[StockDB] XA Connection pool StockDB bound to java:/StockDB
	…
	…
	[J2EE Deployer Default] Deploy J2EE application: file:/D:/app/JBoss/JBoss-2.4.3_Tomcat3.2.3/jboss/deploy/stockonline_ejb_jboss.jar
	[J2eeDeployer] Create application stockonline_ejb_jboss.jar
	[J2eeDeployer] install EJB module stockonline_ejb_jboss.jar
	[Container factory] Deploying:file:/D:/app/JBoss/JBoss-2.4.3_Tomcat-3.2.3/jboss/tmp/deploy/Default/stockonline_ejb_jboss.jar
	[Verifier] Verifying file:/D:/app/JBoss/JBoss-2.4.3_Tomcat3.2.3/jboss/tmp/deploy/Default/stockonline_ejb_jboss.jar/ejb1001.jar
	[Container factory] Deploying cmpAccount
	[Container factory] Deploying cmpStockTx
	[Container factory] Deploying cmpStockItem
	[Container factory] Deploying cmpStockHolding
	[Container factory] Deploying cmpBroker
	[Container factory] Deploying statelessBroker

5. Test by running client
   · Before run client, you should:
	- Edit the etc/jboss.properties file for your client, 
          esp. the JNDI server name and its port number, such as:
	  java.naming.provider.url=localhost:1099
 	  Note: the port number (1099) must be consistent with 
   	  the naming server port number in jboss.jcml.
 
	- Make it sure that stockonline_client.jar on classpath.
   · To run functional test for query:
	java stockonline.client.Client –test f –env etc\jboss.properties –p query
	You should see something like:

	To test: query
	To create a broker bean instance
    	stockID = 1
	current_val = 8.14
   	high_val = 59.17
    	low_val = 37.3

   · To run performance test for 10 concurrent clients:
	java stockonline.client.Client –test p –env etc\jboss.properties –p 10

   · To get complete usage of stockonline client:
	java stockonline.client.Client





Good Luck!