/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.io.Serializable;

public class AverageCounter implements Serializable {

	private static final long serialVersionUID = 3507501664853383412L;

	private final String name;
	private final int depth;

	private long sum;
	private long min = Long.MAX_VALUE;
	private long max;
	private long count;

	public AverageCounter(String name, int depth) {
		this.name = name;
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public void touch(long value) {
		sum += value;
		count++;
		max = Math.max(max, value);
		min = Math.min(min, value);
	}

	public boolean log() {
		if (count > 0) {
			System.out.println(name + "(" + depth + ")" + " (avg:" + getAverage() + " sum:" + sum + " min:" + min + " max:" + max + " count:" + count + ")");
			return true;
		}
		return false;
	}

	public long getAverage() {
		return sum / count;
	}

}
