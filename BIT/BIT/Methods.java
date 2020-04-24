//
// StatisticsTool.java
//
// This program measures and instruments to obtain different statistics
// about Java programs.
//
// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).
// ALL RIGHTS RESERVED.
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for non-commercial purposes is hereby granted provided 
// that this copyright notice appears in all copies.
// 
// This software is provided "as is".  The licensor makes no warrenties, either
// expressed or implied, about its correctness or performance.  The licensor
// shall not be liable for any damages suffered as a result of using
// and modifying this software.
package BIT;


import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Methods {


	private static ConcurrentHashMap<Long, Integer> dyn_method_count_map = new ConcurrentHashMap<>();


	public static void printUsage()
	{
		System.out.println("Syntax: java StatisticsTool -stat_type in_path [out_path]");
		System.out.println("        in_path:  directory from which the class files are read");
		System.out.println("        out_path: directory to which the class files are written");
		System.exit(-1);
	}

	public static void doDynamic(File in_dir, File out_dir)
	{
		String filelist[] = in_dir.list();

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);
				ci.addBefore("BIT/Methods", "init_counters", "");


				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					routine.addBefore("BIT/Methods", "dynMethodCount", new Integer(1));
				}
				ci.write(out_filename);
			}
		}
	}

	public static void init_counters(String s) {
		long thread_id = Thread.currentThread().getId();
		clear_dyn_counters(thread_id);
	}

	public static void clear_dyn_counters(long thread_id) {
		dyn_method_count_map.put(thread_id, 0);
	}


	public static int get_dyn_method_count(long thread_id) {
		return dyn_method_count_map.get(thread_id);
	}


	public static void dynMethodCount(int incr) {
		long thread_id = Thread.currentThread().getId();
		dyn_method_count_map.put(thread_id, dyn_method_count_map.get(thread_id) + incr);
	}

	public static void main(String[] argv)
	{
		if (argv.length != 2) {
			printUsage();
		}
		else{
			try {
				File in_dir = new File(argv[0]);
				File out_dir = new File(argv[1]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doDynamic(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}
	}
}
