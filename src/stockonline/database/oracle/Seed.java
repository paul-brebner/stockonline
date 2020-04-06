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


/*
 * @(#) seed.java
 *
 * 
 *
 * This java program is to seed an oracle stockonline database 
 * using Oracle JDBC driver.
 * 
 * 
 */

package stockonline.database.oracle;

import java.util.*;
import java.io.*;
import java.sql.*;

public class Seed
{
	final boolean verbose = true;
	// Change the following settings for your database
	//
	String dbURL;
	String userName;
	String passWord;

	// The following parameters will be set on the command-line
	// 
	int		ACCT = 3000;		// number of accounts
	int		ITEM = 3000;		// number of stock items
	int		HOLDING = 10;		// number of stockholding per account

	Connection 	con;
	Statement 	stmt;
	String		sql;

	public Seed(String[] args) throws Exception
	{
		dbURL 	 = new String(args[0]);
		userName = new String(args[1]);
		passWord = new String(args[2]);

		System.out.println("Argments:");
		
		System.out.println("dbURL    = " + dbURL);
		System.out.println("userName = " + userName);
		System.out.println("passWord = " + passWord);
		
		System.out.println("Number of Account: " + ACCT);
		System.out.println("Number of Item   : " + ITEM);
		System.out.println("Number of Holding: " + HOLDING);

		try
		{
			System.out.println("To get a connection");
			init();

			System.out.println("To create stmt");
			stmt = con.createStatement();
			
			System.out.println("To seed the database...");
			population();

			con.close();
			System.out.println("\n....................done!");		
		}
		catch (SQLException se)
		{
			System.err.println("get SQL Exception: " + se);
			return;
		}
	}

	private void population() throws SQLException
	{
		cleanUp();

		seedAccount();
		seedStockItem();
		seedStockHolding();
	}

	private void cleanUp() throws SQLException
	{
		System.out.println("Clean up stockonline database...");

		sql = "truncate table StockTransaction";
		System.out.println(sql);
        stmt.executeUpdate(sql);
		
		sql = "truncate Table StockHolding";
		System.out.println(sql);
            	stmt.executeUpdate(sql);

		sql = "truncate table SubAccount";
		System.out.println(sql);
        stmt.executeUpdate(sql);

		sql = "truncate table StockItem";
		System.out.println(sql);
        stmt.executeUpdate(sql);
        
        try {
        	sql = "Drop sequence subaccountseq";
			System.out.println(sql);
        	stmt.executeUpdate(sql);
        	
        } catch (Exception e) {};
             
     	try {   
        	sql = "Drop sequence StockTransactionSeq";
			System.out.println(sql);
        	stmt.executeUpdate(sql);
        } catch (Exception e) {};
        
        sql = "Create sequence subaccountseq increment by 1 START WITH 3001 MAXVALUE 10000000 NOCYCLE CACHE 10000";
		System.out.println(sql);
        stmt.executeUpdate(sql);
        
        sql = "Create sequence StockTransactionSeq increment by 1 START WITH 1 MAXVALUE 10000000 NOCYCLE CACHE 10000";
		System.out.println(sql);
        stmt.executeUpdate(sql);
	}

	private void seedAccount() throws SQLException
	{
		System.out.println("Populating Account with "+ACCT+" records ...");

		for (int i=1; i<=ACCT; i++)
        {
			sql = "INSERT INTO subaccount VALUES("
                                   + i + ",'"
                                   + RandGenerator.randomStrFixLen(3) + "','"
                                   + RandGenerator.randomStrChaLen(i) + "',"
                                   + RandGenerator.randomInt(100000, 1000000)
                                   + ")";
			if(verbose) 
				if(i%100==0) System.out.println(sql);
			stmt.executeUpdate(sql);
		}
	}

	private void seedStockItem() throws SQLException
	{

            System.out.println("Populating StockItem with "+ITEM+" records ...");
            for (int i=1; i<=ITEM; i++)
            {
				String code = "C" + i;

				String sql = "INSERT INTO StockItem VALUES("
                                   + i + ",'"
                                   + RandGenerator.randomStrFixLen(5) + "','"
                                   + code + "',"
                                   + RandGenerator.randomFloat(10.0F, 20.0F) + ","
                                   + RandGenerator.randomFloat(20.0F, 30.0F) + ","
                                   + RandGenerator.randomFloat(10.0F, 20.0F)
                                   + ")";
				if(verbose) 
					if(i%100==0) System.out.println(sql);
				stmt.executeUpdate(sql);
            }
	}

	private void seedStockHolding() throws SQLException
	{
		int stock[] = new int[HOLDING+1];
		int stockID, amount;
		boolean not_hold;
		String sql = "INSERT INTO StockHolding VALUES(?,?,?)";

            	System.out.println("Populating StockHolding with "+HOLDING+" records per account ...");
            	PreparedStatement pstmt = con.prepareStatement(sql);

            	for (int i=1; i<=ACCT; i++)
            	{
					for (int k=1; k<=HOLDING; k++)	stock[k] = -1;
                	
                	for (int j=1; j<=HOLDING; )
                	{
						stockID = RandGenerator.randomInt(1, ITEM);
						not_hold = true;

						for(int k=1; k<=j; k++)
						{
							if(stockID==stock[k])
							{
								not_hold = false;
								break;
							}
						}

						if(not_hold)
						{
							stock[j++] = stockID;
							amount  = RandGenerator.randomInt(1000,10000);
							pstmt.setInt(1, i);
	                    	pstmt.setInt(2, stockID);
        	            	pstmt.setInt(3, amount);

							if(verbose) 
							if(i%100==0) System.out.println(sql + " with " + i + ", " + stockID + ", " + amount);
							pstmt.executeUpdate();
						}
					}
            	}
		}

  	public static void main(String[] args) throws Exception 
  	{
		if (args.length<3) 
		{
			System.out.println("Usage: java stockonline.datatbase.oracle.seed dbURL userName passWord");
			System.out.println("where: \n\t dbURL = jdbc:oracle:thin:@hostname:1521:SID");
			return;
		}
		
		new Seed(args);
  	}

	public void init()
	{
		System.out.println("init() called");

		try 
		{
			System.out.println("To register the driver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("To register the driver.......................OK");
		} 
		catch(SQLException ex)
		{
			String msg = "Fail to register the JDBC Driver";
			System.out.println(msg);
		}

		try 
		{
			System.out.println("To get a JDBC connecion");
			con = DriverManager.getConnection(dbURL,userName, passWord);
			System.out.println("To get a JDBC connection..........................OK");
		} 
		catch(SQLException ex)
		{
			String msg = "Fail to get and set a JDBC connection";
			System.out.println(msg);
		}
	}

}
