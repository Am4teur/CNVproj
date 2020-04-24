	/* ICount.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */

package tests;

import BIT.highBIT.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


    public class ICount {
    private static PrintStream out = null;
    private static ConcurrentHashMap<Long, Long> i_count_map = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, Long> b_count_map = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, Long> m_count_map = new ConcurrentHashMap<>();

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();
        
        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);
				
                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
					routine.addBefore("BIT/ICount", "mcount", new Integer(1));
                    
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("BIT/ICount", "count", new Integer(bb.size()));
                    }
                }
                ci.addAfter("BIT/ICount", "printICount", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }
    
    public static synchronized void printICount(String foo) {
        System.out.println("printICount disabled!");
//        System.out.println(i_count + " instructions in " + b_count + " basic blocks were executed in " + m_count + " methods.");
    }
    

    public static void count(int incr) {
        long thread_id = Thread.currentThread().getId();
        i_count_map.put(thread_id, i_count_map.get(thread_id) + incr);
        b_count_map.put(thread_id, b_count_map.get(thread_id) + 1);

//        i_count += incr;
//        b_count++;
    }

    public static void mcount(int incr) {
        long thread_id = Thread.currentThread().getId();
        m_count_map.put(thread_id, m_count_map.get(thread_id) + 1);

//        m_count++;
    }


    public static void clearCounters(long thread_id) {
//        long thread_id = Thread.currentThread().getId();
        i_count_map.put(thread_id, 0L);
        b_count_map.put(thread_id, 0L);
        m_count_map.put(thread_id, 0L);
    }

    public static long getBCount(long thread_id) {
        return b_count_map.get(thread_id);
    }

    public static long getICount(long thread_id) {
        return i_count_map.get(thread_id);
    }

    public static long getMCount(long thread_id) {
		return m_count_map.get(thread_id);
    }


}

