package com.floow.codechallenge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

//This is the main class reads files as chunks and calculate the word count using multi threading.
public class WordCount {

	final static Logger logger = Logger.getLogger(WordCount.class);

	private static volatile Map<String, Integer> wordMap = new ConcurrentHashMap<String, Integer>();

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String fileName = args[0];
		String hostName = args[1];
		String port = args[2];

		// original arguments passed
		/*
		 * String fileName = "E://Ganesh//foo.txt"; String hostName =
		 * "localhost"; String port = "27017";
		 */

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
		ExecutorService service = Executors.newFixedThreadPool(chunks);
		for (int i = 0; i < chunks; i++) {
			long start = offsets[i];
			long end = i < chunks - 1 ? offsets[i + 1] : file.length();
			service.execute(new FileProcessor(file, start, end, hostName, port));

		}

		// Make sure executor stops
		service.shutdown();

		try {

			// Blocks until all tasks have completed execution after a shutdown
			// request
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);

		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

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
			// String threadName = Thread.currentThread().getName();
			// logger.debug("Thread "+ threadName + " started running! ");

			RandomAccessFile raf;
			String line = null;
			try {
				raf = new RandomAccessFile(file, "r");
				raf.seek(start);
				while (raf.getFilePointer() < end) {

					line = raf.readLine();
					if (line != null) {

						StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,.:;?![]'");
						while (st.hasMoreElements()) {
							String tmp = st.nextToken();
							// logger.debug("tmp"+tmp);

							Integer count = wordMap.putIfAbsent(tmp, 1);
							if (count != null) {
								wordMap.put(tmp, wordMap.get(tmp) + 1);
							}

						}
					}

				}
			}

			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block

				logger.error(e.getMessage());
			}

			catch (IOException e) {

				logger.error(e.getMessage());
			}
			/*
			 * finally {
			 * 
			 * logger.debug("Thread " +threadName + " finished running! "); }
			 */
		}

	}

}
