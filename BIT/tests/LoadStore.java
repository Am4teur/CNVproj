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


import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class LoadStore {
	private static int loadcount = 0;
	private static int storecount = 0;
	private static int fieldloadcount = 0;
	private static int fieldstorecount = 0;

	private static ConcurrentHashMap<Long, Integer> loadcount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> storecount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> fieldloadcount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> fieldstorecount_map = new ConcurrentHashMap<>();


	public static void printUsage()
	{
		System.out.println("Syntax: java LoadStore in_path out_path");
		System.out.println("        in_path:  directory from which the class files are read");
		System.out.println("        out_path: directory to which the class files are written");
		System.exit(-1);
	}

	public static void doLoadStore(File in_dir, File out_dir)
	{
		String filelist[] = in_dir.list();

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);
				ci.addBefore("BIT/LoadStore", "initCounter", "");

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();

					for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode=instr.getOpcode();
						if (opcode == InstructionTable.getfield)
							instr.addBefore("BIT/LoadStore", "LSFieldCount", new Integer(0));
					}
				}
				ci.write(out_filename);
			}
		}
	}

	public static synchronized void printLoadStore(String s)
	{
		System.out.println("Load Store Summary:");
		System.out.println("Field load:    " + fieldloadcount);
		System.out.println("Field store:   " + fieldstorecount);
		System.out.println("Regular load:  " + loadcount);
		System.out.println("Regular store: " + storecount);
	}

	public static void initCounter(String s) {
		long thread_id =Thread.currentThread().getId();
		clear_LSCounter(thread_id);
	}


	public static void clear_LSCounter(long thread_id) {
		fieldloadcount_map.put(thread_id, 0);
		fieldstorecount_map.put(thread_id, 0);
		loadcount_map.put(thread_id, 0);
		storecount_map.put(thread_id, 0);
	}


	public static void LSFieldCount(int type)
	{
		long thread_id = Thread.currentThread().getId();
		if (type == 0)
			fieldloadcount_map.put(thread_id, fieldloadcount_map.get(thread_id) + 1);
//			fieldloadcount++;
		else
			fieldstorecount_map.put(thread_id, fieldstorecount_map.get(thread_id) + 1);
//			fieldstorecount++;
	}

	public static void LSCount(int type)
	{
		long thread_id = Thread.currentThread().getId();
		if (type == 0)
			loadcount_map.put(thread_id, loadcount_map.get(thread_id) + 1);
//			loadcount++;
		else
			storecount_map.put(thread_id, storecount_map.get(thread_id) + 1);
//			storecount++;
	}

	public static int getLSCount_fieldloadcount(long thread_id) {
		return fieldloadcount_map.get(thread_id);
	}

	public static int getLSCount_fieldstorecount(long thread_id) {
		return fieldstorecount_map.get(thread_id);
	}

	public static int getLSCount_loadcount(long thread_id) {
		return loadcount_map.get(thread_id);
	}

	public static int getLSCount_storecount(long thread_id) {
		return storecount_map.get(thread_id);
	}

	public static void main(String[] argv) {
		if (argv.length != 2) {
			printUsage();
		} else {
			try {
				File in_dir = new File(argv[0]);
				File out_dir = new File(argv[1]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doLoadStore(in_dir, out_dir);
				} else {
					printUsage();
				}
			} catch (NullPointerException e) {
				printUsage();
			}
		}

	}
}
