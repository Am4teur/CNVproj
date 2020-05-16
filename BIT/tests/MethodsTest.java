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
package tests;


import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class MethodsTest {


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
				ci.addBefore("BIT/MethodsTest", "init_counters", "");


				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					routine.addBefore("BIT/MethodsTest", "dynMethodCount", new Integer(1));
					routine.addBefore("BIT/MethodsTest", "test", ci.getClassName());
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
		class_map.put(thread_id, new ConcurrentHashMap<String, Integer>());

	}


	public static int get_dyn_method_count(long thread_id) {
		return dyn_method_count_map.get(thread_id);
	}


	private static ConcurrentHashMap<Long, ConcurrentHashMap<String, Integer>> class_map = new ConcurrentHashMap<>();

	public static void test(String c) {
		long thread_id = Thread.currentThread().getId();
		ConcurrentHashMap<String, Integer> map = class_map.get(thread_id);
		if (!map.containsKey(c)) map.put(c, 0);
		map.put(c, map.get(c) + 1);
		class_map.put(thread_id, map);
	}

	public static String getTestString(long thread_id) {
		ConcurrentHashMap<String, Integer> map = class_map.get(thread_id);
		int total_methods = dyn_method_count_map.get(thread_id);
		StringBuilder res = new StringBuilder().append("Total Methods: ").append(total_methods).append("\n");
		for (String c: map.keySet()) {
			int c_met = map.get(c);
			float percent = (c_met / (float)total_methods) * 100;
			res.append(c).append(": ").append(c_met).append(" - ").append(percent).append("%\n");
		}
		return res.toString();
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
