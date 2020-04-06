/*
 * StockOnline: EJB 1.1 Benchmark.
 *
 * Copyright © Commonwealth Scientific and Industrial Research Organisation (CSIRO - www.csiro.au), Australia 2001, 2002, 2003.
 *
 * Contact: Paul.Brebner@csiro.au
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by   
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Originally developed for the CSIRO Middleware Technology Evaluation (MTE) Project, by
 * the Software Architectures and Component Technologies Group, CSIRO Mathematical and Information Sciences
 * Canberra and Sydney, Australia
 *
 *      www.cmis.csiro.au/sact/
 *      www.cmis.csiro.au/adsat/mte.htm 
 *
 * Initial developer(s): Shiping Chen, Paul Brebner, Lei Hu, Shuping Ran, Ian Gorton, Anna Liu.
 * Contributor(s): ______________________.
 */

//	
//
//	History:
//		10/08/2001	Shiping	Initial coding based on the existing code
//
//		09/11/2001	Shiping	Change the update transaction amount (20000)
//						with (tranInfo.stockID*tranInfo.accountNo).
//
//		12/11/2001	Shiping	Rule off ResetDB.
//		
//		12/11/2001	Shiping	Let each thread sleep for a random time to avoid
//						the contention on the container to create instances of beans.
//						(There is such a problem in IBM WebSphere)
//
//		20/11/2001	Shiping	Add code for warm-up and cool-down for critical performance measuring.
//		20/03/2003	Shiping	Removed the code for performance monitor for open source.
//
//
package stockonline.client;

import javax.ejb.*;
import javax.naming.*;
import java.rmi.*;
import java.util.*;
import java.io.*;

import stockonline.util.*;
import stockonline.ejb.session.interf.*;

public class Client implements Runnable
{
	final static boolean verbose = false;

	// Result log instances
	//
	ResultLog resultLogByID          = new ResultLog();
	ResultLog resultLogNewAccount    = new ResultLog();
	ResultLog resultLogBuyStock      = new ResultLog();
	ResultLog resultLogUpdateAccount = new ResultLog();
	ResultLog resultLogSellStock     = new ResultLog();
	ResultLog resultLogGetHolding    = new ResultLog();
	ResultLog resultLogTPS    	   = new ResultLog();

	static Synch 	synch;
	static Properties runProperties = new Properties();

	BrokerHome home = null;
	int threadNum = 0;

	int testIteration = 0;
	int warmupIteration = 0;
	int cooldownIteration = 0;
	int numRetry = 0;

	public Client(Synch synch, BrokerHome home, int threadNum)
	{
		this.synch = synch;
		this.home  = home;
		this.threadNum = threadNum;

		this.testIteration = Integer.parseInt(runProperties.getProperty("client.test.iteration", "10"));
		if(verbose) System.out.println(Thread.currentThread().getName() + ": testIteration = " + this.testIteration);

		this.warmupIteration = Integer.parseInt(runProperties.getProperty("client.warmup.iteration", "2"));
		if(verbose) System.out.println(Thread.currentThread().getName() + ": warmupIteration = " + this.warmupIteration);

		this.cooldownIteration = Integer.parseInt(runProperties.getProperty("client.cooldown.iteration", "2"));
		if(verbose) System.out.println(Thread.currentThread().getName() + ": cooldownIteration = " + this.cooldownIteration);

		this.numRetry = Integer.parseInt(runProperties.getProperty("client.create.retry", "9"));
		if(verbose) System.out.println(Thread.currentThread().getName() + ": numRetry = " + this.numRetry);
	}

	public static void main(String[] args)
	{
		// To process args
		//
		args_proc (args);

		// To get the bean home
		//
		BrokerHome myHome = getBeanHome();

		// To check if this is functional test
		//
		String testType = runProperties.getProperty("client.test.type");
		if(testType.equalsIgnoreCase("f"))	
			functionTest_proc(myHome);

		// -----------------------------------
		// The follows is for performance test
		// -----------------------------------

		// To get the number of threads (concurrent clients)
		//
		int threadNum = Integer.parseInt(runProperties.getProperty("client.test.P1"));
		if(verbose) System.out.println("threadNum = " + threadNum);

		// Create a synchronisation object, which ensures
		// all threads start testing at the same time.
		//
		Synch mySynch = new Synch(threadNum);

		// Note: We only create one object of the Runnable (Client) class;
		//       and we pass this object to all the Thread instances.
		Client t = new Client(mySynch, myHome, threadNum);

		// To create an array to keep the thread objects,
		//
		Thread threadArray[] = new Thread[threadNum];

		System.out.println("\nTo start testing......");
		//
		for (int i = 0; i < threadNum; i++)
		{
			threadArray[i] = new Thread(t);
			threadArray[i].start();
		}

		// To wait until all threads are ready
		while(!synch.setGo()) Synch.sleep(100);

		// To wait for all threads to finish
		//
		for (int i = 0; i < threadNum; i++) 
		{
			try {
				threadArray[i].join();
				if(verbose) System.out.println("Thread-" + i + " : exited");
			}
			catch (InterruptedException e) {}
		}

		System.out.println("\n--- All threads are finished ---\n");

		// To collect the test results
		//
		collectTestResult (t);
	}

	private static void collectTestResult ( Client t )
	{
		try
		{
			// To print out the result
			//
			System.out.println();
			System.out.println("----- Test Result --------");
			System.out.println("Buy     (" + t.resultLogBuyStock.getCount()      + ")  " + t.resultLogBuyStock.getAverage());
			System.out.println("Create  (" + t.resultLogNewAccount.getCount()    + ")  " + t.resultLogNewAccount.getAverage());
			System.out.println("Holding (" + t.resultLogGetHolding.getCount()    + ")  " + t.resultLogGetHolding.getAverage());
			System.out.println("Query   (" + t.resultLogByID.getCount()          + ")  " + t.resultLogByID.getAverage());
			System.out.println("Sell    (" + t.resultLogSellStock.getCount()     + ")  " + t.resultLogSellStock.getAverage());
			System.out.println("Update  (" + t.resultLogUpdateAccount.getCount() + ")  " + t.resultLogUpdateAccount.getAverage());
			System.out.println();
			System.out.println("TPS     (" + t.resultLogTPS.getCount()           + ")  " + t.resultLogTPS.getTotal());
			System.out.println("--------------------------");

			// To check if the test results need logging
			//
			String fileName = runProperties.getProperty("client.logfile");
			if(fileName==null) return;

			// To print out on screen without names for copy/past
			//
			System.out.println( t.resultLogBuyStock.getAverage());
			System.out.println( t.resultLogNewAccount.getAverage());
			System.out.println( t.resultLogGetHolding.getAverage());
			System.out.println( t.resultLogByID.getAverage());
			System.out.println( t.resultLogSellStock.getAverage());
			System.out.println( t.resultLogUpdateAccount.getAverage());
			System.out.println();
			System.out.println( t.resultLogTPS.getTotal());

			if(verbose) System.out.println("The test result is to be written to: " + fileName);
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter pw = new PrintWriter(fos);

			pw.println( t.resultLogBuyStock.getAverage());
			pw.println( t.resultLogNewAccount.getAverage());
			pw.println( t.resultLogGetHolding.getAverage());
			pw.println( t.resultLogByID.getAverage());
			pw.println( t.resultLogSellStock.getAverage());
			pw.println( t.resultLogUpdateAccount.getAverage());
			pw.println( t.resultLogTPS.getTotal());

			pw.close ();
			fos.close();
		}
		catch(Exception e)
		{
			System.err.println("File I/O exception: " + e.toString());
		}
	}

	private static BrokerHome getBeanHome()
	{
		try 
		{
			// To get the client type
			//
			String clientType = runProperties.getProperty("client.type");
			if(verbose) System.out.println("clientType = " + clientType);

			// To create the initial context according to the client type
			//
			Context ctx = null;
			ctx = new InitialContext(runProperties);	
			// ctx = new InitialContext();

			// To get the EJB's JNDI name
			//
			String ejbJNDIName = runProperties.getProperty("ejb.JNDIName");
			if(verbose) System.out.println("ejbJNDIName = " + ejbJNDIName);

			// To look up the bean via the JNDI
			// 

			Object obj = ctx.lookup(ejbJNDIName);
			BrokerHome myHome = (BrokerHome)javax.rmi.PortableRemoteObject.narrow(obj, BrokerHome.class);

			if(verbose) System.out.println("getBeanHome()............OK");
			ctx.close();

			return myHome;
		}
		catch (javax.naming.NamingException e) 
		{
			System.err.println("Unable to find the object, due to: " + e.toString());
			return null;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return null;
		}
	}

	public void run()
	{
		Broker broker = null;

		int accountID;
		QueryResult qr = null;
		Collection list = null;
		TranDeck tranDeck = null;
		TranInfo tranInfo = null;
		long startTime, endTime;

		stockonline.util.Timer timer = new stockonline.util.Timer();
		int _testIteration = 0;
		int _warmupIteration = warmupIteration;
		int _cooldownIteration = cooldownIteration;
		int _numRetry = numRetry;

		// Total itetrations, including warm-up and cool-down.
		// But only the test results between warm-up and cool-down will be recorded.
		///
		_testIteration = testIteration + _warmupIteration + _cooldownIteration;

		// Local Result log instances within a thread. 
		// After testing, add them to the "globle" log.
		// In this way, the threads will not interfere 
		// with each other too much.
		//
		ResultLog _resultLogByID          = new ResultLog();
		ResultLog _resultLogNewAccount    = new ResultLog();
		ResultLog _resultLogBuyStock      = new ResultLog();
		ResultLog _resultLogUpdateAccount = new ResultLog();
		ResultLog _resultLogSellStock     = new ResultLog();
		ResultLog _resultLogGetHolding    = new ResultLog();
		ResultLog _resultLogTPS           = new ResultLog();

		try
		{
			tranDeck = new TranDeck();

			// To sleep for random time to avoid too much concurrent access to JNDI
			//
			tranDeck.shuffle();
			tranInfo = tranDeck.getNext();
			sleep(tranInfo.accountNo);

			// To create the Broker EJB Object via the bean home interf
			for(int i=0; i<_numRetry; i++)
			{
				broker = home.create();
				if(broker!=null) break;
				else System.out.println(Thread.currentThread().getName() + " : To retry " + i);
			}

			// To be ready to go for test
			//
			synch.increase();
			while(!synch.getGo()) Thread.sleep(99);

			// If the bean is NULL, return to let other threads go on.
			// But make it sure there are NOT many threads failed here!
			//
			if(broker==null)
			{
				System.err.println(Thread.currentThread().getName() + " : failed to create the bean");
				return;
			}
		}
		catch (Exception e) 
		{
			System.err.println("Got exception: " + e.toString());
			e.printStackTrace();
		}

		// For warm-up
		//
		for (int i = 0; i < _warmupIteration; i++)
		{
			// Shuffle the deck first
			tranDeck.shuffle();
			if(verbose) System.out.println(Thread.currentThread().getName() + " - Loop " + i);

			for (int j = 0; j < TranDeck.TRAN_SIZE; j++) 
			{
				// Get next transaction descriptor
				tranInfo = tranDeck.getNext();

				try 
				{
					switch (tranInfo.tranType) 
					{
					case TranDeck.TRAN_BUY:
						startTime = ResultLog.getCurrentTime();
						broker.buyStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						// _resultLogBuyStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_SELL:
						startTime = ResultLog.getCurrentTime();
						broker.sellStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						// _resultLogSellStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_ID:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						// _resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_CODE:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						// _resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_GETHOLDING:
						startTime = ResultLog.getCurrentTime();
						list = broker.getHoldingStatement(tranInfo.accountNo, 0);
						endTime = ResultLog.getCurrentTime();
						// _resultLogGetHolding.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_CREATE:
						startTime = ResultLog.getCurrentTime();
						accountID = broker.newAccount("myName", "myAddress", 123456);
						endTime = ResultLog.getCurrentTime();
						// _resultLogNewAccount.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_UPDATE:
						startTime = ResultLog.getCurrentTime();
						broker.updateAccount(tranInfo.accountNo, tranInfo.stockID*tranInfo.accountNo);
						endTime = ResultLog.getCurrentTime();
						// _resultLogUpdateAccount.addSample(endTime - startTime);
						break;
					}

					// To update the performance monitor
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_RAW,    1);
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_PERSEC, 1);

					// To give other threads a chance to run
					Thread.yield();
				}
				catch (Exception e) 
				{
					// We should record number of exceptions here.
					//
					e.printStackTrace();
				}	// end of try 
			}		// end of loop j
		}			// end of loop i

		// For testing
		//
		timer.start();
			
		for (int i = 0; i < _testIteration; i++) 
		{
			// Shuffle the deck first
			tranDeck.shuffle();
			if(verbose) System.out.println(Thread.currentThread().getName() + " - Loop " + i);

			for (int j = 0; j < TranDeck.TRAN_SIZE; j++) 
			{
				// Get next transaction descriptor
				tranInfo = tranDeck.getNext();

				try 
				{
					switch (tranInfo.tranType) 
					{
					case TranDeck.TRAN_BUY:
						startTime = ResultLog.getCurrentTime();
						broker.buyStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						_resultLogBuyStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_SELL:
						startTime = ResultLog.getCurrentTime();
						broker.sellStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						_resultLogSellStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_ID:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						_resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_CODE:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						_resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_GETHOLDING:
						startTime = ResultLog.getCurrentTime();
						list = broker.getHoldingStatement(tranInfo.accountNo, 0);
						endTime = ResultLog.getCurrentTime();
						_resultLogGetHolding.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_CREATE:
						startTime = ResultLog.getCurrentTime();
						accountID = broker.newAccount("myName", "myAddress", 123456);
						endTime = ResultLog.getCurrentTime();
						_resultLogNewAccount.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_UPDATE:
						startTime = ResultLog.getCurrentTime();
						broker.updateAccount(tranInfo.accountNo, tranInfo.stockID*tranInfo.accountNo);
						endTime = ResultLog.getCurrentTime();
						_resultLogUpdateAccount.addSample(endTime - startTime);
						break;
					}

					// To update the performance monitor
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_RAW,    1);
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_PERSEC, 1);

					// To give other threads a chance to run
					// Thread.yield();
				}
				catch (Exception e) 
				{
					// We should record number of exceptions here.
					//
					e.printStackTrace();
				}	// end of try 
			}		// end of loop j
		}			// end of loop i

		timer.stop();

		// For cool-dowm
		for (int i = 0; i < _warmupIteration; i++)
		{
			// Shuffle the deck first
			tranDeck.shuffle();
			if(verbose) System.out.println(Thread.currentThread().getName() + " - Loop " + i);

			for (int j = 0; j < TranDeck.TRAN_SIZE; j++) 
			{
				// Get next transaction descriptor
				tranInfo = tranDeck.getNext();

				try 
				{
					switch (tranInfo.tranType) 
					{
					case TranDeck.TRAN_BUY:
						startTime = ResultLog.getCurrentTime();
						broker.buyStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						// _resultLogBuyStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_SELL:
						startTime = ResultLog.getCurrentTime();
						broker.sellStock(tranInfo.accountNo, tranInfo.stockID, 5);
						endTime = ResultLog.getCurrentTime();
						// _resultLogSellStock.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_ID:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						// _resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_QUERY_CODE:
						startTime = ResultLog.getCurrentTime();
						qr = broker.queryStockValueByID(tranInfo.stockID);
						endTime = ResultLog.getCurrentTime();
						// _resultLogByID.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_GETHOLDING:
						startTime = ResultLog.getCurrentTime();
						list = broker.getHoldingStatement(tranInfo.accountNo, 0);
						endTime = ResultLog.getCurrentTime();
						// _resultLogGetHolding.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_CREATE:
						startTime = ResultLog.getCurrentTime();
						accountID = broker.newAccount("myName", "myAddress", 123456);
						endTime = ResultLog.getCurrentTime();
						// _resultLogNewAccount.addSample(endTime - startTime);
						break;

					case TranDeck.TRAN_UPDATE:
						startTime = ResultLog.getCurrentTime();
						broker.updateAccount(tranInfo.accountNo, tranInfo.stockID*tranInfo.accountNo);
						endTime = ResultLog.getCurrentTime();
						// _resultLogUpdateAccount.addSample(endTime - startTime);
						break;
					}

					// To update the performance monitor
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_RAW,    1);
					// monTPS.incCount(Monitor.COUNT_TYPE.CNTTYPE_PERSEC, 1);

					// To give other threads a chance to run
					Thread.yield();
				}
				catch (Exception e) 
				{
					// We should record number of exceptions here.
					//
					e.printStackTrace();
				}	// end of try 
			}		// end of loop j
		}			// end of loop i

		// To remove the bean after testing
		//
		try
		{			
			broker.remove();
		}
		catch (Exception e) 
		{
			System.err.println("fail to remove the bean, due to: " + e.toString());
			e.printStackTrace();

		}

		// To calculate TPS for this thread
		//
		long numTrans = testIteration*43;
		long TPS = 1000*numTrans/timer.getTime();
		_resultLogTPS.addSample(TPS);

		// Add the result to global
		//
		resultLogByID.add(_resultLogByID);
		resultLogNewAccount.add(_resultLogNewAccount);
		resultLogBuyStock.add(_resultLogBuyStock);
		resultLogUpdateAccount.add(_resultLogUpdateAccount);
		resultLogSellStock.add(_resultLogSellStock);
		resultLogGetHolding.add(_resultLogGetHolding);
		resultLogTPS.add(_resultLogTPS);

		return;
	}
	
	// To process args
	//
	private static void args_proc(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("Usage: java stockonline.client.Client -test type -env run.properties [-log testResult.log] -param P1 [P2] [3] [P4]");
			System.err.println("where: type           = f | p");
			System.err.println("       run.properties = name of the file where testing environment properties are set");
			System.err.println("       testResult.log = name of the file where test results will be put into, only for performance test");
			System.err.println("       P1 depending on test type:");
			System.err.println("          if type = p, P1 = numner of threads, no [P2] [3] and [P4] are required.");
			System.err.println("          if type = f, P1 = create | query | buy | sell | holding | update | resetdb | testrollback");
			System.err.println("                       [P2] [P3] [P4] are optional, depending on P1:");
			System.err.println("                                      if P1 = create,  P2 = name, P3 = address and P4 = credit");
			System.err.println("                                      if P1 = update,  P2 = accountID and P3 = credit");
			System.err.println("                                      if P1 = holding, P2 = accountID");
			System.err.println("                                      if P1 = query,   P2 = stockID (1-3000)");
			System.err.println("                                      if P1 = buy | sell, P2 = accountID, P3 = stockID and P4 = amount ");
			System.err.println("                                      if P1 = resetDB | testrollback, no [P2] [3] and [P4] are required. ");
			System.err.println("                                      Note: For all funcational test, [P2] [3] and [P4] are optional. ");
			System.err.println("                                      This means valid default parameters can be applied if they are not provided.");

			System.exit(1);
		}

		try
		{
			String key = null;
			int k = 2;

			for(int i=0; i<args.length; i += k)
			{
				if(args[i].equalsIgnoreCase("-test") || args[i].equalsIgnoreCase("-t"))
				{
					key = "client.test.type";
					if(verbose) System.out.println(key + " = " + args[i+1]);
					runProperties.setProperty(key, args[i+1]);
				}
				else if(args[i].equalsIgnoreCase("-env") || args[i].equalsIgnoreCase("-e"))
				{
					if(verbose) System.out.println("test properties fileName = " + args[i+1]);
					java.io.FileInputStream in = new java.io.FileInputStream(args[i+1]);
					runProperties.load(in);
					if(verbose) runProperties.list( System.out );
				}
				else if(args[i].equalsIgnoreCase("-log") || args[i].equalsIgnoreCase("-l"))
				{
					key = "client.logfile";
					if(verbose) System.out.println(key + " = " + args[i+1]);
					runProperties.setProperty(key, args[i+1]);
				}
				else if(args[i].equalsIgnoreCase("-param") || args[i].equalsIgnoreCase("-p"))
				{
					for(k=1; k<args.length - i; k++)
					{
						key = "client.test.P" + k;
						if(verbose) System.out.println(key + " = " + args[i+k]);
						runProperties.setProperty(key, args[i+k]);
					}
				}
			}
		}
		catch(java.io.FileNotFoundException e1)
		{
			e1.printStackTrace();
			System.exit(1);
		}
		catch(java.io.IOException e2)
		{
			e2.printStackTrace();
			System.exit(1);
		}
		catch(Exception e3)
		{
			e3.printStackTrace();
			System.exit(1);
		}
	}

  	private static void functionTest_proc(BrokerHome myHome)
	{
		System.out.println("\n To perform functional Test:\n");

		String testName = runProperties.getProperty("client.test.P1");
    		System.out.println("To test: " + testName);

    		try 
		{
    			System.out.println("To create a broker bean instance");
   			Broker broker = myHome.create();

			if ( testName.equalsIgnoreCase("create") )
			{
				String name = runProperties.getProperty("client.test.P2", "test");
				String addr = runProperties.getProperty("client.test.P3", "CMIS");
				int  credit = Integer.parseInt(runProperties.getProperty("P4", "10000"));

				System.out.println("\t name   = " + name);
				System.out.println("\t addr   = " + addr);
				System.out.println("\t credit = " + credit);
 
				int accountID = broker.newAccount(name, addr, credit);
				System.out.println("the new accountID = " + accountID);
			}
			else if ( testName.equalsIgnoreCase("update") )
			{
				int accountID = Integer.parseInt(runProperties.getProperty("client.test.P2", "1"));				
				int    credit = Integer.parseInt(runProperties.getProperty("client.test.P3", "10000"));
				System.out.println("\t accounID = " + accountID); 
				System.out.println("\t credit   = " + credit);

      			broker.updateAccount(accountID, credit);
				System.out.println("........................OK");
			}
			else if ( testName.equalsIgnoreCase("query") )
			{
				int stockID = Integer.parseInt(runProperties.getProperty("client.test.P2", "1"));
	    			System.out.println("\t stockID = " + stockID);

				QueryResult rs = (QueryResult) broker.queryStockValueByID(stockID);

				System.out.println("current_val = " + rs.current_val);
 				System.out.println("   high_val = " + rs.high_val);
				System.out.println("    low_val = " + rs.low_val);
			}
			else if ( testName.equalsIgnoreCase("buy") )
			{
				int accountID = Integer.parseInt(runProperties.getProperty("client.test.P2", "1"));
				int stockID   = Integer.parseInt(runProperties.getProperty("client.test.P3", "10"));
				int amount    = Integer.parseInt(runProperties.getProperty("client.test.P4", "10"));
	
				System.out.println("\t accountID = " + accountID);
 				System.out.println("\t stockID   = " + stockID);
				System.out.println("\t amount    = " + amount);

				broker.buyStock(accountID, stockID, amount);
				System.out.println("Buy.........................OK");
			}
			else if ( testName.equalsIgnoreCase("sell") )
			{
				int accountID = Integer.parseInt(runProperties.getProperty("client.test.P2", "1"));
				int stockID   = Integer.parseInt(runProperties.getProperty("client.test.P3", "10"));
				int amount    = Integer.parseInt(runProperties.getProperty("client.test.P4", "10"));
	
				System.out.println("\t accountID = " + accountID);
 				System.out.println("\t stockID   = " + stockID);
				System.out.println("\t amount    = " + amount);

				broker.sellStock(accountID, stockID, amount);
				System.out.println("Sell........................OK");
			}
			else if ( testName.equalsIgnoreCase("holding") )
			{
				int accountID = Integer.parseInt(runProperties.getProperty("client.test.P2", "1"));
				System.out.println("\t accountID = " + accountID);

				Collection list = broker.getHoldingStatement(accountID, 0);
				Iterator iterator = list.iterator();

				for(int i=0; iterator.hasNext(); i++)
				{
					Holding hold = (Holding) iterator.next();
					System.out.println("stockID = " + hold.stock_id + " amount = " + hold.amount);
				}
    			}
			else if ( testName.equalsIgnoreCase("testrollback") )
			{
				int accountID = broker.newAccountForTestRollback("test", "rollback", 8888);
				System.out.println("accountID = " + accountID);

				if(broker.testRollback(accountID))
    					System.out.println("Rollback................OK");
				else
    					System.out.println("Rollback................Failed");
    			}
			else
			{
				throw new Exception("Invalid test name. Run the program with no args to get help.");
			}

			// To remove the bean after testing
			broker.remove();
		}
		catch (Exception ex) 
		{
      		System.err.println(ex.toString());
    		}
		
		System.exit(0);
  	}

	private static void sleep(int inval)
     	{
		try 
		{
			Thread.sleep(inval);
		} catch(Exception ex1) {}
	}
}
