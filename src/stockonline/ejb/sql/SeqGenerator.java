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
//          30/08/2001  Shiping     Two modifications: 
//                                  (1) Encapsulate datasource within the class
//                                  (2) Encapsulate the sequence names by providing two
//                                      interfaces: getNextAccountID() and getNextTxID().
//
//          08/11/2001 Shiping	Fix the bug: not close the DB connection for 
//                                  getNextAccountID(String resName) and 
//                                  getNextTxID(String resName)
//
//          16/11/2001 Shiping	Add interfaces for Datasource
//
//

package stockonline.ejb.sql;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

/**	This class implements a sequence generator, which can generate a set 
*	of unique numbers for pre-defined sequence name from the Oracle database.
*/

public class SeqGenerator 
{
	final static boolean verbose = false;
	final static String oralceSqlForAccountID  = "SELECT SubaccountSeq.nextval FROM dual";
	final static String oralceSqlForTxID = "SELECT StocktransactionSeq.nextval FROM dual";

    	/** 	Default Contructor
     	*/
	public SeqGenerator ()	{}

    	/** 	To get next account ID via connection
	* 	@param con	the connection to the database
	*	@return	a sequence number
     	*/
	public static int getNextAccountID(Connection con)
		throws Exception
	{
		return getNext(con, oralceSqlForAccountID);
	}

    	/** 	To get next account ID via datasource
	* 	@param resName	datasource
	*	@return		a sequence number
     	*/
	public static int getNextAccountID(DataSource ds)
		throws Exception
	{
		Connection con = ds.getConnection();
		int id = getNext(con, oralceSqlForAccountID);
		con.close();

		return id;
	}

    	/** 	To get next account ID vis datasource name
	* 	@param resName	datasource name
	*	@return		a sequence number
     	*/
	public static int getNextAccountID(String resName)
		throws Exception
	{
		Connection con = getConnection(resName);
		int id = getNext(con, oralceSqlForAccountID);
		con.close();

		return id;
	}

	// ----------------------------

    	/** 	To get next tx ID
	* 	@param con	the connection to the database
	*	@return	a sequence number
     	*/
	public static int getNextTxID(Connection con)
		throws Exception
	{
		return getNext(con, oralceSqlForTxID);
	}

    	/** 	To get next Tx ID via datasource
	* 	@param ds	datasource
	*	@return	a sequence number
     	*/
	public static int getNextTxID(DataSource ds)
		throws Exception
	{
		Connection con = ds.getConnection();
		int id = getNext(con, oralceSqlForTxID);
		con.close();

		return id;
	}

    	/** 	To get next Tx ID
	* 	@param resName	datasource name
	*	@return		a sequence number
     	*/
	public static int getNextTxID(String resName)
		throws Exception
	{
		Connection con = getConnection(resName);
		int id = getNext(con, oralceSqlForTxID);
		con.close();

		return id;
	}

	// ------------------------------
	// Internal methods
	// ------------------------------

	private static int getNext(Connection con, String sql)
		throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			if(verbose) System.out.println(sql);
			pstmt = con.prepareStatement(sql);
        		rs = pstmt.executeQuery();

        		if (!rs.next())
            		throw new SQLException("Failed to create the sequence: " + sql);

        		int seqVal = rs.getInt(1);
			if(verbose) System.out.println("SeqGenerator.getNext(): " + "The generated seqVal = " + seqVal);
	
			return seqVal;
		}
		catch(SQLException ex)
		{
			System.err.println("SeqGenerator.getNext(): " + ex.getMessage());
			throw new Exception(ex.toString());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}
	}

     	// To get a connection by giving the resource name
	//
	private static Connection getConnection (String resName)
		throws Exception
	{
		try
		{
			if(verbose) System.out.println("To look up datasource: " + resName);
			DataSource ds = (DataSource)(new InitialContext()).lookup(resName);

			return ds.getConnection();
		}
		catch (javax.naming.NamingException e1)
		{
			System.err.println(e1.toString());
			throw new Exception(e1.toString());
		}
		catch(SQLException e2)
		{
			System.err.println(e2.toString());
			throw new Exception(e2.toString());
		}
	}
}