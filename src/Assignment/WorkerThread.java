/*
 * Jagmeet Singh Grewal
 * WorkerThread
 * Desc: This file is the thread that handles the communication with the workers by sending 
 * strings and later aggregating and storing computation result
 */
package Assignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class WorkerThread extends Thread{
	private Socket socket;					//Worker Socket
	private HashMap<String, Request> mem;	//Shared memory
	private Request request;				//Request to process
	private Object lock;					//Object to synchronise shared memory on
	private Object workerlock;				//Object to synchronise workers queue on
	private Queue<Socket> workers;			//Workers queue
	private Boolean split;					//Boolean to check whether the request was split
	private String message;					//String to process

	//Constructor
	public WorkerThread(Socket socket, HashMap<String, Request> mem, Request request, Object lock, Object workerLock, Queue<Socket> workers, Boolean split, String message) {
		this.socket = socket;
		this.mem = mem;
		this.request = request;
		this.lock = lock; 
		this.workerlock = workerLock;
		this.workers = workers;
		this.split = split;
		this.message = message;
	}
	
	public void run() {
		try {
			//Setup input and output streams for socket
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			char result; 	//result retrieved from worker
			
			//If the request message wasnt split, the send whole message to worker, else send portion
			if(!split) {
				dos.writeUTF(request.message);
				dos.flush();
			} else {
				dos.writeUTF(message);
				dos.flush();
			}

			//Retrieve result from worker
			result=dis.readUTF().charAt(0);

			synchronized (lock) {
				//If request wasnt split, then put result to shared memory and set status to done
				if(!split) {
					mem.get(request.password).output = result;
					mem.get(request.password).requestStatus = Request.Status.DONE;
				} else {
					//If request was split, then add result to shared memory
					if(mem.get(request.password).numOfWorkers <=1) { //If last worker
						//Decrement workers currently working on request
						mem.get(request.password).numOfWorkers--;
						
						//Aggregate all the results and get the total sum
						int sum = Pos(result);
						for(char c : mem.get(request.password).buffer) {
							sum += Pos(c);
						}
						
						//Find average of buffered results and add it to shared memory
						//set the status to done, reset buffer
						char r = Alpha(sum/(mem.get(request.password).buffer.size()+1));
						mem.get(request.password).output = r;
						mem.get(request.password).requestStatus = Request.Status.DONE;
						mem.get(request.password).buffer = new LinkedList<Character>();

					} else {
						//if this isnt last worker, then decrement number of workers working on this request and add your result to buffer
						mem.get(request.password).numOfWorkers--;
						mem.get(request.password).buffer.add(result);
					}
				}
				lock.notifyAll();
			}
			//Add worker back to worker Queue
			synchronized (workerlock) {
				workers.add(socket);
				workerlock.notifyAll();				
			}

		} catch(IOException e) {
			//If worker disconnected, close server
			System.out.println("Connection Lost to Worker ("+ e.getMessage()+")");
			System.exit(1);
			
		}
	}
	
	//Returns the Alphabet Character of an integer
	//Input - int number
	//Output - char letter
	public char Alpha(int num) {
		return (char) (num + 96);
	}

	//Returns the integer equivalent of an character
	//Input - char letter
	//Output - int number
	public int Pos(char a) {
		return (int) a - 96;
	}
}
