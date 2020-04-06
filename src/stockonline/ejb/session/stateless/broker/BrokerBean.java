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
//		26/11/2001	Shiping	Change to avoide application excpetions:
//				If an account's credit is not enough, still keep going.
//
//
//

package stockonline.ejb.session.stateless.broker;

import java.rmi.*;
import java.util.*;
import java.sql.*;

import javax.sql.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import stockonline.util.*;				// common stuff shared between clients and EJB servers
import stockonline.ejb.session.interf.*;		// stockonline public interfaces
import stockonline.ejb.sql.*;				// SQL stuff in EJB servers


/** This class implements the business logic for the Stockonline application.
*/

public class BrokerBean implements SessionBean 
{
	// Internal constances
	//
	final static boolean verbose 		= false;
	final static String BEAN_NAME 	= "statelessBroker";
      final static String RESOURCE_NAME   = "java:comp/env/jdbc/StockDB";

	protected SessionContext 	ctx 		= null;
	private DataSource		ds 		= null;
	private Account			account 	= new Account();
	private StockItem			stock		= new StockItem();
	private StockHolding		holding 	= new StockHolding();
 	private StockTx			transaction = new StockTx();

	private boolean timing = false;
	private stockonline.util.Timer	timer			= new stockonline.util.Timer();
	private stockonline.util.ResultLog	buyLog		= new stockonline.util.ResultLog();
	private stockonline.util.ResultLog	sellLog		= new stockonline.util.ResultLog();
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
			System.err.println("Exception in newAccount(): " + ex.toString());

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
			System.err.println("Exception in queryStockValueByID(): " + ex.toString());

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
			System.err.println("Exception in queryStockValueByCode(): " + ex.toString());

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
			System.err.println("Exception in buyStock(): " + ex.toString());

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
			if(verbose) print("buyStock()......OK: return txID = " + txID);
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
			System.err.println("Exception in sellStock(): " + ex.toString());

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
			System.err.println("Exception in updateAccount(): " + ex.toString());

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
			System.err.println("Exception in updateAccount(): " + ex.toString());

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

	/**	This method provides an interface for clients and servers to communicate each other
	* 	@param msg	The command 
	* 	@return	The expected message in a string
	*/
	public Collection cmdChannel(CmdMessage msg) 
		throws Exception
	{
		if(verbose) print("cmdChannel(" + msg.cmd + ") called"); 

		ArrayList list = new ArrayList();

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

	/**	To create a new account for rollbacktest
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
			if(verbose) print("accountID = " + accountID);

			if(verbose) System.out.println("To rollback");
			ctx.setRollbackOnly();
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
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
	// Internal methods
	//-----------------------------------------------

	private int newAccount_Imp(String name, String address, int credit)
		throws Exception
	{
		if(verbose) print("newAccount_Imp(" + name + ", " + address + ", " + credit + ") called"); 

		Connection con = ds.getConnection();

		try
		{
			int accountID = account.createAccount(con, name, address, credit);
			return accountID;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private float getCurrentCredit_Imp(int accountID)
		throws Exception
	{
		if(verbose) print("getCurrentCredit_Imp(" + accountID + ") called"); 

		Connection con = ds.getConnection();

		try
		{
			float credit = account.getCredit(con, accountID);
			return credit;
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private void updateAccount_Imp(int accountID, int credit)
		throws Exception
	{
		if(verbose) print("nupdateAccount_Imp(" + accountID + ", " + credit + ") called"); 

		Connection con = ds.getConnection();

		try
		{
			account.updateCredit(con, accountID, credit);
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private QueryResult queryStockValueByID_Imp(int stockID)
		throws Exception
	{
		if(verbose) print("queryStockValueByID_Imp(" + stockID + ") called");

		Connection con = ds.getConnection();

		try
		{
			QueryResult prices = stock.queryByID(con, stockID);
			return prices;		
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private QueryResult queryStockValueByCode_Imp(String stockCode)
		throws Exception
	{
		if(verbose) print("queryStockValueByCode_Imp(" + stockCode + ") called");

		Connection con = ds.getConnection();

		try
		{
			QueryResult prices = stock.queryByCode(con, stockCode);		
			return prices;		
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private int buyStock_Imp(int accountID, int stockID, int amount)
		throws Exception
	{
		if(verbose) print("bugStock_Imp(" + accountID + ", " + stockID + ", " + amount + ") called");

		// To get a connection
		Connection con = ds.getConnection();

		try
		{
			// Step 1. To check credit
			//		If the account does not have enough credit to buy,
			//		an exception will be thrown.
			//
			float credit = account.getCredit(con, accountID);
			if(verbose) System.out.println("credit = $" + credit);

			float current_val = stock.getCurrentPrice(con, stockID);
			if(verbose) System.out.println("current_val = $" + current_val);

			float payment = current_val * amount;
			if(verbose) System.out.println("payment = $" + payment);

			// if (credit < payment) // if the account cannot afford the buy
			// throw new Exception("No enough credit for the buy transaction: accountID = " + accountID);

			// Step 2. To update stockholding associated with this account
			//
			holding.updateForBuy(con, accountID, stockID, amount);

			// Step 3. To update the account credit for the transaction. 
			//	(Should do, but we do NOT do that currently)
			//
			// account.updateCredit(con, accountID, -payment);

			// Step 4. To create the transaction record
			//
			int txID = transaction.create(con, "B", accountID, stockID, amount, current_val);

			return txID;
		}
		catch (Exception ex)
		{
			System.err.println("Exception in BrokerBean.buyStock_Imp(): " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private int sellStock_Imp(int accountID, int stockID, int amount) 
		throws Exception
	{
		if(verbose) print("sellStock_Imp(" + accountID + ", " + stockID + ", " + amount + ") called");

		// To get a connection
		Connection con = ds.getConnection();

		try
		{
			// Step 2. To get current price
			//
			float currentPrice = stock.getCurrentPrice(con, stockID);
			if(verbose) System.out.println("currentPrice = " + currentPrice);

			// Step 1. To update stockholding associated with this account
			//		If the account does not hold or does not hold enough holding for sell,
			//		an exception will be thrown.
			//
			holding.updateForSell(con, accountID, stockID, amount);

			// Step 3. To update the account credit for the transaction. 
			//	(Should do, but we do NOT do that currently)
			//
			// float payment = currentPrice * amount;
			// account.updateCredit(con, accountID, payment);

			// Step 4. To create the transaction record
			//
			int txID = transaction.create(con, "S", accountID, stockID, amount, currentPrice);

			return txID;
		}
		catch (Exception ex)
		{
			System.err.println("Exception in BrokerBean.sellStock_Imp(): " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	private Collection getHoldingStatement_Imp (int accountID, int start_stockID)
		throws Exception
	{
		if(verbose) print("getHoldingStatement_Imp(" + accountID + ", " + start_stockID + ") called");

		Connection con = ds.getConnection();

		try
		{
			Collection list = holding.getHoldingList(con, accountID, start_stockID);		
			return list;		
		}
		catch(Exception ex)
		{
			System.err.println(BEAN_NAME + " Exception: " + ex.getMessage());
			throw ex;
		}
		finally
		{
			con.close();
		}
	}

	// Print out a message with the current thread ID
	//
      private void print( String message )
	{
		System.out.println("[" + Thread.currentThread().getName() + "] " + message);
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
	public void ejbCreate() 
		throws CreateException	
	{
		if(verbose) print("ejbCreate() called");

		try
		{
			if(verbose) print("To get datasource: " + RESOURCE_NAME);
			ds = (DataSource)(new InitialContext()).lookup(RESOURCE_NAME);
		}
		catch (javax.naming.NamingException ex)
		{
			System.err.println(BEAN_NAME + ".ejbCreate(): " + ex.toString());
			// throw new CreateException(ex.toString());
    		}
	}

	public void ejbRemove() 	{}
	public void ejbActivate()	{}
	public void ejbPassivate()	{}
}

