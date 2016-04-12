/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;

public class MinMax {

	protected Board<? extends Piece> board;
	protected int searchDepth;
	protected boolean ignoreRepeated;

	protected Map<Move, Long> scores = new HashMap<Move, Long>();
	protected Map<String, EvaluatedBoard> boards = new HashMap<String, EvaluatedBoard>();

	protected static class EvaluatedBoard {

		int depth;
		Long value;

		public EvaluatedBoard(int depth, Long value) {
			this.depth = depth;
			this.value = value;
		}

	}

	protected int nodes = 0;
	protected int repeated = 0;

	public MinMax(Board<? extends Piece> board, int searchDepth, boolean ignoreRepeated) {
		this.searchDepth = searchDepth;
		this.board = board;
		this.ignoreRepeated = ignoreRepeated;
	}

	public int getNodes() {
		return nodes;
	}

	public int getRepeated() {
		return repeated;
	}

	public Long minMax(List<Move> availableMoves) {
		return minMax(availableMoves, 1, board.isBlackMove());
	}

	private Long minMax(List<Move> availableMoves, int depth, boolean max) {
		Long score = null;
		if (availableMoves.isEmpty()) {
			//Cheguei no final do jogo
			return board.evaluate();
		} else if (depth >= searchDepth) {
			//Cheguei no limite da busca
			for (Move move: availableMoves) {
				board.doMove(move);
				nodes++;
				long evaluate = board.evaluate();
				if (score == null)
					score = evaluate;
				else {
					if (max)
						score = Math.max(evaluate, score);
					else
						score = Math.min(evaluate, score);
				}
				if (depth == 1)
					scores.put(move, evaluate);
				board.undoMove();
			}
		} else {
			for (Move move: availableMoves) {
				board.doMove(move);
				nodes++;
				Long value;
				if (ignoreRepeated) {
					String hash = board.hash();
					if (boards.containsKey(hash) && boards.get(hash).depth <= depth) {
						value = boards.get(hash).value;
						repeated++;
					} else {
						value = minMax(board.getAvailableMoves(), depth + 1, !max);
						boards.put(hash, new EvaluatedBoard(depth, value));
					}
				} else
					value = minMax(board.getAvailableMoves(), depth + 1, !max);
				if (value != null) {
					if (score == null)
						score = value;
					else {
						if (max)
							score = Math.max(value, score);
						else
							score = Math.min(value, score);
					}
					if (depth == 1)
						scores.put(move, value);
				}
				board.undoMove();
			}
		}
		return score;
	}

	public int getBoardsAnalysed() {
		return boards.size();
	}

	public Map<? extends Move, Long> getScores() {
		return scores;
	}

}
