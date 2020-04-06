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
//
//
package stockonline.ejb.sql;


import java.sql.*;
import javax.sql.DataSource;

import stockonline.util.QueryResult;

public class StockItem implements java.io.Serializable 
{
	final static boolean verbose = false;
	private static QueryResult prices = new QueryResult();

	public StockItem (){}

	/**	The SQL implementation to query stock prices by its ID
	*	@param conn		The connection to the database
	*	@param stockID	The iditifier of the stock to query
	*/	
	public static QueryResult queryByID (Connection conn, int stockID)  
		throws Exception 
	{
		if(verbose) System.out.println("StockItem.queryByID(conn, " + stockID + ") called");

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT current_val, high_val, low_val FROM StockItem WHERE stock_id = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, stockID);
			rs = pstmt.executeQuery();

			if(rs==null)   throw new SQLException("queryByID.rs = null");
			if(!rs.next()) throw new SQLException("Fail to query the prices for stockID = " + stockID);

			prices.current_val = rs.getFloat(1);
			prices.high_val = rs.getFloat(2);
			prices.low_val = rs.getFloat(3);

			if(verbose) print();
			return prices;
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in SockItem.queryByID(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}	
	}

	/**	The SQL implementation to query stock prices by its Code
	*	@param conn		The connection to the database
	*	@param stockCode	The code of the stock to query
	*/
	public static QueryResult queryByCode (Connection conn, String stockCode)  
		throws Exception 
	{
		if(verbose) System.out.println("StockItem.queryByCode(conn, " + stockCode + ") called");

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT current_val, high_val, low_val FROM StockItem WHERE code = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, stockCode);
			rs = pstmt.executeQuery();

			if(rs==null)   throw new SQLException("queryByCode.rs = null");
			if(!rs.next()) throw new SQLException("Fail to query the prices for stockCode = " + stockCode);

			prices.current_val = rs.getFloat(1);
			prices.high_val = rs.getFloat(2);
			prices.low_val = rs.getFloat(3);

			if(verbose) print();
			return prices;
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in SockItem.queryByCode(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}
	}

	/**	The SQL implementation to query stock current price by its ID.
	*	@param conn		The connection to the database
	*	@param stockID	The iditifier of the stock to query
	*/	
	public static float getCurrentPrice (Connection conn, int stockID)  
		throws Exception 
	{

		if(verbose) System.out.println("StockItem.getCurrentPrice(conn, " + stockID + ") called");

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT current_val FROM StockItem WHERE stock_id = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, stockID);
			rs = pstmt.executeQuery();

			if(rs==null)   throw new SQLException("queryCurrentPrice(): rs = null");
			if(!rs.next()) throw new SQLException("Fail to query the prices for stockID = " + stockID);

			prices.current_val = rs.getFloat(1);
			if(verbose) System.out.println("Current price = " + prices.current_val);

			return prices.current_val;
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in SockItem.getCurrentPrice(): " + ex.getMessage());
			throw new Exception(ex.toString());	
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}
	}

	// Internal method to print all prices
	//
	private static void print()
	{
		System.out.println("Current price = " + prices.current_val);
		System.out.println("   high price = " + prices.high_val);
		System.out.println("    low price = " + prices.low_val);
	}
}

