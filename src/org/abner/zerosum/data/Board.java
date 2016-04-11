package org.abner.zerosum.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsável por mapear posições das peças e o historico do jogo
 * 
 * @author Abner
 */
public abstract class Board<T extends Piece> implements Serializable {

	private static final long serialVersionUID = -2641023779089735494L;

	public static final int BOARD_SIZE = 8;

	protected Map<Position, T> pieces = new HashMap<Position, T>();

	protected boolean isBlackMove = false;
	protected int qtdMoves = 0;

	protected List<Move> moves = new ArrayList<Move>();

	protected Board() {}

	public Board(Map<Position, T> pieces) {
		this.pieces = pieces;
	}

	public boolean isBlackMove() {
		return isBlackMove;
	}

	public abstract String hash();

	public abstract long evaluate();

	public abstract List<Move> getAvailableMoves();

	public abstract void save() throws IOException;

	public int getQtdMoves() {
		return qtdMoves;
	}

	@SuppressWarnings("unchecked")
	public void doMove(Move move) {
		qtdMoves += 1;
		isBlackMove = !isBlackMove;
		pieces.remove(move.getStartPosition());
		pieces.put(move.getPosition(), (T)move.getPiece());
		if (move.getCapturedPositions() != null)
			for (Position position: move.getCapturedPositions())
				move.addCapturedPiece(pieces.remove(position));
		this.moves.add(move);
	}

	@SuppressWarnings("unchecked")
	public Move undoMove() {
		if (!moves.isEmpty()) {
			Move move = moves.remove(moves.size() - 1);
			qtdMoves -= 1;
			isBlackMove = !isBlackMove;
			pieces.remove(move.getPosition());
			pieces.put(move.getStartPosition(), (T)move.getPiece());
			List<Position> positions = move.getCapturedPositions();
			if (positions != null) {
				List<T> pieces = (List<T>)move.getCapturedPieces();
				for (int i = 0; i < positions.size(); i++)
					this.pieces.put(positions.get(i), pieces.get(i));
			}
			return move;
		}
		return null;
	}

	public Map<Position, T> getPieces() {
		return new HashMap<Position, T>(pieces);
	}

	@Override
	public abstract Board<? extends Piece> clone();

}
