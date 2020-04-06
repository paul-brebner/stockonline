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

package stockonline.util;

import java.util.*;

public class TranDeck
{
	public static final int TRAN_SIZE = 43;	// Total number of transactions

	public static final int TRAN_BUY        = 1;
	public static final int TRAN_SELL       = 2;
	public static final int TRAN_QUERY_ID   = 3;
	public static final int TRAN_QUERY_CODE = 4;
	public static final int TRAN_GETHOLDING = 5;
	public static final int TRAN_CREATE     = 6;
	public static final int TRAN_UPDATE     = 7;

	int   maxAccountNo;	// Maximum account number that will be randomly created
	int   deckOrder[];
	int   deckCards[][];
	Stack buyStack;
	Stack sellStack;
	int   index;

	Random rand;	// Random generator

	public TranDeck()
	{
		// The initial database has at least 3000 subscribers
		this(3000);
	}

	public TranDeck(int maxAccountNo)
	{
		this.maxAccountNo = maxAccountNo;
		deckOrder = new int[TRAN_SIZE];
		deckCards = new int[TRAN_SIZE][2];
		buyStack  = new Stack();
		sellStack = new Stack();

		// Initialize the deck
		for (int i = 0; i < TRAN_SIZE - 2; i++) {
			if (0 <= i && i <= 2) {
				deckOrder[i] = TRAN_BUY;
				continue;
			}

			if (3 <= i && i <= 5) {
				deckOrder[i] = TRAN_SELL;
				continue;
			}

			if (6 <= i && i <= 20) {
				deckOrder[i] = TRAN_QUERY_ID;
				continue;
			}

			if (21 <= i && i <= 35) {
				deckOrder[i] = TRAN_QUERY_CODE;
				continue;
			}

			if (36 <= i && i <= 40) {
				deckOrder[i] = TRAN_GETHOLDING;
				continue;
			}
		}

		deckOrder[41] = TRAN_CREATE;
		deckOrder[42] = TRAN_UPDATE;

		rand = new Random(getCurrentThreadNum());
	}

	public void setRandomGen(Random rand)
	{
		this.rand = rand;
	}

	public TranInfo getNext()
	{
		TranInfo tranInfo = new TranInfo();

		tranInfo.tranType  = deckOrder[index];
		tranInfo.accountNo = deckCards[index][0];
		tranInfo.stockID   = deckCards[index][1];
		index++;

		return tranInfo;
	}

	public void shuffle()
	{
		// Shuffle deck
		random_shuffle(deckOrder);

		// Populate the deck
		for (int i = 0; i < TRAN_SIZE; i++) {
			deckCards[i][0] = Math.abs(rand.nextInt() % maxAccountNo) + 1;	// subaccno
			deckCards[i][1] = Math.abs(rand.nextInt() % 3000) + 1;			// stockid
		}

		// Scan the deck and match buys and sells
		for (int i = 0; i < TRAN_SIZE; i++) {
			if (deckOrder[i] == TRAN_BUY) {
				if (!sellStack.empty()) {
					int pos = ((Integer)sellStack.pop()).intValue();
					deckOrder[i]   = TRAN_SELL;
					deckOrder[pos] = TRAN_BUY;

					deckCards[pos][0] = deckCards[i][0];
					deckCards[pos][1] = deckCards[i][1];
				} else {
					buyStack.push(new Integer(i));
				}
			} else if (deckOrder[i] == TRAN_SELL) {
				if (!buyStack.empty()) {
					int pos = ((Integer)buyStack.pop()).intValue();
					deckCards[i][0] = deckCards[pos][0];
					deckCards[i][1] = deckCards[pos][1];
				} else {
					sellStack.push(new Integer(i));
				}
			}
		}

		index = 0;
	}

	// Get the current thread number.
	// Note:
	// (1) this method can only be used in a thread
	//     with an automatically generated name, that is,
	//     no thread name is specified in the contructors
	//     when it is created.
	// (2) if the thread name is not auto-generated,
	//     this method returns zero, hopefully.
	protected int getCurrentThreadNum()
	{
		// Automatically generated names are of the form "Thread-"+n,
		// where n is an integer.
		try {
			return Integer.parseInt(Thread.currentThread().getName().substring(7));
		}
		catch (Exception e) {
			return 0;
		}
	}

	/**
		This method evaluates
			swap(*(first + N), *(first + M))
		once for each N in the range [1, last - first),
		where M is a value from some uniform random distribution
		over the range [0, N).
	*/
	protected void random_shuffle(int deck[])
	{
		for (int n = 1; n < deck.length; n++) {
			int m = Math.abs(rand.nextInt() % n);
			// Swap
			int temp = deck[n];
			deck[n]  = deck[m];
			deck[m]  = temp;
		}
	}
}
