/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.util.List;

import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;

public class AlphaBetaPruning extends MinMax {

	public AlphaBetaPruning(Board<? extends Piece> board, int searchDepth, boolean ignoreRepeated) {
		super(board, searchDepth, ignoreRepeated);
	}

	@Override
	public Long minMax(List<Move> availableMoves) {
		return minMax(Long.MIN_VALUE, Long.MAX_VALUE, availableMoves, 1, board.isBlackMove());
	}

	private Long minMax(long alpha, long beta, List<Move> availableMoves, int depth, boolean max) {
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
				if (depth == 1)
					scores.put(move, evaluate);
				if (score == null)
					score = evaluate;
				else {
					if (max)
						score = Math.max(evaluate, score);
					else
						score = Math.min(evaluate, score);
				}
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
						value = minMax(alpha, beta, board.getAvailableMoves(), depth + 1, !max);
						boards.put(hash, new EvaluatedBoard(depth, value));
					}
				} else
					value = minMax(alpha, beta, board.getAvailableMoves(), depth + 1, !max);
				if (value != null) {
					if (score == null)
						score = value;
					else {
						if (max) {
							alpha = score = Math.max(value, score);
						} else {
							beta = score = Math.min(value, score);
						}
					}
					if (depth == 1)
						scores.put(move, value);
					//Poda alpha beta
					boolean prune = false;
					if (max) {
						prune = score > beta;
					} else {
						prune = score < alpha;
					}

					if (prune) {
						board.undoMove();
						return score;
					}

				}
				board.undoMove();
			}
		}
		return score;
	}
}
