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


package stockonline.ejb.entity.cmp.account;

import java.sql.*;
import java.util.*;
import java.rmi.*;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import javax.rmi.*;

import stockonline.ejb.sql.SeqGenerator;
import stockonline.ejb.entity.interf.*;

/**
 * Container-Managed Persistent Entity Bean.
 * This Entity Bean represents a subscriber Account.
 */
public class AccountBean implements EntityBean
{
	final static boolean verbose 		= false;
	final static String BEAN_NAME 	= "cmpAccount";
	final static String RESOURCE_NAME 	= "java:comp/env/jdbc/StockDB";

	public int		sub_accno;
	public String	sub_name;
	public String	sub_address;
	public int		sub_credit;

	protected	EntityContext ctx;
	private transient boolean isDirty;				// "dirty" flag for WebLogic

	public AccountBean()
	{
		if (verbose) System.out.println(BEAN_NAME + " instantiated");
	}

	//----------------------------------------------------------------
	// Begin business methods
	//----------------------------------------------------------------

	/**
	 * Updates the credit of account.
	 */
	public void update(int amount)
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".update(" + amount + ") called.");

		sub_credit = amount;
       	setModified(true);
	}

	/**
	 * Deposits amount into account.
	 */
	public void deposit(int amount)
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".deposit(" + amount + ") called.");

		sub_credit += amount;
		setModified(true);
	}

	/**
	 * Withdraws amount from account.
	 * @throw AccountException thrown in amount < available credit
	 */
	public int withdraw(int amount) throws Exception
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".withdraw(" + amount + ") called.");

		if (amount > sub_credit) {
			throw new Exception("Your credit is " + sub_credit + "! You cannot withdraw " + amount + "!");
		}

		sub_credit -= amount;
		setModified(true);

		return sub_credit;
	}

	// Getter/setter methods on Entity Bean fields

	public int getCredit()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".getCredit() called.");

		return sub_credit;
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
			System.out.println(BEAN_NAME + ".setEntityContext called");

		this.ctx = ctx;
	}

	/**
	 * Disassociates this Bean instance with a
	 * particular context environment.
	 */
	public void unsetEntityContext()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".unsetEntityContext called");

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
	 * @return The primary key for this account
	 */
	public AccountPK ejbCreate(String sub_name, String sub_address, int sub_credit)
		throws CreateException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbCreate() called.");

		try
		{
			this.sub_accno   = SeqGenerator.getNextAccountID(RESOURCE_NAME);
			this.sub_name    = sub_name;
			this.sub_address = sub_address;
			this.sub_credit  = sub_credit;

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
	public void ejbPostCreate(String sub_name, String sub_address, int sub_credit)
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbPostCreate() called.");

		setModified(false);
	}

	/**
	 * This is the initialization method that corresponds to the
	 * create() method in the Home Interface.
	 *
	 * @return The primary key for this account
	 */
	public AccountPK ejbCreate(int sub_accno, String sub_name, String sub_address, int sub_credit)
		throws CreateException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbCreate() called.");

		try
		{
			this.sub_accno   = sub_accno;
			this.sub_name    = sub_name;
			this.sub_address = sub_address;
			this.sub_credit  = sub_credit;

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
	public void ejbPostCreate(int sub_accno, String sub_name, String sub_address, int sub_credit)
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbPostCreate() called.");

		setModified(false);
	}


	/**
	 * Removes the Entity Bean from the database.
	 * Corresponds to when client calls home.remove().
	 */
	public void ejbRemove() throws RemoteException
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

		if (!isModified()) return;
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
		if (!isModified()) {
			return;
		}

		setModified(false);
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

	//----------------------------------------------------------------
	// End EJB-Required methods.
	//----------------------------------------------------------------
}
