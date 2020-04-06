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
//		25/11/2001	Shiping	Change to avoid application exceptions:	
//					If an account does not have enough holding, still keep going.
//
//
//
//

package stockonline.ejb.sql;

import java.sql.*;
import javax.sql.DataSource;
import java.util.*;

import stockonline.util.Holding;

public class StockHolding implements java.io.Serializable 
{
	final static boolean	verbose	= false;
	final static String	className	= "StockHolding";

	private int stock_id;
	private int stock_amount;

	public StockHolding () {}

	/** To get holding list for an account
	*	@param conn			The connection to the database
	*	@param accountID		The account iditifier
	*	@param start_stockID	The stockID from which the list starts, default is 0
	*/
	public  static Collection getHoldingList (Connection conn, int accountID, int start_stockID)
	throws Exception 
	{
		if(verbose) System.out.println(className + ".getHoldingList(" + accountID + "," + start_stockID + ") called");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int rowNum  = 20;

		try
		{
		   // String sql = "SELECT stock_id, amount FROM StockHolding WHERE sub_accno = ? AND stock_id > ? AND rownum <= ?";
			String sql = "SELECT stock_id, amount FROM StockHolding WHERE sub_accno = ? AND stock_id > ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, accountID);
			pstmt.setInt(2, start_stockID);
			// pstmt.setInt(3, rowNum);

			rs = pstmt.executeQuery();

			ArrayList list = new ArrayList();
        		for(int i=0; rs.next() && i<rowNum; i++)
			{
				Holding holding = new Holding();

	    			holding.stock_id	= rs.getInt(1);
            		holding.amount 	= rs.getInt(2);
				if(verbose) System.out.println("stock_id = " + holding.stock_id + " amount = " + holding.amount);

	    			list.add( holding );
        		}

			return list;
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in StockHolding.getHoldingList(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}
    	}

	/**	To update the stock holding for buy transactions
	*	@param conn		The connection to the database
	*	@param accountID	The account iditifier
	*	@param stockID	The stock iditifier
	*	@param amount	The amount to buy
	*/
	public static void updateForBuy (Connection conn, int accountID, int stockID, int amount)
	throws Exception
	{
		if(verbose) System.out.println(className + ".updateForBuy(" + accountID + "," + stockID + "," + amount + ") called");
 
		PreparedStatement pstmt  = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT amount FROM StockHolding WHERE sub_accno = ? AND stock_id = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, accountID);
			pstmt.setInt(2, stockID);
			rs = pstmt.executeQuery();

			if (rs.next())	// if the account holds the stock, then update the record
			{
				sql = "UPDATE StockHolding SET amount = ? WHERE sub_accno = ? AND stock_id = ?";
				if(verbose) System.out.println(sql);

            		pstmt1 = conn.prepareStatement(sql);
				pstmt1.setInt(1, rs.getInt(1) + amount);
				pstmt1.setInt(2, accountID);
				pstmt1.setInt(3, stockID);
				pstmt1.executeUpdate();
			}
			else		// if the account does NOT hold the stock, then create a new record
			{
				sql = "INSERT INTO StockHolding VALUES (?,?,?)";
				if(verbose) System.out.println(sql);

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setInt(1, accountID);
				pstmt1.setInt(2, stockID);
				pstmt1.setInt(3, amount);
				pstmt1.executeUpdate();
			}
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in StockHolding.updateForBuy(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
			if(pstmt1!=null)	pstmt1.close();
		}
	}

	/**	To update the stock holding for sell transactions
	*	@param conn		The connection to the database
	*	@param accountID	The account iditifier
	*	@param stockID	The stock iditifier
	*	@param amount	The amount to sell
	*/
	public static void updateForSell (Connection conn, int accountID, int stockID, int amount)
	throws Exception
	{
		if(verbose) System.out.println(className + ".updateForSell(" + accountID + "," + stockID + "," + amount + ") called");
 
		PreparedStatement pstmt  = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT amount FROM StockHolding WHERE sub_accno = ? AND stock_id = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, accountID);
			pstmt.setInt(2, stockID);
			rs = pstmt.executeQuery();

			if (!rs.next())	
				throw new SQLException("StockHolding.updateForSell(): The account does not hold the stock to sell");

			int currentHolding = rs.getInt(1);			
			if(verbose) System.out.println("currentHolding = " + currentHolding);

			if(currentHolding < amount)
				// throw new SQLException("StockHolding.updateForSell(): The account does not hold enough the stock to sell");
				amount = currentHolding;	// To sell the current holding amount

			sql = "UPDATE StockHolding SET amount = ? WHERE sub_accno = ? AND stock_id = ?";
			if(verbose) System.out.println(sql);

            	pstmt1 = conn.prepareStatement(sql);
			pstmt1.setInt(1, currentHolding - amount);
			pstmt1.setInt(2, accountID);
			pstmt1.setInt(3, stockID);
			pstmt1.executeUpdate();
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in StockHolding.updateForSell(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
			if(pstmt1!=null)	pstmt1.close();
		}
	}
}