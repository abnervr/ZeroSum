/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.game;

import java.io.IOException;
import java.util.Map.Entry;

import org.abner.zerosum.UIBuilder;
import org.abner.zerosum.data.Board;
import org.abner.zerosum.data.Move;
import org.abner.zerosum.data.Piece;
import org.abner.zerosum.data.Position;
import org.abner.zerosum.data.checkers.CheckersBoard;

public class GameSession {

	private Board<? extends Piece> board;
	private Search search;

	public String getUndo() {
		if (board != null) {
			Move move = board.undoMove();

			if (move != null) {
				StringBuilder sb = new StringBuilder();

				if (move.getCapturedPositions() != null)
					for (int i = 0; i < move.getCaptured(); i++)
						sb.append(UIBuilder.buildPiece(move.getCapturedPieces().get(i), move.getCapturedPositions().get(i)));

				sb.append(UIBuilder.removePiece(move.getPosition(), 1));

				sb.append(UIBuilder.buildPiece(move.getPiece(), move.getStartPosition()));

				//Debug
				sb.append(UIBuilder.showScore(board.evaluate(), board.getQtdMoves(),
					"Jogadas disponiveis: " + board.getAvailableMoves().size()));

				return sb.toString();
			}
		}
		return "";
	}

	public String getPlay(boolean nextMove, boolean showScore, boolean ignoreRepeated, boolean alphaBeta, boolean multithreading, int p1, int p2) {
		try {
			if (board == null) {
				board = new CheckersBoard();
				search = new Search(board, 1);
				StringBuilder sb = new StringBuilder();
				sb.append(UIBuilder.buildBoard(board, false));
				if (nextMove)
					sb.append(UIBuilder.nextMove(1900));
				return sb.toString();
			} else {
				if (board.isBlackMove())
					search.setSearchDepth(p1);
				else
					search.setSearchDepth(p2);
				search.setIgnoreRepeated(ignoreRepeated);
				search.setAlphaBetaPruning(alphaBeta);
				search.setMultithreading(multithreading);
				Move move = search.getMove();
				if (move != null) {
					StringBuilder sb = new StringBuilder();
					Long value = search.getScores().get(move);
					if (showScore)
						for (Entry<Move, Long> entry: search.getScores().entrySet()) {
							board.doMove(entry.getKey());
							String color = entry.getKey().equals(move) ? "red" : "black";
							if (color.equals("black") && (value != null ? entry.getValue() != null && value.equals(entry.getValue()) : entry.getValue() == null))
								color = "purple";
							sb.append(UIBuilder.buildScore(entry.getKey(), String.valueOf(entry.getValue()), color));
							board.undoMove();
						}
					board.doMove(move);
					if (move.getCapturedPositions() != null)
						for (int i = 0; i < move.getCaptured(); i++)
							sb.append(UIBuilder.removePiece(move.getCapturedPositions().get(i), i + 1));

					if (move.isQueenMove())
						sb.append(UIBuilder.upgradePiece(move.getPosition(), move.getCaptured() > 0 ? move.getCaptured() : 1));

					sb.append(UIBuilder.movePiece(move));

					//Debug
					sb.append(UIBuilder.showScore(board.evaluate(), board.getQtdMoves(),
						"Nós: " + search.getNodes() + "<br>" +
							"Tempo: " + search.getSearchTime() + "ms" + "<br>"
							+ (search.getRepeated() > 0 ? ("Tabuleiros analisados: " + search.getBoardsAnalysed() + "<br>"
								+ "Estados de tabuleiros repetidos: " + search.getRepeated() + "<br>") : "")
							+ search.getExtraInfo()
						));
					if (nextMove)
						if (move.getCaptured() > 1)
							sb.append(UIBuilder.nextMove(1900 + (500 * move.getCaptured() - 1)));
						else
							sb.append(UIBuilder.nextMove(1900));

					return sb.toString();
				} else {
					Logger.logStatistic();
					StringBuilder sb = new StringBuilder();
					sb.append(UIBuilder.showScore(board.evaluate(), board.getQtdMoves(), "Game over"));
					for (Position x: board.getPieces().keySet())
						sb.append(UIBuilder.removePiece(x, 1));
					sb.append(UIBuilder.nextMove(1900));
					board = null;
					return sb.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return UIBuilder.showError(e.getMessage());
		}
	}

	public String saveBoard() {
		String status;
		try {
			board.save();
			status = "Tabuleiro salvo com sucesso";
		} catch (IOException e) {
			e.printStackTrace();
			status = "Erro ao salvar tabuleiro: " + e.getMessage();
		}
		return UIBuilder.showScore(board.evaluate(), board.getQtdMoves(), status);

	}
}
