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


import BIT.StatisticsBranch;
import BIT.highBIT.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsTool
{
	private static int dyn_method_count = 0;
	private static int dyn_bb_count = 0;
	private static int dyn_instr_count = 0;

	private static ConcurrentHashMap<Long, Integer> dyn_method_count_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> dyn_bb_count_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> dyn_instr_count_map = new ConcurrentHashMap<>();

	private static int newcount = 0;
	private static int newarraycount = 0;
	private static int anewarraycount = 0;
	private static int multianewarraycount = 0;

	private static ConcurrentHashMap<Long, Integer> newcount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> newarraycount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> anewarraycount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> multianewarraycount_map = new ConcurrentHashMap<>();


	private static int loadcount = 0;
	private static int storecount = 0;
	private static int fieldloadcount = 0;
	private static int fieldstorecount = 0;

	private static ConcurrentHashMap<Long, Integer> loadcount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> storecount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> fieldloadcount_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> fieldstorecount_map = new ConcurrentHashMap<>();

	private static StatisticsBranch[] branch_info;
	private static int branch_number;
	private static int branch_pc;
	private static String branch_class_name;
	private static String branch_method_name;

	private static ConcurrentHashMap<Long, Integer> taken_count_map = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Integer> not_taken_count_map = new ConcurrentHashMap<>();

	public static void printUsage()
	{
		System.out.println("Syntax: java StatisticsTool -stat_type in_path [out_path]");
		System.out.println("        where stat_type can be:");
		System.out.println("        static:     static properties");
		System.out.println("        dynamic:    dynamic properties");
		System.out.println("        alloc:      memory allocation instructions");
		System.out.println("        load_store: loads and stores (both field and regular)");
		System.out.println("        branch:     gathers branch outcome statistics");
		System.out.println();
		System.out.println("        in_path:  directory from which the class files are read");
		System.out.println("        out_path: directory to which the class files are written");
		System.out.println("        Both in_path and out_path are required unless stat_type is static");
		System.out.println("        in which case only in_path is required");
		System.exit(-1);
	}

	public static void doStatic(File in_dir)
	{
		String filelist[] = in_dir.list();
		int method_count = 0;
		int bb_count = 0;
		int instr_count = 0;
		int class_count = 0;

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				class_count++;
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);
				Vector routines = ci.getRoutines();
				method_count += routines.size();

				for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					BasicBlockArray bba = routine.getBasicBlocks();
					bb_count += bba.size();
					InstructionArray ia = routine.getInstructionArray();
					instr_count += ia.size();
				}
			}
		}

		System.out.println("Static information summary:");
		System.out.println("Number of class files:  " + class_count);
		System.out.println("Number of methods:      " + method_count);
		System.out.println("Number of basic blocks: " + bb_count);
		System.out.println("Number of instructions: " + instr_count);

		if (class_count == 0 || method_count == 0) {
			return;
		}

		float instr_per_bb = (float) instr_count / (float) bb_count;
		float instr_per_method = (float) instr_count / (float) method_count;
		float instr_per_class = (float) instr_count / (float) class_count;
		float bb_per_method = (float) bb_count / (float) method_count;
		float bb_per_class = (float) bb_count / (float) class_count;
		float method_per_class = (float) method_count / (float) class_count;

		System.out.println("Average number of instructions per basic block: " + instr_per_bb);
		System.out.println("Average number of instructions per method:      " + instr_per_method);
		System.out.println("Average number of instructions per class:       " + instr_per_class);
		System.out.println("Average number of basic blocks per method:      " + bb_per_method);
		System.out.println("Average number of basic blocks per class:       " + bb_per_class);
		System.out.println("Average number of methods per class:            " + method_per_class);
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
				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					routine.addBefore("BIT/StatisticsTool", "dynMethodCount", new Integer(1));

					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
						BasicBlock bb = (BasicBlock) b.nextElement();
						bb.addBefore("BIT/StatisticsTool", "dynInstrCount", new Integer(bb.size()));
					}
				}
				ci.addAfter("BIT/StatisticsTool", "printDynamic", "null");
				ci.write(out_filename);
			}
		}
	}

	public static synchronized void printDynamic(String foo)
	{
		System.out.println("Dynamic information summary:");
		System.out.println("Number of methods:      " + dyn_method_count);
		System.out.println("Number of basic blocks: " + dyn_bb_count);
		System.out.println("Number of instructions: " + dyn_instr_count);

		if (dyn_method_count == 0) {
			return;
		}

		float instr_per_bb = (float) dyn_instr_count / (float) dyn_bb_count;
		float instr_per_method = (float) dyn_instr_count / (float) dyn_method_count;
		float bb_per_method = (float) dyn_bb_count / (float) dyn_method_count;

		System.out.println("Average number of instructions per basic block: " + instr_per_bb);
		System.out.println("Average number of instructions per method:      " + instr_per_method);
		System.out.println("Average number of basic blocks per method:      " + bb_per_method);
	}

	public static void clear_dyn_counters(long thread_id) {
		dyn_instr_count_map.put(thread_id, 0);
		dyn_bb_count_map.put(thread_id, 0);
		dyn_method_count_map.put(thread_id, 0);
	}


	public static int get_dyn_instr_count(long thread_id) {
		return dyn_instr_count_map.get(thread_id);
//		return dyn_instr_count;
	}

	public static int get_dyn_method_count(long thread_id) {
		return dyn_method_count_map.get(thread_id);
//		return dyn_method_count;
	}

	public static int get_dyn_bb_count(long thread_id) {
		return dyn_bb_count_map.get(thread_id);
//		return dyn_bb_count;
	}

	public static void dynInstrCount(int incr) {
		long thread_id = Thread.currentThread().getId();
		dyn_instr_count_map.put(thread_id, dyn_instr_count_map.get(thread_id) + incr);
		dyn_bb_count_map.put(thread_id,dyn_bb_count_map.get(thread_id) + 1);

//		dyn_instr_count += incr;
//		dyn_bb_count++;
	}

	public static void dynMethodCount(int incr) {
		long thread_id = Thread.currentThread().getId();
		dyn_method_count_map.put(thread_id, dyn_method_count_map.get(thread_id) + incr);

//		dyn_method_count++;
	}

	public static void doAlloc(File in_dir, File out_dir)
	{
		String filelist[] = in_dir.list();

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					InstructionArray instructions = routine.getInstructionArray();

					for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode=instr.getOpcode();
						if ((opcode==InstructionTable.NEW) ||
								(opcode==InstructionTable.newarray) ||
								(opcode==InstructionTable.anewarray) ||
								(opcode==InstructionTable.multianewarray)) {
							instr.addBefore("BIT/StatisticsTool", "allocCount", new Integer(opcode));
						}
					}
				}
				ci.addAfter("BIT/StatisticsTool", "printAlloc", "null");
				ci.write(out_filename);
			}
		}
	}

	public static synchronized void printAlloc(String s)
	{
		System.out.println("Allocations summary:");
		System.out.println("new:            " + newcount);
		System.out.println("newarray:       " + newarraycount);
		System.out.println("anewarray:      " + anewarraycount);
		System.out.println("multianewarray: " + multianewarraycount);
	}

	public static void clearAllocCounter(long thread_id) {
		System.out.println("entra");
		newcount_map.put(thread_id, 0);
		newarraycount_map.put(thread_id, 0);
		anewarraycount_map.put(thread_id, 0);
		multianewarraycount_map.put(thread_id, 0);
		System.out.println("sai");
	}

	public static void allocCount(int type) {
		long thread_id = Thread.currentThread().getId();
		switch(type) {
			case InstructionTable.NEW:
				newcount_map.put(thread_id, newcount_map.get(thread_id) + 1);
//				newcount++;
				break;
			case InstructionTable.newarray:
				newarraycount_map.put(thread_id, newarraycount_map.get(thread_id) + 1);
//				newarraycount++;
				break;
			case InstructionTable.anewarray:
				anewarraycount_map.put(thread_id, anewarraycount_map.get(thread_id) + 1);
//				anewarraycount++;
				break;
			case InstructionTable.multianewarray:
				multianewarraycount_map.put(thread_id, multianewarraycount_map.get(thread_id) + 1);
//				multianewarraycount++;
				break;
		}
	}

	public static int getAllocCount_newcount(long thread_id) {
		return newcount_map.get(thread_id);
	}

	public static int getAllocCount_newarraycount(long thread_id) {
		return newarraycount_map.get(thread_id);
	}

	public static int getAllocCount_anewarraycount(long thread_id) {
		return anewarraycount_map.get(thread_id);
	}

	public static int getAllocCount_multianewarraycount(long thread_id) {
		return multianewarraycount_map.get(thread_id);
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

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();

					for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode=instr.getOpcode();
						if (opcode == InstructionTable.getfield)
							instr.addBefore("BIT/StatisticsTool", "LSFieldCount", new Integer(0));
						else if (opcode == InstructionTable.putfield)
							instr.addBefore("BIT/StatisticsTool", "LSFieldCount", new Integer(1));
						else {
							short instr_type = InstructionTable.InstructionTypeTable[opcode];
							if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
								instr.addBefore("BIT/StatisticsTool", "LSCount", new Integer(0));
							}
							else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
								instr.addBefore("BIT/StatisticsTool", "LSCount", new Integer(1));
							}
						}
					}
				}
				ci.addAfter("BIT/StatisticsTool", "printLoadStore", "null");
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


	public static void doBranch(File in_dir, File out_dir)
	{
		String filelist[] = in_dir.list();
		int k = 0;
		int total = 0;

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					InstructionArray instructions = routine.getInstructionArray();
					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
						BasicBlock bb = (BasicBlock) b.nextElement();
						Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
						short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
						if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
							total++;
						}
					}
				}
			}
		}

		for (int i = 0; i < filelist.length; i++) {
			String filename = filelist[i];
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					routine.addBefore("BIT/StatisticsTool", "setBranchMethodName", routine.getMethodName());
					InstructionArray instructions = routine.getInstructionArray();
					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
						BasicBlock bb = (BasicBlock) b.nextElement();
						Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
						short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
						if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
							instr.addBefore("BIT/StatisticsTool", "setBranchPC", new Integer(instr.getOffset()));
							instr.addBefore("BIT/StatisticsTool", "updateBranchNumber", new Integer(k));
							instr.addBefore("BIT/StatisticsTool", "updateBranchOutcome", "BranchOutcome");
							k++;
						}
					}
				}
				ci.addBefore("BIT/StatisticsTool", "setBranchClassName", ci.getClassName());
				ci.addBefore("BIT/StatisticsTool", "branchInit", new Integer(total));
				ci.addAfter("BIT/StatisticsTool", "printBranch", "null");
				ci.write(out_filename);
			}
		}
	}

	public static synchronized void setBranchClassName(String name)
	{
		branch_class_name = name;
	}

	public static synchronized void setBranchMethodName(String name)
	{
		branch_method_name = name;
	}

	public static synchronized void setBranchPC(int pc)
	{
		branch_pc = pc;
	}

	public static synchronized void branchInit(int n)
	{
		if (branch_info == null) {
			branch_info = new StatisticsBranch[n];
		}
	}

	public static synchronized void updateBranchNumber(int n)
	{
		branch_number = n;

		if (branch_info[branch_number] == null) {
			branch_info[branch_number] = new StatisticsBranch(branch_class_name, branch_method_name, branch_pc);
		}
	}


	public static void clearBranchCounter(long thread_id) {
		taken_count_map.put(thread_id, 0);
		not_taken_count_map.put(thread_id, 0);
	}
	public static int getTakenCounter(long thread_id) {
		return taken_count_map.get(thread_id);
	}
	public static int getNotTakenCounter(long thread_id) {
		return not_taken_count_map.get(thread_id);
	}

	public static void updateBranchOutcome(int br_outcome)
	{
		long thread_id = Thread.currentThread().getId();

		if (br_outcome == 0) {
			taken_count_map.put(thread_id, taken_count_map.get(thread_id) + 1);
			branch_info[branch_number].incrNotTaken();
		}
		else {
			not_taken_count_map.put(thread_id, not_taken_count_map.get(thread_id) + 1);
			branch_info[branch_number].incrTaken();
		}
	}

	public static synchronized void printBranch(String foo)
	{
		System.out.println("Branch summary:");
		System.out.println("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");


		for (int i = 0; i < branch_info.length; i++) {
			if (branch_info[i] != null) {
				branch_info[i].print();
			}
		}
	}


	public static void main(String argv[])
	{
		if (argv.length < 2 || !argv[0].startsWith("-")) {
			printUsage();
		}

		if (argv[0].equals("-static")) {
			if (argv.length != 2) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);

				if (in_dir.isDirectory()) {
					doStatic(in_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}

		else if (argv[0].equals("-dynamic")) {
			if (argv.length != 3) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

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

		else if (argv[0].equals("-alloc")) {
			if (argv.length != 3) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doAlloc(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}

		else if (argv[0].equals("-load_store")) {
			if (argv.length != 3) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doLoadStore(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}

		else if (argv[0].equals("-branch")) {
			if (argv.length != 3) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doBranch(in_dir, out_dir);
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
