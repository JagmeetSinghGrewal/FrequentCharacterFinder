/*
 * Jagmeet Singh Grewal
 * ServerClientThread
 * Desc: Thread that handles communications with client and inputs new requests and retrieves processed requests
 */
package Assignment;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;


public class ServerClientThread extends Thread {
	//Properties
	private Socket s1;								//Client Socket
	private HashMap<String, Request> sharedMemory;	//Shared Memory
	private Object lock;							//Object to synchronise shared memory

	public ServerClientThread(Socket s1, HashMap<String, Request> sharedMemory, Object lock) {
		this.s1 = s1;
		this.sharedMemory = sharedMemory;
		this.lock = lock;
	}
	
	public void run() {
		//Options menu
		String message = "-------------------------------------- \n" 
				   + "Welcome to CharFreqServer\n"
				   + "Type any of the following options: \n"
				   + "NewRequest  <INPUTSTRING>\n"
				   + "StatusRequest  <passcode>\n" 
				   + "Exit\n" 
				   + "--------------------------------------";
		try {
			Boolean run = true;		//Boolean to determine if communications continue
			
			//Setup output and input streams
			DataOutputStream dos = new DataOutputStream(s1.getOutputStream());
			DataInputStream dis = new DataInputStream(s1.getInputStream());
		
			while (run) {
				//Send menu and receive user option
				dos.writeUTF(message);
				String[] userMessage =  new String(dis.readUTF()).split(" ");

				//Determines what option was chosen and how to process
				switch (userMessage[0]) {
					case "NewRequest":
						//If new request, do the following
						if(isValidParameter(userMessage)) {
							//If we have valid parameters do the following
							
							String temp = userMessage[1]; 	//New string to process
							if(temp.replaceAll("[^a-zA-Z]", "").length() != 0) { 
								//If valid string, then make request object, generate password and append to shared memory
								String password = generatePassword();
								appendRequest(new Request(password, Request.Status.NEW, userMessage[1]));
								dos.writeUTF("Your Request password is: " + password); 	//Tell user the password
								dos.flush();
							} else {
								dos.writeUTF("Please Enter a valid parameter");
								dos.flush();
							}
						} else {
							dos.writeUTF("Please Enter a valid parameter");
							dos.flush();
						}
						break;
					case "StatusRequest":
						//If checking the status of a request
						if(isValidParameter(userMessage)) {
							//Print Request Status
							String status = printStatus(userMessage[1]);
							dos.writeUTF("The Status of your request is: " + status);
							dos.flush();
						} else {
							dos.writeUTF("Please Enter a valid parameter");
							dos.flush();
						}
						break;
					case "Exit":
						//Exit from loop
						run = false;
						dos.writeUTF("Goodbye");
						dos.flush();
						break;
					default:
						//Invalid option
						dos.writeUTF("INVALID OPTION! PLEASE SPECIFY OPTION AGAIN");
						dos.flush();
						break;
				}
			}
				//Close input, output streams and socket
			 	dos.close(); 
			 	dis.close(); 
				s1.close();
		} catch (IOException e) {
			System.out.println("Connection Lost With Client");
			try {
				s1.close();
			} catch (IOException e1) {
				System.out.println("Error with Closing Socket: "+e1.getMessage());
			}
		}
	}

	//Generate Password for a new Request
	//Output String password
	private String generatePassword() {
		long timestamp = new Date().getTime();		//Get timestamp
		long port = (long)s1.getLocalPort();		//Get port number
		long num = (long) (Math.random() *100);		//Get random number between 0 to 99
		String password = timestamp+port+num+"";	//Password
		
		synchronized (lock) {
			//Generate a unique password, by checking if it exists in shared memory
			while(sharedMemory.get(password) != null) {
				timestamp = new Date().getTime();
				port = (long)s1.getLocalPort();
				num = (long) (Math.random() *100);
				password = timestamp+port+num+"";
			}
			
			lock.notifyAll();
		}
		
		return password;
	}
	
	//Append new request to shared memory
	//Input - Request 
	private void appendRequest(Request request) {

		//Append new request with password as key
		synchronized (lock) {
			sharedMemory.put(request.password, request);
			lock.notifyAll();
		}		
	}

	//Print the current status of a request
	//Input - String password
	//Output - String - Status
	private String printStatus(String password) {
		synchronized (lock) {
			//Get the request with password
			Request r = sharedMemory.get(password);
			if(r != null) {
				//Based on status, send a corresponding message
				switch(r.requestStatus) {
					case NEW:
						return " Hasn't been processed yet";
					case PROCESSING:
						return " Currently Being Processed";
					case DONE:
						return " Processing Complete(Result:" + r.output+")";
				}
			}
			lock.notifyAll();
		}
		return "Request Not Found";
	}

	//Check whether user input was valid
	//Input String[] user input
	//Output - Boolean valid or not
	private boolean isValidParameter(String[] userMessage) {
		return !(userMessage.length < 2);
	}
}
