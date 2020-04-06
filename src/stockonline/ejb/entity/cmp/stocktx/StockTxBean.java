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
//

package stockonline.ejb.entity.cmp.stocktx;

import javax.naming.*;
import javax.rmi.*;
import javax.ejb.*;
import javax.sql.*;

import java.sql.*;
import java.util.*;
import java.rmi.*;

import stockonline.ejb.sql.SeqGenerator;
import stockonline.ejb.entity.interf.*;

/**
 * Bean-Managed Persistent Entity Bean.
 * This Entity Bean represents a StockTx.
 */
public class StockTxBean implements EntityBean
{
	final static boolean verbose = false;
	final static String BEAN_NAME = "cmpStockTx";
	final static String RESOURCE_NAME = "java:comp/env/jdbc/StockDB";

	// Container-managed state fields
	public int    trans_id;			// Primary key
	public String trans_type;
	public int    sub_accno;
	public int    stock_id;
	public int    amount;
	public float  price;
	public String trans_date;

	protected EntityContext ctx;
	private transient boolean isDirty;	// "dirty" flag
    	private java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("dd/MM/yy");

	public StockTxBean()
	{
		if (verbose) System.out.println(BEAN_NAME + " instantiated");
	}

	//----------------------------------------------------------------
	// Begin business methods
	//----------------------------------------------------------------
	// Getter/setter methods on Entity Bean fields

	public int getTxID()
	{
		return trans_id;
	}

	//----------------------------------------------------------------
	// End public business methods
	//----------------------------------------------------------------

	//----------------------------------------------------------------
	// Begin EJB-Required methods.  The methods below are called
	// by the Container, and never called by client code.
	//----------------------------------------------------------------

	/**
	 * Associates this Bean instance with a particular context.
	 * We can query the Bean properties which customize the Bean here.
	 */
	public void setEntityContext(EntityContext ctx)
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".setEntityContext() called");

		this.ctx = ctx;
	}

	/**
	 * Disassociates this Bean instance with a
	 * particular context environment.
	 */
	public void unsetEntityContext()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".unsetEntityContext() called");

		this.ctx = null;
	}

	/**
	 * Implementation can acquire needed resources.
	 */
	public void ejbActivate()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbActivate() called.");

		setModified(true);
	}

	/**
	 * Releases held resources for passivation.
	 */
	public void ejbPassivate()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbPassivate () called.");
	}

	/**
	 * This is the initialization method that corresponds to the
	 * create() method in the Home Interface.
	 *
	 * @return The primary key for this StockTx
	 */
	public StockTxPK ejbCreate(String trans_type, int sub_accno, int stock_id, int amount, float price)
		throws CreateException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbCreate() called.");

		try
		{
			this.trans_id   = SeqGenerator.getNextTxID(RESOURCE_NAME);
			this.trans_type = trans_type;
			this.sub_accno  = sub_accno;
			this.stock_id   = stock_id;
			this.amount     = amount;
			this.price      = price;
			this.trans_date = dateFormatter.format( new java.util.Date() );

			if(verbose) System.out.println("trans_date = " + trans_date);

			return null;
		}
		catch(Exception ex)
		{
			String msg = BEAN_NAME + ".ejbCreate(): Failed to get a sequence number, due to: " + ex.toString();

			System.err.println(msg);
			throw new CreateException(msg);
		}
	}

	/**
	 * Called after ejbCreate(). Now, the Bean can retrieve its EJBObject
	 * from its context, and pass it as a 'this' argument.
	 */
	public void ejbPostCreate(String trans_type, int sub_accno, int stock_id, int amount, float price)
	{
		setModified(false);
	}

	/**
	 * This is the initialization method that corresponds to the
	 * create() method in the Home Interface.
	 *
	 * @return The primary key for this StockTx
	 */
	public StockTxPK ejbCreate(int trans_id, String trans_type, int sub_accno, int stock_id, int amount, float price)
		throws CreateException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbCreate() called.");

		try
		{
			String currentDate = dateFormatter.format( new java.util.Date() );
			if(verbose) System.out.println(currentDate);

			this.trans_id   = trans_id;
			this.trans_type = trans_type;
			this.sub_accno  = sub_accno;
			this.stock_id   = stock_id;
			this.amount     = amount;
			this.price      = price;
			this.trans_date = currentDate;

			return null;
		}
		catch(Exception ex)
		{
			String msg = BEAN_NAME + ".ejbCreate(): Failed to get a sequence number, due to: " + ex.toString();

			System.err.println(msg);
			throw new CreateException(msg);
		}
	}

	/**
	 * Called after ejbCreate(). Now, the Bean can retrieve its EJBObject
	 * from its context, and pass it as a 'this' argument.
	 */
	public void ejbPostCreate(int trans_id, String trans_type, int sub_accno, int stock_id, int amount, float price)
	{
		setModified(false);
	}

	/**
	 * Removes the Entity Bean from the database.
	 * Corresponds to when client calls home.remove().
	 */
	public void ejbRemove()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbRemove() called.");
	}


	/**
	 * Updates the in-memory Entity Bean object
	 * to reflect the current value stored in the database.
	 */
	public void ejbLoad() throws RemoteException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbLoad() called.");

		setModified(false);
	}

	/**
	 * Updates the database to reflect the
	 * current values of this in-memory Entity Bean object representation.
	 */
	public void ejbStore() throws RemoteException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbStore() called.");

		// Cannot be modified, so do nothing!
	}

	/**
	 * Returns whether the EJBean has been modified or not.
	 *
	 * @return	boolean isDirty
	 */
	public boolean isModified()
	{
		if (verbose)
			System.out.println (BEAN_NAME + ".isModified(): isDirty = " + (isDirty ? "true" : "false"));

		return isDirty;
	}

	/**
	 * Sets the EJBean as modified.
	 *
	 * @param flag	boolean Flag
	 */
	public void setModified(boolean flag)
	{
		isDirty = flag;
	}
}
