/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Logger {

	//Se aumentar não diminua nunca mais...
	static final int MAX_DEPTH = 10;

	private static Map<String, Logger> loggers = new LinkedHashMap<String, Logger>();

	static {
		for (int i = 0; i <= 7; i++) {
			String modifier = Integer.toBinaryString(i);
			while (modifier.length() < 3)
				modifier = "0" + modifier;
			loggers.put(modifier, new Logger());
		}
		for (String modifier: loggers.keySet())
			try {
				load(modifier);
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}
	private Map<Integer, AverageCounter> nodesCounters = new HashMap<Integer, AverageCounter>();
	private Map<Integer, AverageCounter> timeCounters = new HashMap<Integer, AverageCounter>();

	private static void load(String modifiers) throws Exception {
		File file = new File("./log" + modifiers + ".dat");
		loggers.get(modifiers).nodesCounters.clear();
		loggers.get(modifiers).timeCounters.clear();
		if (file.exists()) {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			for (int i = 1; i <= MAX_DEPTH; i++) {
				Object object = input.readObject();
				if (object instanceof AverageCounter) {
					AverageCounter counter = (AverageCounter)object;
					loggers.get(modifiers).nodesCounters.put(counter.getDepth(), counter);
				}
				object = input.readObject();
				if (object instanceof AverageCounter) {
					AverageCounter counter = (AverageCounter)object;
					loggers.get(modifiers).timeCounters.put(counter.getDepth(), counter);
				}
			}
			input.close();
		} else {
			for (int i = 1; i <= MAX_DEPTH; i++) {
				loggers.get(modifiers).nodesCounters.put(i, new AverageCounter("Nós", i));
				loggers.get(modifiers).timeCounters.put(i, new AverageCounter("Tempo", i));
			}
		}
	}

	public static void save(String modifiers) throws IOException {
		File file = new File("./log" + modifiers + ".log");
		if (!file.exists()) {
			file.createNewFile();
		}
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
		for (int i = 1; i <= MAX_DEPTH; i++) {
			output.writeObject(loggers.get(modifiers).nodesCounters.get(i));
			output.writeObject(loggers.get(modifiers).timeCounters.get(i));
		}
		output.flush();
		output.close();
	}

	public static void logStatistic() {
		for (String modifier: loggers.keySet())
			logStatistic(modifier);
	}

	public static void logStatistic(String modifiers) {
		try {
			System.out.println(modifiers);
			for (int i = 1; i <= MAX_DEPTH; i++)
				if (loggers.get(modifiers).nodesCounters.get(i).log() | loggers.get(modifiers).timeCounters.get(i).log())
					System.out.println();
			save(modifiers);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Map<Integer, AverageCounter> getTimeCounters(String modifiers) {
		return loggers.get(modifiers).timeCounters;
	}

	public static Map<Integer, AverageCounter> getNodeCounters(String modifiers) {
		return loggers.get(modifiers).nodesCounters;
	}

	public static void main(String[] args) {
		System.out.println("Nodes");
		System.out.print("Depth");
		for (String modifier: loggers.keySet())
			System.out.print(";" + modifier);
		System.out.println();
		for (int depth = 1; depth <= 6; depth++) {
			String line = String.valueOf(depth);
			for (String modifier: loggers.keySet())
				line += ";" + String.valueOf(loggers.get(modifier).nodesCounters.get(depth).getAverage());
			System.out.println(line);
		}
		System.out.println("Time");
		System.out.print("Depth");
		for (String modifier: loggers.keySet())
			System.out.print(";" + modifier);
		System.out.println();
		for (int depth = 1; depth <= 6; depth++) {
			String line = String.valueOf(depth);
			for (String modifier: loggers.keySet())
				line += ";" + String.valueOf(loggers.get(modifier).timeCounters.get(depth).getAverage());
			System.out.println(line);
		}
	}
}
