/*
 * Jagmeet Singh Grewal
 * ServerWorkerThread
 * Desc: Thread that accepts worker clients and decides how to process requests
 */
package Assignment;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ServerWorkerThread extends Thread{
	//Properties
	private final int MAX_ONE_LENGTH = 10; 		//Max String Length for Strategy 1
	private final int MAX_TWO_LENGTH = 25;		//Max String Length for Strategy 2
	private int portNumber;						//Port Number for worker to connect to 
	private Queue<Socket> workers;				//Queue of sockets that connect to workers
	private HashMap<String, Request> mem;		//Access to shared memory in server
	private Object lock;						//Object for Shared memory to synchronise on
	private Object workerLock = new Object();	//Object for workers queue to synchronise on
	private int numOfWorkers;					//Number of workers available to server
	
	//Constructor 
	public ServerWorkerThread(int port, int numOfWorkers,HashMap<String, Request> mem, Object lock) {
		this.portNumber = port;
		this.lock = lock;
		this.mem = mem;
		this.workers = new LinkedList<Socket>();
		this.numOfWorkers = numOfWorkers;
		
		try {
			//Connect to all workers first, before starting work
			ServerSocket s = new ServerSocket(portNumber);
			for(int i =0;i<numOfWorkers;i++) {
				Socket s1 = s.accept();		//Accept Worker Connection
				workers.add(s1);			//Add connection to queue
			}
			s.close(); //Close so that no more workers can connect
		} catch (IOException e) {
			System.out.println("Error Connecting to Workers ("+e.getMessage()+")");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}  
		
	}
	
	public void run() {
		while(true) {
			Request r = null;
			
			//Make Server to wait until there is a new request 
			synchronized (lock) {

				r = readNewRequest();
				//If there are no new request, wait
				while(r==null) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						System.out.println("Shared Memory Lock Interuption");
					}
					//Check for new request
					r = readNewRequest();
				}
				//Request Found at this point
				lock.notifyAll();
			}
			//If new request exists then process
			if(r!=null) {			
				//Process Strategy is chosen based on string length
				if(r.message.length() <= MAX_ONE_LENGTH) {
					strategyOne(r);
					System.out.println("1");
				} else if(r.message.length() <= MAX_TWO_LENGTH) {
					strategyTwo(r);
					System.out.println("2");
				} else {
					strategyThree(r);
					System.out.println("3");
				}
			}	
		}
	}
	
	//Strategy One of assignment - Server does work
	//Input - Request
	private void strategyOne(Request r){

		//Change the request status to processing in shared memory
		synchronized (lock) {
			mem.get(r.password).requestStatus = Request.Status.PROCESSING;
			lock.notifyAll();
		}
		
		//Compute Average and store result in request object
		char c = FrequentCharacterFinder(r.message);
		r.output = c;
		r.requestStatus = Request.Status.DONE;
		
		//Add Request Object with output back to shared memory
		synchronized (lock) {
			mem.replace(r.password, r);
			lock.notifyAll();
		}
	}
	
	//Strategy Two of assignment - use worker to compute work
	//Input - Request
	private void strategyTwo(Request r) {
		synchronized (workerLock) {
			//Wait until there is at least one worker available
			while(workers.size() <1){
				try {
					workerLock.wait();
				} catch (InterruptedException e) {
					System.out.println("Worker Wait Interupted in Strategy Two: "+e.getMessage());
				}
			}
			//Get the Socket to worker
			Socket s = workers.poll();
			if(s!=null) {
				//Set request status to processing 
				synchronized (lock) {
					mem.get(r.password).requestStatus = Request.Status.PROCESSING;
					lock.notifyAll();
				}
				//Start worker thread with request and access to queue of workers and shared memory, and let thread know that request wasnt split
				WorkerThread worker = new WorkerThread(s, mem, r, lock, workerLock, workers, false,"");
				worker.start();	
			}
			workerLock.notifyAll();
		}
	}
	
	//Strategy Three of assignment - split work among workers
	//Input - Request
	private void strategyThree(Request r) {
		synchronized (workerLock) {
			//wait until all workers are available 
			while(workers.size() != numOfWorkers){
				try {
					workerLock.wait();
				} catch (InterruptedException e) {
					System.out.println("Worker Wait Interupted in Strategy Three: "+e.getMessage());
				}
			}
			
			//Once available, split string
			
			//Instantiate Array buffer
			String[] buff = new String[numOfWorkers];
			for(int i = 0; i < buff.length;i++) {
				buff[i] = "";
			}
			
			String temp = r.message; 	//String to process
			
			//Split the string so that work is divided as evenly as possible
			//a simple temp/numOfWorkers wouldnt work as it doesnt account for integer division
			int i = 0;
			while(i<temp.length()) {
				buff[i%numOfWorkers] += temp.charAt(i);
				i++;
			}
			
			//Set the request status to processing and set the number of workers working on request
			synchronized (lock) {
				mem.get(r.password).requestStatus = Request.Status.PROCESSING;
				mem.get(r.password).numOfWorkers = numOfWorkers;
				lock.notifyAll();
			}
			
			//Split the work among the workers
			i = numOfWorkers;
			while(i !=0) {
				//get worker socket and start thread with partial data
				Socket s = workers.poll();
				WorkerThread worker = new WorkerThread(s, mem, r, lock, workerLock, workers,true,buff[i-1]);
				worker.start();
				i--;
			}
			workerLock.notifyAll();
		}
	}
	
	//Read new request from shared memory
	//Output - request
	private Request readNewRequest() {
		Request r = null; 
		//Get all requests in memory
		Object[] requests = mem.values().toArray();
		Boolean notFound = true; 	//Boolean to see if a new request is found
		int i = 0;					//Counter to see how many requests we have gone through
		int size = mem.size(); 		//Number of requests in memory
		
		//Loop through memory until the end is reached or a new request is found
		while(notFound && i < size) {
			//get request
			r = (Request)requests[i];
			
			//If this is a new request, stop loop and send back
			if(r.requestStatus == Request.Status.NEW) {
				notFound = false;
			} else {
				//Else continue
				i++;
				r= null;
			}
		}
		return r;
		
	}

	//Returns the Alphabet Character of an integer
	//Input - int number
	//Output - char letter
	public static char Alpha(int num) {
		return (char) (num + 96);
	}

	//Returns the integer equivalent of an character
	//Input - char letter
	//Output - int number
	public static int Pos(char a) {
		return (int) a - 96;
	}

	//Find the averagely occurring character in a string
	//Input - String message
	//Output - Char character
	public static char FrequentCharacterFinder(String t) {
		String s = t.replaceAll("[^a-zA-Z]", "");	//For string t, remove all characters that arent letters
		int l; 		//Length of t
		int sum = 0;	//Sum of all letters

		s = s.toLowerCase();
		l = s.length();

		//Calculate the sum
		for (int i = 0; i < l; i++) {
			sum = sum + Pos(s.charAt(i));
		}

		//Return the average character occurring 
		return (char) Alpha(sum / l);
	}
}
