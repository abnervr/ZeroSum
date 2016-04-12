/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.data;

import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = 1548860888415278562L;

	private final short x, y;

	public Position(int x, int y) {
		this.x = (short)x;
		this.y = (short)y;
	}

	public short getX() {
		return x;
	}

	public short getY() {
		return y;
	}

	public boolean isValid() {
		return x % 2 != y % 2 && x >= 0 && y >= 0 && x < Board.BOARD_SIZE && y < Board.BOARD_SIZE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position)obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}
