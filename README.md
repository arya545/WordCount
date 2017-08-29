# WordCount
This program is designed to read a file with a large body of text and to count unique words so that the most common and least 
common words can be identified.
Note:Program is expecting ANSI file format for input text file.To read UTF-8/Unicode encoded text file,program need modifications

Softwares needs to be installed:
--------------------------------
Mongodb - version 3.4,
Eclipse Neon,
Jdk-8.

The command to run the jar

java –Xmx8192m  -jar Nameofjar  FileName  hostName  port

FileName : Input text file to be read ,
hostname : host in which mongodb is installed,
port : port name .

Below command is used in my system to run this program in command prompt.

java –Xmx8192m -jar C:\Users\ratheev\git\word-count\target\word-count-0.0.1-SNAPSHOT-jar-with-dependencies.jar   E://Ganesh//foo.txt   localhost     27017     

This will display the maximum and minimum repeated unique words and their count on the screen.
 
Design and Architecture
------------------------

This program is designed in such a way that it can distribute the workload efficiently.It is also easily scalable.Program supports
execution on multiple servers that communicate via a common means and work together to break down the workload.
Based on the number of processors available to the java virtual machine,a threadpool is created to 
enable parallel data processing and store the data in a common shared memory area.The processed data from shared memory is inserted into MongoDB
collection for wordcount.

List of property files:
----------------------
config.properties - This is to enter the database name and collection name of MongoDB.

log4j.properties -  This is to set the logging properties.

List of classes:
---------------
WordCount.java - This is the main class from which the program starts.This class perform read operation by dividing the very large file 
                 into a number of chunks using RandomAccessFile API.The number of chunks is calculated based on the number of processors available to the java 
                 virtual machine.Based on the number of chunks ,a threadpool is created to 
                 enable parallel data processing(Read the text line by line,extract words out of it,calculate the number of 
                 occurences of each word) of each chunk.The words and its corresponding count is 
                 stored  in a common shared memory area called "concurrent hashmap" which is thread safe .Once all the threads
                 have finished the data processing ,threads are shut down 
                 gracefully .Here "ExecutorService" is used to manage the threads.WordCount
                 class uses an inner class named "FileProcessor" to implement Runnable interface.Finally control is handed to MongoDAO class.
                 
                                 
 MongoDAO.java - This class contains method to connect with mongoDBserver.Once the connection to the server is established, data 
                 gets inserted into the MongoDB collection.Once insertion is successfull , methods to find the maximum 
                 repeated words and minimum repeated words are performed.
                 
                 List of Methods
                 ---------------
                 getMongoDBConnection - This is to establish connection to MongoDB server from java platform.
                 checkIfCollectionPresent - This is to check whether collection exist or not.If collection already exist,
				                                    it will drop the existing collection else new collection gets created.
                 insertIntoCollection - This is to insert the words and its number of occurences to MongoDB server.
                 getMaxRepeatedWord  - This is to get the words having maximum number of occurences.
                 getMinRepeatedWord  -  This is to get the words having minimum number of occurences.
                 closeMongoDBConnection - This is to close the Mongoclient.
                 
ReadProperties.java - This class is for reading the config.properties file.

                    List of Methods
                    ---------------
                    getPropValues- This is to read the config.properties file.
                    getters and setters

WordCountTest.java -  This is a sample test class.Currently this is not used in this program.User can modify this class and write the
                      test conditions according to the requirements.

