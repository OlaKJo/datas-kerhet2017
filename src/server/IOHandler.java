package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IOHandler {
	public static List<String> read(String path) {
		List<String> lines = new ArrayList<>();

		String line = null;
		BufferedReader br = null;
		FileReader fr = null;

		try {

			fr = new FileReader(path);
			br = new BufferedReader(fr);

			br = new BufferedReader(new FileReader(path));

			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
		return lines;
	}

	private static void write(String path, String data, boolean append) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(path, append);
			bw = new BufferedWriter(fw);

			bw.write(data);

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
	}

	private static void write(String path, List<String> data, boolean append) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(path, append);
			bw = new BufferedWriter(fw);

			for (String s : data) {
				bw.write(s + "\n");
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
	}

	public static void append(String path, String data) {
		write(path, data, true);
	}
	
	public static void append(String path, List<String> data) {
		write(path, data, true);
	}

	public static void clearFile(String path) {
		write(path, "", false);
	}

	public static void createFile(String path) {
		write(path, "", true);
	}

	public static void eraseFile(String path) {
		try {
			File file = new File(path);
			file.delete();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
