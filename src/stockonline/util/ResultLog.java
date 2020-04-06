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
//		25/10/2001	Shiping	Two changes:
//						1. Add getTotal for TPS calculation
//						2. Remove all synchronized except for add()
//						   for reducing overheads.
//
//


package stockonline.util;

public class ResultLog
{
	long total = 0;
	int  count = 0;

	public void addSample(long time)
	{
		total += time;
		count++;
	}

	synchronized public void add(ResultLog rlog)
	{
		total += rlog.total;
		count += rlog.count;
	}

	public long getAverage()
	{
		return (count == 0) ? 0 : total/count;
	}

	public long getTotal()
	{
		return total;
	}

	public long getCount()
	{
		return count;
	}

	public static long getCurrentTime()
	{
		return System.currentTimeMillis();
	}
}
