/* Copyright (c) 2013 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

public class ReadFile {

	private static String xOffset, yOffset;

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("./src/org/abner/damas/tower.txt")));
		while (reader.ready()) {
			String originalLine = reader.readLine();
			String line = originalLine;
			if (xOffset == null && line.indexOf("transform=\"") != -1) {
				line = line.substring(line.indexOf("transform=\"translate(") + "transform=\"translate(".length());
				line = line.substring(0, line.indexOf(')'));
				xOffset = line.split(",")[0];
				yOffset = line.split(",")[1];
				System.out.println(originalLine);
			} else if (line.indexOf(" d=\"") != -1) {
				line = line.substring(line.indexOf(" d=\"") + 4);
				if (line.indexOf('"') != -1)
					line = line.substring(0, line.indexOf('"'));
				else {
					line.trim();
					String nextLine = originalLine.trim();
					line += nextLine.substring(0, nextLine.indexOf('"'));
				}
				String x = null, y = null;
				Character operator = null;
				char[] path = line.toCharArray();
				line = "";
				for (int i = 0; i < path.length; i++) {
					char c = path[i];
					if (operator != null && Character.isUpperCase(operator) && ((c >= '0' && c <= '9') || c == '.' || c == '-')) {
						switch (operator) {
							case 'M':
								x = getNumber(path, i);
								i += x.length();
								c = path[++i];
								y = getNumber(path, i);
								i += y.length();
								line += new BigDecimal(x).add(new BigDecimal(xOffset)) + ",";
								line += new BigDecimal(y).add(new BigDecimal(yOffset));
								break;
							case 'H':
								x = getNumber(path, i);
								i += x.length();
								line += new BigDecimal(x).add(new BigDecimal(xOffset));
								break;
							case 'V':
								y = getNumber(path, i);
								i += y.length();
								line += new BigDecimal(y).add(new BigDecimal(yOffset));
								break;
							default:
								break;
						}
						i--;
						operator = null;
						continue;
					} else if (c != '\n') {
						if (Character.isUpperCase(c))
							operator = c;
						line += (c);
					}
				}
				System.out.println(originalLine.replaceAll("[$ ]d=\".*\"", " d=\"" + line + "\""));
			} else {
				System.out.println(originalLine);
			}

		}
		reader.close();
	}

	private static String getNumber(char[] path, int i) {
		String number = "";
		char c = path[i];
		while ((c >= '0' && c <= '9') || c == '.' || c == '-') {
			number += c;
			c = path[++i];
		}
		return number;
	}
}
