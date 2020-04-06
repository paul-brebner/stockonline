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

public class Account 
{
	/** A boolean varible to control debug, default is true
      */    
	final static boolean verbose	= false;
	final static String className = "Account";

	public Account() {}

	public static int createAccount (Connection conn, String name, String address, int credit) throws Exception 
	{
		if(verbose) System.out.println(className + ".createAccount() called");

		int accountID = SeqGenerator.getNextAccountID(conn);
 		createAccountRecord(conn, accountID, name, address, credit);
		if(verbose) System.out.println(className + ".createAccount(): accountId = " + accountID + " created");

		return accountID;
	}

    	public static void updateCredit( Connection conn, int accountID, int credit) throws Exception
	{
		if(verbose) System.out.println(className + ".updateCredit() called");

		PreparedStatement pstmt = null;

    		try 
		{
			String sql = "UPDATE subaccount SET sub_credit = ? WHERE sub_accno = ?";
      		if(verbose) System.out.println(sql);

      		pstmt = conn.prepareStatement(sql);
      		pstmt.setInt(1, credit);
      		pstmt.setInt(2, accountID);
      		pstmt.executeUpdate();

			pstmt.close();
		} 
		catch (SQLException ex) 
		{
			System.err.println(className + ".updateCredit(): " + ex.toString());
			throw new Exception(ex.toString());
    		}
	}

    	public static float getCredit (Connection conn, int accountID) throws Exception 
	{
		if(verbose) System.out.println("SubAccount.getCredit(conn, " + accountID + ") called");

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			String sql = "SELECT sub_credit FROM Subaccount WHERE sub_accno = ?";
			if(verbose) System.out.println(sql);

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, accountID);
			rs = pstmt.executeQuery();

			if(rs==null)   throw new SQLException("SubAccount.getCredit(): rs = null");
			if(!rs.next()) throw new SQLException("Fail to get current credit for accountID = " + accountID);

			float currentCredit= rs.getFloat(1);
			if(verbose) System.out.println("currentCredit = " + currentCredit);

			return currentCredit;
		}
		catch(SQLException ex)
		{
			System.err.println("Exception in SubAccount.getCredit(): " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		finally
		{
			if(rs!=null)	rs.close();
			if(pstmt!=null)	pstmt.close();
		}
	}

	// Interanl methods
	//
	private static void createAccountRecord(	Connection 	conn,
							int 		sub_accno,
							String 	sub_name,
							String 	sub_address,
							int		sub_credit) 
	throws Exception
	{
		if(verbose) System.out.println(className + ".createAccountRecord() called"); 

		PreparedStatement pstmt = null;

    		try 
		{
			String sql = "INSERT INTO subaccount VALUES (?,?,?,?)";
      		if(verbose) System.out.println(sql);

      		pstmt = conn.prepareStatement(sql);
      		pstmt.setInt(1, sub_accno);
      		pstmt.setString(2, sub_name);
      		pstmt.setString(3, sub_address);
      		pstmt.setFloat(4, sub_credit);
      		pstmt.executeUpdate();

			pstmt.close();
		} 
		catch (SQLException ex) 
		{
			System.err.println(className + ".createAccountRecord(): " + ex.toString());
			throw new Exception(ex.toString());
    		}
	}
}