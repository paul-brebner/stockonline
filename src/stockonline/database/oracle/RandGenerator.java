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


package stockonline.database.oracle;

public class RandGenerator
{
    private static java.util.Random _r = new java.util.Random();

    public static String _randomStrFixLen(java.util.Random r, int length)
    {
        String str = "";
        for (int i=0; i<length; i++)
	{
		str += (char)((int)'A' + Math.abs(r.nextInt())%25);
	}
        return str;
    }

    public static String _randomStrChaLen(java.util.Random r, int seed)
    {
        String str = "";

	r.setSeed(seed);
	int length = Math.abs(r.nextInt())%7 + 3;

        for (int i=0; i<length; i++)
	{
		str += (char)((int)'A'+Math.abs(r.nextInt())%25);
	}
        return str;
    }

    public static String randomStrFixLen(int length)
    {
        return _randomStrFixLen(_r, length);
    }

    public static String randomStrChaLen(int seed)
    {
        return _randomStrChaLen(_r, seed);
    }

    public static int randomInt(java.util.Random r, int a, int b)
    {
        return (Math.abs(r.nextInt()) % (b-a-1) + a);
    }

    /**
     * @return an integer between a (inclusive) and b (exclusive)
     */
    public static int randomInt(int a, int b)
    {
        return randomInt(_r, a, b);
    }

    public static float randomFloat(java.util.Random r, float a, float b)
    {
        return Math.abs(r.nextFloat())*(b-a-1.0F) + a;
    }

    /**
     * @return a float between a (inclusive) and b (exclusive)
     */
    public static float randomFloat(float a, float b)
    {
        return randomFloat(_r, a, b);
    }

    public static void main(String args[])
    {
            for(int i=0; i<10; i++)
                System.out.println(RandGenerator.randomStrFixLen(10));

            for(int i=0; i<10; i++)
                System.out.println(RandGenerator.randomStrChaLen(i));

            for(int i=0; i<10; i++)
                System.out.println(RandGenerator.randomInt(10, 20));

            for(int i=0; i<10; i++)
                System.out.println(RandGenerator.randomFloat(1.6F, 2.3F));
    }
}