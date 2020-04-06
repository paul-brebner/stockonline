# stockonline
CSIRO MTE Project Stockonline J2EE Benchmark (from early 2000s) - copy of https://download.forge.ow2.org/stock-online/

This is the complete archive (code and documentation) from a 
project I was involved with in the early 2000s (1999-2003) in CSIRO (Australia) led by Ian Gorton (https://www.linkedin.com/in/gortonator/), Anna Liu (https://www.linkedin.com/in/anna-liu-493781/?originalSubdomain=au) and Jeffrey Gosper (https://www.linkedin.com/in/jeffrey-gosper-34b590a/?originalSubdomain=au). It was called by various names including "Software Architecture and Component Technologies" (SACT) and "Advanced Software Architecture and Technologies" (AdSAT), and focussed on empirical evaluation of trending distributed and component-based technologies. This project focussed on the new Enterprise Java, a standards-based component-based technolgy for Java distributed systems. We wrote a J2EE benchmark, StockOnline (which was originally used for Corba benchmarking), for scalability and response time evaluation of different J2EE implmentations, and different alternative architectures (e.g. container managed vs. bean managed persistence). This was one of the novel features of J2EE, as it allowed different vendors to implement the J2EE specification, which potentially different extra features and tradeoffs. This products were also called "Application Servers" and enabled easy deployment of complete distributed applications to clusters of Application Servers. 

We did lots of benchmarking, looking at the impact of different JVMs, JVM tunning, architectural choices, etc. We published lots of papers and attended lots of conferences (academic and industry conferences, and went on road shows around the country to help educate people about the pros and cons of different J2EE solutions), and worked with vendors to fix bugs and improve results (e.g. we found bugs in the Sun JVM, and tried to work out if the Fujitsu Interstage AppServer was actually J2EE compliant given it's rather novel deployment pattern - every bean had to be deployed in a separate container!). 

The main output of the project was a detailed comparison of about 6 Application Servers which was published by CSIRO Publishing and Cutter Consortium.  This report was initially sold for lots of $$$, but we eventually made it free when the project closed up (2003) and it was downloaded over 1000 times: https://www.theserverside.com/discussions/thread/18630.html
A PDF copy of the final report is here: https://github.com/paul-brebner/stockonline/blob/master/J2EE21.pdf

We also made the headlines (https://www.computerworld.com/article/3470516/scientific-group-aims-at-independent-platform-evaluation.html) and generated lots of discussion, particularly given that at the time CSIRO for more known for studying Wombat Droppings than Middlware evaluations! E.g. https://www.theserverside.com/discussions/thread/8875.html
 
I was awarded an Australian Science Academy/French Embassy travel grant to work with Inria and ObjectWeb in France in 2003, but this was cancelled when the project was terminated, but we did make the code Open Source at https://download.forge.ow2.org/stock-online/.

We open sourced an ECPerf kit for JBoss: https://www.theserverside.com/discussions/thread/13635.html

I also had in-depth knowledge of other J2EE benchmarks including ECPerf, which evolved into the SPEC J2EE benchmarks.  I contributed to a couple of these: https://www.spec.org/jAppServer2001/press_release.html and https://www.spec.org/jAppServer2002/press_release.html

After this project ended I worked on a CSIRO Astronomy Cluster Computing/Grid project, and then worked for a year (2004) with Professor Wolfgang Emmerich (https://www.linkedin.com/in/wolfgangemmerich/?originalSubdomain=uk) at UCL in London on a very interesting Grid (OGSA) project, which suprisingly still has the original web page: http://sse.cs.ucl.ac.uk/UK-OGSA/

A couple of my papers from the MTE StockOnline project are:

Paul Brebner, Shuping Ran:
Entity Bean A, B, C's: Enterprise Java Beans Commit Options and Caching. Middleware 2001: 36-55

Shuping Ran, Paul Brebner, Ian Gorton:
The Rigorous Evaluation of Enterprise Java Bean Technology. ICOIN 2001: 93-104

Paul Brebner, Ben Logan:
Project JebX: A Java ebXML Experience. IPDPS 2003: 246
2001

Brebner, P. (Ed.), Evaluating J2EE Application Servers - Version 2.1, July 2002, CSIRO Publishing and Cutter Consortium.

Paul Brebner, Jeffrey Gosper:
How scalable is J2EE technology? ACM SIGSOFT Software Engineering Notes 28(3): 4 (2003)

Paul Brebner, Jeffrey Gosper:
J2EE infrastructure scalability and throughput estimation. SIGMETRICS Performance Evaluation Review 31(3): 30-36 (2003)

Ian Gorton, Anna Liu, Paul Brebner:
Rigorous Evaluation of COTS Middleware Technology. IEEE Computer 36(3): 50-55 (2003)
(We were the cover feature for this issue, see: https://i1.rgstatic.net/publication/2955854_Rigorous_evaluation_of_COTS_Middleware_technology/links/0046353b34f4771202000000/largepreview.png

Paul Brebner, Jeffrey Gosper:
The J2EE ECperf benchmark results: transient trophies or technology treasures? Concurrency - Practice and Experience 16(10): 1023-1036 (2004)

Paul Brebner, Emmanuel Cecchet, Julie Marguerite, Petr Tuma, Octavian Ciuhandu, Bruno Dufour, Lieven Eeckhout, Stéphane Frénot, Arvind S. Krishna, John Murphy, Clark Verbrugge:
Middleware benchmarking: approaches, results, experiences. Concurr. Comput. Pract. Exp. 17(15): 1799-1805 (2005)

For a bit more background see: http://acomputerscientistlearnsaws.blogspot.com/2017/04/who-am-i-what-did-computer-scientist-do_52.html




