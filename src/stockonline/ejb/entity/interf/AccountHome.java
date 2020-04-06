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

package stockonline.ejb.entity.interf;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.sql.*;


/**
 * This is the home interface for Account.
 */
public interface AccountHome extends EJBHome
{
	/**
	 * This method creates the EJB Object.
	 *
	 * @param sub_name The name of the subscriber
	 * @param sub_address The address of the subscriber
	 * @param sub_credit The initial credit of the subscriber
	 *
	 * @return The newly created EJB Object.
	 */
	public Account create(String sub_name, String sub_address, int sub_credit) throws CreateException, RemoteException;

	/**
	 * This method creates the EJB Object.
	 *
	 * @param sub_accno The ID of the subscriber
	 * @param sub_name The name of the subscriber
	 * @param sub_address The address of the subscriber
	 * @param sub_credit The initial credit of the subscriber
	 *
	 * @return The newly created EJB Object.
	 */
	public Account create(int sub_accno, String sub_name, String sub_address, int sub_credit) throws CreateException, RemoteException;

	/**
	 * Finds an Account by its primary Key (Account ID)
	 */
	public Account findByPrimaryKey(AccountPK key) throws FinderException, RemoteException;
}
