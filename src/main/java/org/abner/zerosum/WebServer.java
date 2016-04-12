/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.abner.zerosum.game.GameSession;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class WebServer {

	public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(4280), 0);
		server.createContext("/", new MyHandler());
		server.setExecutor(null); // creates a default executor

		server.start();
	}

	static class MyHandler implements HttpHandler {

		private Map<String, GameSession> sessions = new HashMap<String, GameSession>();

		public void handle(HttpExchange t) throws IOException {
			InputStream request = t.getRequestBody();

			GameSession session = null;
			URI requestURI = t.getRequestURI();
			String path = requestURI.getPath();
			String remoteHost = t.getRemoteAddress().getHostString();
			if (path.startsWith("/play") || path.startsWith("/undo") || path.startsWith("/save")) {
				session = sessions.get(remoteHost);
			} else if (path.equals("/")) {
				System.out.println("Removing session for host: " + remoteHost);
				sessions.remove(remoteHost);
			} else if (!path.equals("/favicon.ico")) {
				System.out.println("Ignoring request" + path);
			}
			int read = request.read();
			while (read != -1) {
				System.out.print(read);
				read = request.read();
			}
			request.close();

			byte[] respB = null;
			Headers headers = t.getResponseHeaders();

			try {
				if (session == null) {
					if (path.equals("/favicon.ico")) {
						FileInputStream is = new FileInputStream(new File("./icon.ico"));
						read = is.available();
						respB = new byte[read];
						is.read(respB);
						is.close();
					} else {
						BufferedReader reader = new BufferedReader(new FileReader(new File("./damas.html")));
						String response = "";
						while (reader.ready()) {
							response += reader.readLine() + '\n';
						}
						reader.close();
						respB = response.getBytes();
						System.out.println("Creating session for " + remoteHost);
						sessions.put(remoteHost, new GameSession());
					}
				} else if (path.startsWith("/undo")) {
					respB = session.getUndo().getBytes();
				} else if (path.startsWith("/play")) {
					String query = requestURI.getQuery();
					if (query == null) {
                        query = "";
                    }
					boolean autoRequest = query.contains("autorequest=1");
					boolean showScore = query.contains("showscore=1");
					boolean ignoreRepeated = query.contains("ignorerepeated=1");
					boolean alphaBeta = query.contains("alphabeta=1");
					boolean multithreading = query.contains("threads=1");
					int p1 = 0, p2 = 0;
					try {
						for (String value: query.split("&")) {
                            if (value.contains("p1=") && value.indexOf('=') < value.length() - 1) {
                                p1 = Integer.parseInt(value.substring(value.indexOf("=") + 1));
                            }
                        }
					} catch (NumberFormatException e) {}
					try {
						for (String value: query.split("&")) {
                            if (value.contains("p2=") && value.indexOf('=') < value.length() - 1) {
                                p2 = Integer.parseInt(value.substring(value.indexOf("=") + 1));
                            }
                        }
					} catch (NumberFormatException e) {}
					respB = session.getPlay(autoRequest, showScore, ignoreRepeated, alphaBeta, multithreading, p1, p2).getBytes();
				} else if (path.startsWith("/save")) {
					respB = session.saveBoard().getBytes();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			headers.add("Content-Type", "text/html; charset=utf-8");
			if (respB == null) {
                t.sendResponseHeaders(404, 0);
            } else {
                t.sendResponseHeaders(200, respB.length);
            }
			OutputStream os = t.getResponseBody();
			if (respB != null) {
                os.write(respB);
            }
			os.close();
		}
	}
}
