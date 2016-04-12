/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.abner.zerosum.data.checkers.CheckersPiece;

public class Move implements Comparable<Move>, Serializable, Cloneable {

	private static final long serialVersionUID = -3125793236441694661L;

	private final Piece piece;
	private final Position startPosition;

	private Position position;
	private List<Position> positions;

	private List<Position> capturedPositions;
	private List<Piece> capturedPieces;

	private boolean queenMove = false;

	public Move(Piece piece, Position startPosition, Position position) {
		this.piece = piece.clone();
		this.startPosition = startPosition;
		setPosition(position);
	}

	public Move(Piece piece, Move parentMove, Position position) {
		this.piece = piece.clone();
		this.startPosition = parentMove.getStartPosition();
		for (Position capturedPiece: parentMove.getCapturedPositions()) {
            addCapturedPosition(capturedPiece);
        }
		if (parentMove.getPositions() != null) {
            for (Position oldPosition: parentMove.getPositions()) {
                addPosition(oldPosition);
            }
        }
		setPosition(position);
	}

	public Piece getPiece() {
		return piece;
	}

	public Position getStartPosition() {
		return startPosition;
	}

	public Position getPosition() {
		return position;
	}

	public boolean isQueenMove() {
		return queenMove;
	}

	public void setPosition(Position position) {
		if (this.position != null) {
            addPosition(this.position);
        }
		this.position = position;
		if (piece instanceof CheckersPiece && !((CheckersPiece)piece).isQueen()) {
			if (piece.isBlack() && position.getY() == Board.BOARD_SIZE - 1) {
                queenMove = true;
            }
			if (!piece.isBlack() && position.getY() == 0) {
                queenMove = true;
            }
		}
	}

	public void addCapturedPosition(Position position) {
		if (capturedPositions == null) {
            capturedPositions = new ArrayList<Position>();
        }
		capturedPositions.add(position);
	}

	public boolean containsCapturedPosition(Position position) {
		return capturedPositions != null && capturedPositions.contains(position);
	}

	public List<Position> getCapturedPositions() {
		return capturedPositions;
	}

	public void addCapturedPiece(Piece remove) {
		if (capturedPieces == null) {
            capturedPieces = new ArrayList<Piece>();
        }
		capturedPieces.add(remove);
	}

	public List<Piece> getCapturedPieces() {
		return capturedPieces;
	}

	public void addPosition(Position oldPosition) {
		if (positions == null) {
            positions = new ArrayList<Position>();
        }
		positions.add(oldPosition);
	}

	public List<Position> getPositions() {
		return positions;
	}

	public int getCaptured() {
		if (capturedPositions == null) {
            return 0;
        }
		return capturedPositions.size();
	}
	
	public int compareTo(Move o) {
		if (capturedPositions == null && o.capturedPositions == null) {
			if (isQueenMove() && o.isQueenMove()) {
                return 0;
            } else if (isQueenMove()) {
                return -1;
            } else {
                return 1;
            }
		}
		if (capturedPositions == null) {
            return 1;
        }
		if (o.capturedPositions == null) {
            return -1;
        }
		return o.capturedPositions.size() - capturedPositions.size();
	}

	@Override
	public String toString() {
		return startPosition.toString() + " -> " + position.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capturedPieces == null) ? 0 : capturedPieces.hashCode());
		result = prime * result + ((capturedPositions == null) ? 0 : capturedPositions.hashCode());
		result = prime * result + ((piece == null) ? 0 : piece.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((positions == null) ? 0 : positions.hashCode());
		result = prime * result + (queenMove ? 1231 : 1237);
		result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		Move other = (Move)obj;
		if (capturedPieces == null) {
			if (other.capturedPieces != null) {
                return false;
            }
		} else if (!capturedPieces.equals(other.capturedPieces)) {
            return false;
        }
		if (capturedPositions == null) {
			if (other.capturedPositions != null) {
                return false;
            }
		} else if (!capturedPositions.equals(other.capturedPositions)) {
            return false;
        }
		if (piece == null) {
			if (other.piece != null) {
                return false;
            }
		} else if (!piece.equals(other.piece)) {
            return false;
        }
		if (position == null) {
			if (other.position != null) {
                return false;
            }
		} else if (!position.equals(other.position)) {
            return false;
        }
		if (positions == null) {
			if (other.positions != null) {
                return false;
            }
		} else if (!positions.equals(other.positions)) {
            return false;
        }
		if (queenMove != other.queenMove) {
            return false;
        }
		if (startPosition == null) {
			if (other.startPosition != null) {
                return false;
            }
		} else if (!startPosition.equals(other.startPosition)) {
            return false;
        }
		return true;
	}

	@Override
	public Move clone() {
		Move move = new Move(piece, startPosition, position);
		move.queenMove = queenMove;
		if (positions != null) {
            move.positions = new ArrayList<Position>(positions);
        }
		if (capturedPositions != null) {
            move.capturedPositions = new ArrayList<Position>(capturedPositions);
        }
		if (capturedPieces != null) {
			move.capturedPieces = new ArrayList<Piece>();
			for (Piece piece: capturedPieces) {
                move.capturedPieces.add(piece.clone());
            }
		}
		return move;
	}

}
