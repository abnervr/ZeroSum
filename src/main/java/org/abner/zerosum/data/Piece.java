package org.abner.zerosum.data;

import java.io.Serializable;

public class Piece implements Serializable, Cloneable {

	private static final long serialVersionUID = 8202753527637557555L;

	protected final boolean black;

	public Piece(boolean black) {
		this.black = black;
	}

	/**
	 * Black pieces start with Y in 0, 1 and 2<br>
	 * White pieces start with Y in {@link Board#BOARD_SIZE} -1, -2 and -3
	 */
	public boolean isBlack() {
		return black;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Piece other = (Piece)obj;
		if (black != other.black)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (black ? 1231 : 1237);
		return result;
	}

	@Override
	public Piece clone() {
		return new Piece(black);
	}

}
