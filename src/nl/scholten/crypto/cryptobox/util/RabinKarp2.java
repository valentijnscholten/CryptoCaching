package nl.scholten.crypto.cryptobox.util;

// this code implements the Rabin-Karp algorithm for finding a pattern P
// within a text T. It generates random text and pattern strings of the
// lengths given by the first two command line parameters.
//
// 1. Save this file as RabinKarp.java
// 2. Compile it with javac RabinKarp.java
// 3. Run it with java RabinKarp 10000000 20 (or n and m of your choice)

import java.util.Calendar;
import java.util.Random;

public class RabinKarp2
{
  public static byte[] T, P;
  public static int n, m;

  public static void main(String[] args)
  {
    n = Integer.parseInt(args[0]);      // text length
    m = Integer.parseInt(args[1]);      // pattern length
    int d = 10;                         // alphabet size
    System.out.print("Generating random text and pattern ... ");

    T = generateString(n, d);
    P = generateString(m, d);
    for (int i=0; i<m; i++)             // making sure that the pattern
      T[n-2*m+i] = P[i];                // occurs at the tail of T
    System.out.print("done \nP = \"");
    for (int i=0; i<m; i++)
      System.out.print((char)(P[i] +48));
    System.out.println("\"\n");
   
    // applying the brut force text search algorithm
    System.out.println("Running the brut force method ...");
    boolean match = true; 
    for (int s=0; s<=n-m; s++)
    {
      match = checkMatch(s);
      if (match)
      {
        System.out.println("Pattern occurs with shift " + s + "\n");
        break;
      }
    }
    if (! match)
      System.out.println("Pattern not found");

    // the Rabin-Karp algorithm
    System.out.println("Running the Rabin-Karp method ...");
    int q = 199999991;                  // compare  10q=1,999,999,910 
    System.out.println("q = " + q);     // with MAX_INT=2,147,483,647)

    int h = 1;
    for (int i=0; i<m-1; i++)           // computing h = d^{m-1} mod q
      h = (h*d) % q;                    // (d=10)
    System.out.println("h = " + h);

    int p = 0;                          // value of the patterm string         
    int t = 0;                          // value of the current portion of text
    for (int i=0; i<m; i++)             // compute them
    {
      p = (d*p + P[i]) % q;
      t = (d*t + T[i]) % q;
    }

    for (int s=0; s<=n-m; s++)          // main loop over all shifts
    {
//      System.out.print("trying " + s + ": ");
//      System.out.println("p=" + p + " t=" + t);
      if (p == t)                       // check for spurious hit
      {
        System.out.println("Spurious hit for s=" + s);
        if (checkMatch(s))              // compare the string at the hit pos.
        {
          System.out.println("Pattern occurs with shift " + s);
          break;
        }
      }

      if (s < n-m)                      // update the value of t
      {
        t = (d*(t - ((T[s]*h) % q)) + T[s+m]) % q;       
        if (t < 0)  
          t += q; 
      }
    }        
  }

  public static byte[] generateString(int n, int d)
  {
    Calendar cal = Calendar.getInstance();    // set up the random numbers
    long seed = cal.getTimeInMillis();        // generator
    Random generator = new Random(seed);
    byte[] arr = new byte[n];                 // set up random array of bytes
    for (int i=0; i<n; i++)
      arr[i] = (byte) generator.nextInt(d);
    return(arr);
  }

  // this function checks the pattern string P and text T at shift s for
  // a match
  public static boolean checkMatch(int s)
  {
    boolean match = true;
    for (int i=0; i<m; i++)
    if (T[s+i] != P[i])
    {
      match = false;
//      break;       // this line is taken off to model "real world" data
    }
    return(match);
  }
}
