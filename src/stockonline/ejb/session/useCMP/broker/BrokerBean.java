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
//		15/11/2001	Shiping	Change all entity beans' names to their table names.
//
//		26/11/2001	Shiping	Two changes to avoide application excpetions
//                              (Note: These should not occur if database has been correctly initialised):
//						1. If an account's credit is not enough, keep going
//						2. If an account's holding is not enough, keep going
//

package stockonline.ejb.session.useCMP.broker;

import java.rmi.*;
import java.util.*;
import java.sql.*;

import javax.sql.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
// import javax.transaction.UserTransaction;

import stockonline.util.*;			// common stuff shared between clients and EJB servers
import stockonline.ejb.session.interf.*;	// stockonline session beans interfaces
import stockonline.ejb.entity.interf.*;		// stockonline entity beans interfaces
import stockonline.ejb.sql.SeqGenerator;

/** This class implements the business logic for the Stockonline application.
*/

public class BrokerBean implements SessionBean 
{
	// Internal constances
	//
	final static boolean verbose 					= true;
	final static String BEAN_NAME 					= "cmpBroker";
	final static String RESOURCE_NAME 				= "java:comp/env/jdbc/StockDB";
	final static String accountHomeJNDI_NAME		= "java:comp/env/ejb/SubAccount";
	final static String stockItemHomeJNDI_NAME		= "java:comp/env/ejb/StockItem";
	final static String stockHoldingHomeJNDI_NAME	= "java:comp/env/ejb/StockHolding";
	final static String stockTxHomeJNDI_NAME		= "java:comp/env/ejb/StockTransaction";

	protected SessionContext 	ctx = null;
	private DataSource			ds 	= null;
	private AccountHome 		accountHome		= null;
	private StockItemHome 		stockItemHome	= null;
	private StockHoldingHome 	stockHoldingHome= null;
	private StockTxHome 		stockTxHome		= null;

	private boolean timing = false;
	private stockonline.util.Timer	timer				= new stockonline.util.Timer();
	private stockonline.util.ResultLog	buyLog			= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	sellLog			= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	updateLog		= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	createLog		= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	queryIDLog		= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	queryCodeLog	= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	getHoldingLog	= new stockonline.util.ResultLog();

	// ----------------
	// Business methods
	//-----------------

	/**	To create a new account
	* 	@param name		The name of the account owner
	*	@param address	The address of the account owner
	*	@param credit	The initial credit in the account
	*	@return		The iditifier of the new account
	*/
	public int newAccount(String name, String address, int credit) 
		throws Exception
	{
		if(verbose) print("newAccount(" + name + ", " + address + ", " + credit + ") called");

		try
		{
			if(timing) timer.start();

			int accountID = newAccount_Imp(name, address, credit);

			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("createTime = ");
				createLog.addSample(timer.getTime());
			}

			// To test transaction rollback: 
			// only the accounts with even account numbers should be created.
			// 
			// if(accountID%2==1) throw new Exception("To test rollback of a transaction");

			return accountID;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".newAccount(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			} 
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in newAccount(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	To query a stock's prices by its ID
	* 	@param stockID	The iditifer of the stock (1-3000)
	* 	@return		An object which wraps the stock prices
	*/
	public QueryResult queryStockValueByID(int stockID)
		throws Exception
	{
		if(verbose) print("queryStockValueByID(" + stockID + ") called");
	
		try
		{
			if(timing) timer.start();

			QueryResult prices = queryStockValueByID_Imp(stockID);

			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("queryIDTime = ");
				queryIDLog.addSample(timer.getTime());
			}

			return prices;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".queryStockValueByID(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			} 
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in queryStockValueByID(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	To query a stock's prices by its Code
	* 	@param stockCode	The code of the stock (C1-C3000)
	* 	@return		An object which wraps the stock prices
	*/
/*
	public QueryResult queryStockValueByCode(String stockCode) 
		throws Exception
	{
		if(verbose) print("queryStockValueByCode(" + stockCode + ") called");
	
		try
		{
			if(timing) timer.start();
			QueryResult prices = queryStockValueByCode_Imp(stockCode);
			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("queryCodeTime = ");
				queryCodeLog.addSample(timer.getTime());
			}

			return prices;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".queryStockValueByCode(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			} 
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in queryStockValueByCode(), due to " + e.toString());
			}
			throw ex;
		}
	}
*/
	/**	To buy amount of a stock
	* 	@param accountID	The iditifier of the acccount
	* 	@param stockID	The iditifier of the stock
	* 	@param amount	The amount of the stock to buy
	*/
	public void buyStock(int accountID, int stockID, int amount) 
		throws Exception
	{
		if(verbose) print("buyStock(" + accountID + ", " + stockID + ", " + amount + ") called");

		try
		{
			if(timing) timer.start();

			int txID = buyStock_Imp(accountID, stockID, amount);
			if(verbose) print("buyStock()......OK: return txID = " + txID);

			if(timing)
			{	
				timer.stop();
				if(verbose) timer.printTime("buyTime = ");
				buyLog.addSample(timer.getTime());
			}

			return;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".buyStock(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			}
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in buyStock(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	To buy amount of a stock
	* 	@param accountID	The iditifier of the acccount
	* 	@param stockID	The iditifier of the stock
	* 	@param amount	The amount of the stock to sell
	*/
	public void sellStock(int accountID, int stockID, int amount) 
		throws Exception
	{
		if(verbose) print("sellStock(" + accountID + ", " + stockID + ", " + amount + ") called");

		try
		{
			if(timing) timer.start();

			int txID = sellStock_Imp(accountID, stockID, amount);
			if(verbose) print("sellStock()......OK: return txID = " + txID);

			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("sellTime = ");
				sellLog.addSample(timer.getTime());
			}

			return;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".sellStock(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			}
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in sellStock(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	To update the credit of an account
	* 	@param accountID	The iditifier of the acccount
	* 	@param credit	The credit to be increased or decreased (-)
	*/
	public void updateAccount(int accountID, int credit)
		throws Exception
	{
		if(verbose) print("updataAccount(" + accountID + ", " + credit + ")");

		try
		{
			if(timing) timer.start();
			updateAccount_Imp(accountID, credit);
			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("updayeTime = ");
				updateLog.addSample(timer.getTime());
			}

			if(verbose) print("updataAccount()......OK: return void");
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".updateAccount(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			}
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in updateAccount(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	To get current stock holding statement for an account
	* 	@param accountID		The iditifier of the acccount
	*	@param start_stockID	The stockID from which the list starts, default is 0
	*	@return 			A set of objects of Holding class, which wraps the holding information.
	*	@see 				stockonline.util.Holding
	*/
	public Collection getHoldingStatement(int accountID, int start_stockID) 
		throws Exception
	{
		if(verbose) print("getHoldingStatement(" + accountID + ")");

		try
		{
			if(timing) timer.start();
			Collection list = getHoldingStatement_Imp (accountID, start_stockID);
			if(timing)
			{
				timer.stop();
				if(verbose) timer.printTime("getHoldingTime = ");
				getHoldingLog.addSample(timer.getTime());
			}

			return list;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".getHoldingStatement(): " + ex.toString());

			try 
			{
				if(verbose) print("To rollback");
				ctx.setRollbackOnly();
			}
			catch(Exception e) 
			{
				System.err.println("Fail to rollback in getHoldingStatement(), due to " + e.toString());
			}
			throw ex;
		}
	}

	/**	This method provides an interface for clients and servers to communicate each other
	* 	@param msg	The command 
	* 	@return	The expected message in a string
	*/
	public Collection cmdChannel(CmdMessage msg) 
		throws Exception
	{
		if(verbose) print("cmdChannel(" + msg.cmd + ") called"); 

		ArrayList list = null;

		switch(msg.cmd)
		{
		case CmdMessage.SET_TIMING_ON:
			if(verbose) System.out.println("SET_TIMING_ON");
			timing = true;
			break;
		case CmdMessage.SET_TIMING_OFF:
			if(verbose) System.out.println("SET_TIMING_OFF");
			timing = false;
			break;
		case CmdMessage.GET_TIMING_LOG:
			if(verbose) System.out.println("GET_TIMING_LOG");
			list = new ArrayList();

			list.add(buyLog);
			list.add(sellLog);
			list.add(createLog);
			list.add(updateLog);
			list.add(queryIDLog);
			list.add(queryCodeLog);
			list.add(getHoldingLog);

			if(verbose) System.out.println("GET_TIMING_LOG");
			break;
		case CmdMessage.RESET_DATABASE:
			if(verbose) System.out.println("RESET_DATABASE");
			DBLoad.resetOracleDB(ds);
			break;
		default:
			System.err.println("invalid cmd message");
			break;
		}
		
		return list;
	}

	/**	To create a new account for testing rollback
	* 	@param name		The name of the account owner
	*	@param address	The address of the account owner
	*	@param credit	The initial credit in the account
	*	@return		The iditifier of the new account
	*/
	public int newAccountForTestRollback(String name, String address, int credit) 
		throws Exception
	{
		if(verbose) print("newAccountForTestRollback(" + name + ", " + address + ", " + credit + ") called");
		int accountID = 0;

		try
		{
			accountID = newAccount_Imp(name, address, credit);
			if(verbose) System.out.println("accountID = " + accountID);

			if(verbose) print("To rollback");
			ctx.setRollbackOnly();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			return accountID;
		}
	}

	/**	To test rollback
	*	@param accountID	The iditifier of account
	*	@return		true = rollback sucessful; false = rollback unsucessful
	*/
	public boolean testRollback(int accountID) 
		throws Exception
	{
		if(verbose) print("testRollBack() called");
		boolean testPass = false;
		
		try
		{
			float credit = getCurrentCredit_Imp(accountID);
			testPass = false;
			if(verbose) System.out.println("Test failed !"); 
		}
		catch(Exception e)
		{
			testPass = true;
			if(verbose) System.out.println("Test Passed !"); 
		}
		
		return testPass;
	}


	//-----------------------------------------------
	// Implemenetation using the entity beans
	//-----------------------------------------------

	private int newAccount_Imp(String name, String address, int credit)
		throws Exception
	{
		if(verbose) print("newAccount_Imp(" + name + ", " + address + ", " + credit + ") called"); 

		try
		{
			// get key within the entity bean 
			// Account account = accountHome.create(name, address, credit);
			// AccountPK pk = (AccountPK) account.getPrimaryKey();

			// get key outside the entity bean
			AccountPK pk = new  AccountPK();
			pk.sub_accno = SeqGenerator.getNextAccountID(ds);
			Account account = accountHome.create(pk.sub_accno, name, address, credit);

			if(verbose) System.out.println("accountID = " + pk.sub_accno);
			return pk.sub_accno;
		}
		catch(CreateException e1)
		{
			System.err.println(BEAN_NAME + ".newAccount_Imp(): " + e1.toString());
			throw new Exception(e1.toString());
		}
		catch(Exception e2)
		{
			System.err.println(BEAN_NAME + ".newAccount_Imp(): " + e2.toString());
			throw new Exception(e2.toString());
		}
	}

	private float getCurrentCredit_Imp(int accountID)
		throws Exception
	{
		if(verbose) print("getCurrentCredit_Imp(" + accountID + ") called"); 

		try
		{
			Account account = accountHome.findByPrimaryKey(new AccountPK(accountID));
			float credit = (float) account.getCredit();
			if(verbose) System.out.println("credit = $" + credit);

			return credit;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}


	private void updateAccount_Imp(int accountID, int credit)
		throws Exception
	{
		if(verbose) print("updateAccount_Imp(" + accountID + ", " + credit + ") called");

		try
		{
			Account account = accountHome.findByPrimaryKey(new AccountPK(accountID));
			account.update(credit);

			return;	
		}
		catch(FinderException e1)
		{
			System.err.println(BEAN_NAME + ".updateAccount_Imp(): " + e1.toString());
			throw new Exception(e1.toString());
		}
		catch(Exception e2)
		{
			System.err.println(BEAN_NAME + ".updateAccount_Imp(): " + e2.toString());
			throw new Exception(e2.toString());
		}
	}

	private QueryResult queryStockValueByID_Imp(int stockID)
			throws Exception
	{
		if(verbose) print("queryStockValueByID_Imp(" + stockID + ") called");

		try
		{
			StockItemPK pk = new StockItemPK(stockID);
			StockItem stockItem = stockItemHome.findByPrimaryKey(pk);
			QueryResult prices = stockItem.getValues();

			if(verbose)
			{
				System.out.println("current_val = " + prices.current_val);
				System.out.println("   high_val = " + prices.high_val);
				System.out.println("    low_val = " + prices.low_val);
			}

			return prices;
		}
		catch(Exception ex)
		{
			System.err.println("Exception in BrokerBean.queryStockValueByID_Imp(): " + ex.toString());
			throw ex;
		}
	}
/*
	private QueryResult queryStockValueByCode_Imp(String stockCode)
			throws SQLException, Exception
	{
		if(verbose) print("queryStockValueByCode_Imp(" + stockCode + ") called");

		try
		{
			StockItem stockItem = stockItemHome.findByCode(stockCode);
			QueryResult prices = stockItem.getValues();

			if(verbose)
			{
				System.out.println("current_val = " + prices.current_val);
				System.out.println("   high_val = " + prices.high_val);
				System.out.println("    low_val = " + prices.low_val);
			}

			return prices;
		}
		catch(Exception ex)
		{
			System.err.println("Exception in BrokerBean.queryStockValueByID_Imp(): " + ex.toString());
			throw ex;
		}
	}
*/
	private int buyStock_Imp(int accountID, int stockID, int amount)
		throws Exception
	{
		if(verbose) print("bugStock_Imp(" + accountID + ", " + stockID + ", " + amount + ") called");

		try
		{
			// Step 1. To check credit
			//		If the account does not have enough credit to buy,
			//		an exception will be thrown.
			//
			Account account = accountHome.findByPrimaryKey(new AccountPK(accountID));
			float credit = (float) account.getCredit();
			if(verbose) System.out.println("credit = $" + credit);

			StockItem stockitem = stockItemHome.findByPrimaryKey(new StockItemPK(stockID));
			float current_val = stockitem.getCurrentValue();
			if(verbose) System.out.println("current_val = $" + current_val);

			float payment = current_val * amount;
			if(verbose) System.out.println("payment = $" + payment);

			// if (credit < payment) // if the account cannot afford the buy
			// 	throw new Exception("No enough credit for the buy transaction: accountID = " + accountID);

			// Step 2. To update stockholding associated with this account
			//
			try 
			{
				StockHolding stockholding = stockHoldingHome.findByPrimaryKey(new StockHoldingPK(accountID, stockID));
				// if(verbose) System.out.println("This account holds this stock, so update it");
				stockholding.increase(amount);
			}
			catch (FinderException e) 
			{
				// if(verbose) System.out.println("This account does not hold the stock, thus create a new record");
				stockHoldingHome.create(accountID, stockID, amount);
			}

			// Step 3. To update the account credit for the transaction. 
			//	(Should do, but we do NOT do that currently)
			//
			// account.withdrow( (int) payment);

			// Step 4. To create the transaction record
			//
			StockTxPK pk = new StockTxPK();
			pk.trans_id  = SeqGenerator.getNextTxID(ds);
			StockTx stockTx = stockTxHome.create(pk.trans_id, "B", accountID, stockID, amount, current_val);
			if(verbose) System.out.println("pk.trans_id = " + pk.trans_id);

			return pk.trans_id;
		}
		catch(Exception ex)
		{
			System.err.println("Exception in buyStock_Imp(): " + ex.toString());
			throw ex;
		}
	}

	private int sellStock_Imp(int accountID, int stockID, int amount) 
		throws Exception
	{
		if(verbose) print("sellStock_Imp(" + accountID + ", " + stockID + ", " + amount + ") called");

		try 
		{
			// Step 1. To check and update the stock holding
			//		If the account does not hold or does not hold enough holding for sell,
			//		an exception will be thrown.
			//
			StockHolding stockholding = null;
			try
			{
				stockholding = stockHoldingHome.findByPrimaryKey(new StockHoldingPK(accountID, stockID));
			}
			catch (FinderException ex)
			{
				throw new Exception("The account does not hold the stock.");
			}

			int temp = stockholding.getAmount();
			if (amount > temp)
				// throw new Exception("The account doest not hold enough amount of the stock to sell.");
				amount = temp;	// To sell what you have.

			// To update the stock holding 
			stockholding.decrease(amount);

			// Step 2. To get the stock price
			//
			StockItem stockitem = stockItemHome.findByPrimaryKey(new StockItemPK(stockID));
			float current_val = stockitem.getCurrentValue();
			if(verbose) System.out.println("current_val = $" + current_val);

			// Step 3. To update the account credit for the transaction. 
			//	(Should do, but we do NOT do that currently)
			//
			// account.deposit( (int) current_val*amount );

			// Step 4. To create the transaction record
			//
			StockTxPK pk = new StockTxPK();
			pk.trans_id  = SeqGenerator.getNextTxID(ds);
			StockTx stockTx = stockTxHome.create(pk.trans_id, "S", accountID, stockID, amount, current_val);
			if(verbose) System.out.println("pk.trans_id = " + pk.trans_id);

			return pk.trans_id;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".buyStock_Imp(): " + ex.toString());
			throw ex;
		}
	}

	private Collection getHoldingStatement_Imp (int accountID, int start_stockID)
		throws Exception
	{
		if(verbose) print("getHoldingStatement_Imp(" + accountID + ", " + start_stockID + ") called");

		try
		{
			if(verbose) print("To get holding list by calling stockHoldingHome.findByAccountID()...");
			Collection list1 = stockHoldingHome.findByAccountID(accountID, start_stockID);   // list for entity beans
			Collection list2 = new ArrayList();                                              // list for holdings

			Iterator it = list1.iterator();
			int rowNum = 20;

			for(int i=0; it.hasNext() && i<rowNum; i++)
			{
				StockHolding sh = (StockHolding) PortableRemoteObject.narrow(it.next(), StockHolding.class);
				Holding holding = sh.getHolding();
				if(verbose) System.out.println("stock_id = " + holding.stock_id + " amount = " + holding.amount);
				list2.add(holding);
			}

			return list2;
		}
		catch(FinderException e)
		{
			System.err.println(BEAN_NAME + ".getHoldingStatement_Imp(): " + e.toString());
			throw new Exception(e.toString());			
		} 
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + ".getHoldingStatement_Imp(): " + ex.toString());
			throw ex;
		}
	}

	// Print out a message with the current thread ID
	//
      private void print( String message )
	{
		System.out.println("[" + Thread.currentThread().getName() + "] " + message);
	}

	// To look up all resources required in the bean, eg. datasources and entity bean home.
	//
	private void lookupResources()
		throws CreateException
	{
		try
		{
			Object obj = null;

			if(verbose) System.out.println("To get a inital ctx...");
			InitialContext initCtx = new InitialContext();

			if(verbose) print("To get datasource: " + RESOURCE_NAME);
			ds = (DataSource) initCtx.lookup(RESOURCE_NAME);

			if(verbose) System.out.println("To get: " + accountHomeJNDI_NAME);
			obj = (Object) initCtx.lookup(accountHomeJNDI_NAME);
			accountHome = (AccountHome) javax.rmi.PortableRemoteObject.narrow(obj, AccountHome.class);

			if(verbose) System.out.println("To get: " + stockItemHomeJNDI_NAME);
			obj = (Object) initCtx.lookup(stockItemHomeJNDI_NAME);
			stockItemHome = (StockItemHome) javax.rmi.PortableRemoteObject.narrow(obj, StockItemHome.class);

			if(verbose) System.out.println("To get: " + stockHoldingHomeJNDI_NAME);
			obj = (Object) initCtx.lookup(stockHoldingHomeJNDI_NAME);
			stockHoldingHome = (StockHoldingHome) javax.rmi.PortableRemoteObject.narrow(obj, StockHoldingHome.class);

			if(verbose) System.out.println("To get: " + stockTxHomeJNDI_NAME);
			obj = (Object) initCtx.lookup(stockTxHomeJNDI_NAME);
			stockTxHome = (StockTxHome) javax.rmi.PortableRemoteObject.narrow(obj, StockTxHome.class);
		}
		catch (javax.naming.NamingException ex)
		{
			System.err.println(BEAN_NAME + " : " + ex.toString());
			throw new CreateException("Failed to create " + BEAN_NAME + ", due to " + ex.toString());
    	}
	}

	//-----------------------------------------------
	// EJB-required methods.  
	//-----------------------------------------------

	public void setSessionContext(SessionContext ctx)
	{
		this.ctx = ctx;
	}

	/** In this method, the datasource reference is obtained via JNDI.
	*/
	public void ejbCreate() throws CreateException	
	{
		if(verbose) print("ejbCreate() called");

		lookupResources();
	}

	public void ejbRemove() 	{}
	public void ejbActivate()	{}
	public void ejbPassivate()	{}
}

