package com.floow.codechallenge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//This is the main class reads files as chunks and calculate the word count using multi threading.
public class WordCount {

	private volatile static Map<String, Integer> wordMap = new ConcurrentHashMap<String, Integer>();

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String fileName = args[0];
		String hostName = args[1];
		String port = args[2];

		// returns the number of processors available to the java virtual
		// machine
		int chunks = Runtime.getRuntime().availableProcessors();
		long[] offsets = new long[chunks];
		File file = new File(fileName);

		// Determine line boundaries for number of chunks
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		for (int i = 1; i < chunks; i++) {
			raf.seek(i * file.length() / chunks);
			while (true) {
				int read = raf.read();

				if (read == '\n' || read == -1) {
					break;
				}

			}

			offsets[i] = raf.getFilePointer();

		}
		raf.close();

		// Process each chunk using a thread for each one
		ExecutorService service = Executors.newScheduledThreadPool(chunks);
		for (int i = 0; i < chunks; i++) {
			long start = offsets[i];
			long end = i < chunks - 1 ? offsets[i + 1] : file.length();
			service.execute(new FileProcessor(file, start, end, hostName, port));

		}
		service.shutdown();
		MongoDAO mongodao = new MongoDAO(hostName, port, wordMap);
		mongodao.getMongoDBConnection();

	}

	static class FileProcessor implements Runnable {

		private final File file;
		private final long start;
		private final long end;

		public FileProcessor(File file, long start, long end, String hostName, String port) {
			this.file = file;
			this.start = start;
			this.end = end;

		}

		public void run() {
			// TODO Auto-generated method stub

			RandomAccessFile raf;
			String line = null;
			try {
				raf = new RandomAccessFile(file, "r");
				raf.seek(start);
				while (raf.getFilePointer() < end) {
					if ((line = raf.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,.:;?![]'");
						while (st.hasMoreTokens()) {
							String tmp = st.nextToken();

							if (wordMap.containsKey(tmp)) {
								wordMap.put(tmp, wordMap.get(tmp) + 1);
							} else {
								wordMap.put(tmp, 1);
							}

						}
					}
				}

			}

			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
