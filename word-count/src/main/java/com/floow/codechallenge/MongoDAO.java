package com.floow.codechallenge;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import com.floow.utils.ReadProperties;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

//This class is to perform all operations using MongoDB
public class MongoDAO {

	final static Logger logger = Logger.getLogger(MongoDAO.class);

	private final String hostName;
	private final String port;
	private Map<String, Integer> wordMap;
	private MongoClient mongoClient;

	public MongoDAO(String hostName, String port, Map<String, Integer> wordMap) {
		// TODO Auto-generated constructor stub
		this.hostName = hostName;
		this.port = port;
		this.wordMap = wordMap;

	}

	// Method to connect to MongoDB
	public void getMongoDBConnection() {
		int portNum = Integer.parseInt(port);
		try {
			// Initializing ReadProperties class to read values from property
			// file
			ReadProperties readProperties = new ReadProperties();
			readProperties.getPropValues();
			String dbName = readProperties.getDbName();
			String collectionName = readProperties.getCollectionName();
			this.mongoClient = new MongoClient(hostName, portNum);
			MongoDatabase database = mongoClient.getDatabase(dbName);
			MongoCollection<Document> collection = database.getCollection(collectionName);
			checkIfCollectionPresent(database,collectionName);
			insertIntoCollection(collection);
		} catch (Exception e) {

			logger.error(e.getMessage());
		}

	}
	
	public void checkIfCollectionPresent(MongoDatabase database,String collectionName)
	{
		 MongoIterable<String> collectionNames = database.listCollectionNames();
		 
	        for(String name: collectionNames) {
	            if (name.equalsIgnoreCase(collectionName))
	            {
	            	//logger.debug("both Collections are equal");
	            	database.getCollection(collectionName).drop();
	            }
	        }
	}

	// Method to insert into MongoDB
	public void insertIntoCollection(MongoCollection<Document> collection) {
		Set<Entry<String, Integer>> set = wordMap.entrySet();
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		Document document = null;

		for (Map.Entry<String, Integer> entry : list) {
			document = new Document("wordName", entry.getKey()).append("numberOfOccurances", entry.getValue());

			collection.insertOne(document);

		}

		//logger.debug("Inserted successfully");

		getMinRepeatedWord(collection);
		getMaxRepeatedWord(collection);

	}

	// Method to display all the words having minimum number of occurrences
	public void getMinRepeatedWord(MongoCollection<Document> collection) {

		// Logic for finding the number of words having same number of minimum
		// value in numberOfOccurences field)

		Document group = new Document("$group",
				new Document("_id", "$numberOfOccurances").append("count", new Document("$sum", 1)));
		Document sort = new Document("$sort", new Document("_id", 1));
		Document limit = new Document("$limit", 1);
		Document project = new Document("$project", new Document("_id", 0));
		AggregateIterable<Document> output = collection.aggregate(asList(group, sort, limit, project))
				.allowDiskUse(true);

		
		// Assigning the number of words having same number of minimum value
		// into a variable num
		int num = 0;
		for (Document d : output) {
			num = d.getInteger("count");
		}
		logger.debug("Total number of words having minimum count  " + num);

		// Logic to get all the documents having minimum numberOfOccurences
		Document mainsort = new Document("$sort", new Document("numberOfOccurances", 1));
		Document mainlimit = new Document("$limit", num);
		AggregateIterable<Document> output1 = collection.aggregate(asList(mainsort, mainlimit)).allowDiskUse(true);
		Iterator<Document> itr1 = output1.iterator();

		while (itr1.hasNext()) {

			logger.debug("Minimum repeated words   " + itr1.next());
		}

	}

	// Method to display all the words having maximum number of occurrences
	public void getMaxRepeatedWord(MongoCollection<Document> collection) {

		// Logic for finding the number of words having same number of maximum
		// value in numberOfOccurences field)

		Document groupmax = new Document("$group",
				new Document("_id", "$numberOfOccurances").append("count", new Document("$sum", 1)));
		Document sortmax = new Document("$sort", new Document("_id", -1));
		Document limitmax = new Document("$limit", 1);
		Document projectmax = new Document("$project", new Document("_id", 0));
		AggregateIterable<Document> output = collection.aggregate(asList(groupmax, sortmax, limitmax, projectmax))
				.allowDiskUse(true);

		

		// Assigning the number of words having same number of maximum value
		// into a variable num
		int num = 0;
		for (Document d : output) {
			num = d.getInteger("count");

		}
		logger.debug("Total number of words having maximum count  " + num);

		// Logic to get all the documents having maximum numberOfOccurences
		Document mainmaxsort = new Document("$sort", new Document("numberOfOccurances", -1));
		Document mainmaxlimit = new Document("$limit", num);
		AggregateIterable<Document> output1 = collection.aggregate(asList(mainmaxsort, mainmaxlimit))
				.allowDiskUse(true);
		Iterator<Document> itr1 = output1.iterator();
		while (itr1.hasNext()) {

			logger.debug("Maximum repeated words   " + itr1.next());
		}
		closeMongoDBConnection();
	}

	// Method to close the mongodbConnection
	public void closeMongoDBConnection() {
		mongoClient.close();
	}

}
