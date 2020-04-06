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
//		19/11/2001	Shiping	Change dateFormatter to dynamic varible due to
//						that java.text.SimpleDateFormat is not thread safe.
//
//

package stockonline.ejb.sql;

import java.sql.*;
import javax.sql.DataSource;

public class StockTx 
{
	/** A boolean varible to control debug, default is true
      */    
	final static boolean	verbose = false;
    	private java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("dd/MM/yy");

    	public StockTx() {}

    	/**
	* addTransaction with internal txID
	*/
	public int create (	Connection	conn,
				 	String	txType,
					int 		accountID,
					int		stockID,
					int 		amount,
					float		price)
	throws Exception
	{
		if(verbose)
		{
			System.out.println("Transaction.create(Connection conn) called");
			System.out.println("txType 	= " + txType);
			System.out.println("accountID = " + accountID);
			System.out.println("stockID	= " + stockID);
			System.out.println("amount	= " + amount);
			System.out.println("price	= " + price);
		}

		int txID = SeqGenerator.getNextTxID(conn);
		if(verbose) System.out.println("generated txID = " + txID);

		create_Imp(conn, txID, txType, accountID, stockID, amount, price);

		return txID;
	}

	// Internal methods
	//
	private void create_Imp (	Connection	conn,
						int		trans_id,
						String	trans_type,
						int 		sub_accno,
						int		stock_id,
						int 		amount,
						float		price)
	throws Exception
	{
		PreparedStatement pstmt = null;

		try
		{
			String sql = "INSERT INTO StockTransaction VALUES (?, ?, ?, ?, ?, ?, ?)";
			if(verbose) System.out.println(sql);

			String currentDate = dateFormatter.format( new java.util.Date() );
			if(verbose) System.out.println(currentDate);

        		pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1,	trans_id);
			pstmt.setString(2,trans_type);
			pstmt.setInt(3, 	sub_accno);
			pstmt.setInt(4, 	stock_id);
			pstmt.setInt(5, 	amount);
			pstmt.setFloat(6, price);
			pstmt.setString(7,currentDate);

			pstmt.executeUpdate();
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in StockTransaction.create_Imp(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(pstmt!=null) pstmt.close();
		}
	}
}