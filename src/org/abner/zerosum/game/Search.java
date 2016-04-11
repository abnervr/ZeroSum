/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;
import org.abner.zerosum.data.Position;

public class Search {

	private Board<? extends Piece> board;
	private int searchDepth;

	private int repeated;
	private int nodes;
	private int boardAnalysed;
	private long searchTime;
	private boolean ignoreRepeated;
	private boolean alphaBetaPruning;
	private boolean multithreading;

	private Map<Move, Long> scores = new HashMap<Move, Long>();

	private String extraInfo;

	public Search(Board<? extends Piece> board, int searchDepth) {
		this.board = board;
		this.searchDepth = searchDepth;
	}

	private String getModifiers() {
		return (multithreading ? "1" : "0") + (ignoreRepeated ? "1" : "0") + (alphaBetaPruning ? "1" : "0");
	}

	public void setSearchDepth(int searchDepth) {
		if (searchDepth > Logger.MAX_DEPTH)
			throw new RuntimeException("Profundidade invalida");
		this.searchDepth = searchDepth;
	}

	public int getSearchDepth() {
		return searchDepth;
	}

	public void setIgnoreRepeated(boolean ignoreRepeated) {
		this.ignoreRepeated = ignoreRepeated;
	}

	public void setAlphaBetaPruning(boolean alphaBetaPruning) {
		this.alphaBetaPruning = alphaBetaPruning;
	}

	public void setMultithreading(boolean multithreading) {
		this.multithreading = multithreading;
	}

	public int getRepeated() {
		return repeated;
	}

	public int getBoardsAnalysed() {
		return boardAnalysed;
	}

	public long getSearchTime() {
		return searchTime;
	}

	public Map<Move, Long> getScores() {
		return scores;
	}

	public int getNodes() {
		return nodes;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	@Override
	public String toString() {
		return "Search [searchDepth=" + searchDepth + ", ignoreRepeated=" + ignoreRepeated + ", alphaBetaPruning=" + alphaBetaPruning + ", multithreading=" + multithreading + "]";
	}

	public Move getMove() {
		long time = System.currentTimeMillis();
		Move move = null;
		searchTime = repeated = nodes = boardAnalysed = 0;
		extraInfo = "";
		scores.clear();
		if (searchDepth < 1)
			move = getRandomMove();
		else {
			List<Move> availableMoves = new ArrayList<Move>(board.getAvailableMoves());
			Long value = null;
			if (!availableMoves.isEmpty())
				if (multithreading && availableMoves.size() > 1) {
					List<Worker> workers = new ArrayList<Search.Worker>();
					int threads;
					int movesPerThread;
					if (availableMoves.size() == 3)
						threads = 3;
					else
						threads = 4;
					if (availableMoves.size() % 2 == 1)
						movesPerThread = (availableMoves.size() - 1) / threads;
					else
						movesPerThread = availableMoves.size() / threads;
					for (int i = 0; i < threads && !availableMoves.isEmpty(); i++) {
						List<Move> subMoves = new ArrayList<Move>();
						for (int j = 0; j <= movesPerThread && !availableMoves.isEmpty(); j++)
							subMoves.add(availableMoves.remove(0));
						if (i + 1 == threads)
							while (!availableMoves.isEmpty())
								subMoves.add(availableMoves.remove(0));
						if (!subMoves.isEmpty()) {
							MinMax minMax;
							if (alphaBetaPruning)
								minMax = new AlphaBetaPruning(board.clone(), searchDepth, ignoreRepeated);
							else
								minMax = new MinMax(board.clone(), searchDepth, ignoreRepeated);
							Worker worker = new Worker(minMax);
							worker.availableMoves = subMoves;
							workers.add(worker);
							worker.start();
						}
					}

					boolean alive = true;
					while (alive) {
						Thread.yield();
						alive = false;
						for (Worker worker: workers)
							if (worker.isAlive()) {
								alive = true;
								break;
							}
					}

					for (Worker worker: workers) {
						repeated += worker.minMax.getRepeated();
						nodes += worker.minMax.getNodes();
						boardAnalysed += worker.minMax.getBoardsAnalysed();
						scores.putAll(worker.minMax.getScores());

						if (value == null) {
							value = worker.result;
						} else {
							if (board.isBlackMove()) {
								value = Math.max(value, worker.result);
							} else {
								value = Math.min(value, worker.result);
							}
						}
					}
				} else {
					if (availableMoves.size() == 1) {
						move = availableMoves.get(0);
						board.doMove(move);
						board.undoMove();
						nodes = 1;
						scores.put(move, board.evaluate());
						//Evita que esse valor entre na estatística
						return move;
					} else {
						MinMax minMax;
						if (alphaBetaPruning)

							minMax = new AlphaBetaPruning(board.clone(), searchDepth, ignoreRepeated);
						else
							minMax = new MinMax(board.clone(), searchDepth, ignoreRepeated);
						value = minMax.minMax(availableMoves);

						repeated = minMax.getRepeated();
						nodes = minMax.getNodes();
						boardAnalysed = minMax.getBoardsAnalysed();
						scores.putAll(minMax.getScores());
					}
				}
			if (value != null) {
				Map<Position, List<Move>> moves = new HashMap<Position, List<Move>>();
				for (Entry<Move, Long> e: scores.entrySet())
					if (e.getValue() != null && value.equals(e.getValue())) {
						if (moves.containsKey(e.getKey().getStartPosition()))
							moves.get(e.getKey().getStartPosition()).add(e.getKey());
						else {
							List<Move> pieceMoves = new ArrayList<Move>();
							pieceMoves.add(e.getKey());
							moves.put(e.getKey().getStartPosition(), pieceMoves);
						}
					}
				Random random = new Random();
				int piecePosition = random.nextInt(moves.size());
				int i = 0;
				for (Position position: moves.keySet())
					if (i++ == piecePosition) {
						List<Move> pieceMoves = moves.get(position);
						move = pieceMoves.get(random.nextInt(pieceMoves.size()));
						break;
					}
			}
		}

		searchTime = System.currentTimeMillis() - time;
		if (searchDepth > 0 && searchDepth <= Logger.MAX_DEPTH) {
			Logger.getTimeCounters(getModifiers()).get(searchDepth).touch(searchTime);
			Logger.getNodeCounters(getModifiers()).get(searchDepth).touch(nodes);
		}
		return move;
	}

	public Move getRandomMove() {
		List<Move> moves = board.getAvailableMoves();
		if (moves.isEmpty())
			return null;
		return moves.get(new Random().nextInt(moves.size()));
	}

	private static class Worker extends Thread {

		private MinMax minMax;
		private long result;
		private List<Move> availableMoves;

		public Worker(MinMax minMax) {
			this.minMax = minMax;
		}

		@Override
		public void run() {
			try {
				result = minMax.minMax(availableMoves);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
