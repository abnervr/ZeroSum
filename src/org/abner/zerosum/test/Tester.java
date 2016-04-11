/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.checkers.CheckersBoard;
import org.abner.zerosum.game.Logger;
import org.abner.zerosum.game.Search;

public class Tester {

	private static int MIN_DEPTH = 1;
	private static int MAX_DEPTH = 6;

	private static int MAX_MOVES = 1000;
	private static int c = 0;

	private List<Search> searches = new ArrayList<Search>();
	private CheckersBoard board;

	public int start() {
		board = new CheckersBoard();
		searches.clear();
		for (int d = MIN_DEPTH; d <= MAX_DEPTH; d++)
			for (short i = 0; i <= 7; i++)
				searches.add(buildSearch(d, (i & 1) == 1, (i & 2) == 2, (i & 4) == 4));
		Move bestMove = null;
		for (; c < MAX_MOVES && !board.getAvailableMoves().isEmpty(); c++) {
			Map<Move, Long> scores = null;
			Long bestScore = null;
			if (board.getAvailableMoves().size() > 1) {
				for (int d = MIN_DEPTH; d <= MAX_DEPTH; d++) {
					for (Search search: searches)
						if (search.getSearchDepth() == d) {
							Move move = search.getMove();
							if (scores == null) {
								scores = search.getScores();
								bestScore = scores.get(move);
								bestMove = move;
							} else {
								try {
									if (search.getScores().get(move) == null || !search.getScores().get(move).equals(bestScore)) {
										System.out.println("Invalid best move for " + search.toString());
										System.out.println(board.hash() + " " + search.getScores() + " expected -> " + scores);
										continue;
									}
									for (Entry<Move, Long> e: scores.entrySet())
										if (e.getValue().equals(bestScore) && !search.getScores().get(e.getKey()).equals(bestScore)) {
											System.out.println("Invalid output for " + search.toString());
											System.out.println(board.hash() + " " + search.getScores() + " expected -> " + scores);
											break;
										}
								} catch (Exception e) {
									e.printStackTrace();
									try {
										board.save();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							}
						}
					if (d + 1 <= MAX_DEPTH)
						scores = null;
				}
				board.doMove(bestMove);
			} else {
				board.doMove(board.getAvailableMoves().get(0));
				c--;
			}
		}
		return c;
	}

	private Search buildSearch(int depth, boolean alpha, boolean repeated, boolean multi) {
		Search search = new Search(board, depth);
		search.setAlphaBetaPruning(alpha);
		search.setIgnoreRepeated(repeated);
		search.setMultithreading(multi);
		return search;
	}

	public static void main(String[] args) {
		new Tester().start();
		while (c < MAX_MOVES) {
			new Tester().start();
		}
		Logger.logStatistic();
	}
}
