/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.data.checkers;

import java.io.Serializable;

import org.abner.zerosum.data.Piece;

public class CheckersPiece extends Piece implements Serializable {

	private static final long serialVersionUID = 7915864094881134607L;

	private boolean queen;

	public CheckersPiece(boolean black, boolean queen) {
		this(black);
		this.queen = queen;
	}

	public CheckersPiece(boolean black) {
		super(black);
	}

	public boolean isQueen() {
		return queen;
	}

	public void setQueen(boolean queen) {
		this.queen = queen;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckersPiece other = (CheckersPiece)obj;
		if (black != other.black)
			return false;
		if (queen != other.queen)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (black ? 1231 : 1237);
		result = prime * result + (queen ? 1231 : 1237);
		return result;
	}

	@Override
	public String toString() {
		return (isBlack() ? "B" : "R") + (isQueen() ? "Q" : "");
	}

	@Override
	public Piece clone() {
		return new CheckersPiece(black, queen);
	}
}
