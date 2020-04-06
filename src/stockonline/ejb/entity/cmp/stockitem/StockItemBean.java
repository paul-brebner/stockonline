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

package stockonline.ejb.entity.cmp.stockitem;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import javax.sql.*;

import java.rmi.*;
import java.sql.*;
import java.util.*;

import stockonline.util.*;
import stockonline.ejb.entity.interf.*;

/**
 * Bean-Managed Persistent Entity Bean.
 * This Entity Bean represents a stock item.
 */
public class StockItemBean implements EntityBean
{
	final static boolean 	verbose	= false;
	final static String	BEAN_NAME 	= "cmpStockItem";

	// Container-managed state fields
	public int    stock_id;
	public String name;
	public String code;
	public float  current_val;
	public float  high_val;
	public float  low_val;

	protected EntityContext 	ctx;
	private transient boolean 	isDirty;		// "dirty" flag

	public StockItemBean()
	{
		if (verbose) System.out.println(BEAN_NAME + " instantiated");
	}

	//----------------------------------------------------------------
	// Begin business methods
	//----------------------------------------------------------------

	/**
	 * Get current value of the stock item
	 */
	public float getCurrentValue()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".getCurrentValue() called.");

		return current_val;
	}

	/**
	 * Get values of the stock item, including current, high, low.
	 */
	public QueryResult getValues()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".getValues() called.");

		QueryResult result = new QueryResult();

		result.current_val = current_val;
		result.high_val    = high_val;
		result.low_val     = low_val;

		return result;
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

	public StockItemPK ejbCreate() throws CreateException
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbCreate() called.");

		return null;
	}

	public void ejbPostCreate()
	{
		if (verbose)
			System.out.println(BEAN_NAME + ".ejbPostCreate() called.");
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
			System.out.println(BEAN_NAME + ".ejbPassivate() called.");
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

		setModified(false);
	}

/*
	public StockItemPK ejbFindByCode(String stockCode)
		throws FinderException, RemoteException
	{
		if (verbose)
			System.out.println(BEAN_NAME + "FindByCode.ejb() called.");
		
		this.code = stockCode;
		setModified(false);

		return null;
	}
*/
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
