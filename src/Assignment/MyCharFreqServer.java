/*
 * Jagmeet Singh Grewal
 * MyCharFreqServer
 * Desc: Server that clients and workers connect to. 
 */
package Assignment;
import java.net.*;
import java.util.HashMap;
public class MyCharFreqServer {
	//Properties 
	private static HashMap<String,Request> sharedMemory = new HashMap<String, Request>(); 	//Shared Memory
	private static Object lock = new Object();												//Object to synchronise shared memory on
	
	public static void main(String[] args){
		int portWorker;
		int numOfWorkers;
		//Exit program if insufficient parameters
		if(args.length!=3) {
			System.out.println("Error: Insufficient Parameters");
			System.out.println("USAGE: java MyCharFreqServer PortNumberClient NumOfWorkers PortNumberWorker");
			System.exit(1);
		}
		
		portWorker = Integer.parseInt(args[2]);		//Port from workers connect to
		numOfWorkers = Integer.parseInt(args[1]);	//Number of workers that server will use
		
		//Start thread that handles worker communication
		ServerWorkerThread workerThread = new ServerWorkerThread(portWorker, numOfWorkers, sharedMemory, lock);
		workerThread.start();
		
		try {
			//Port for client to connect to
			int portClient = Integer.parseInt(args[0]);
			ServerSocket s = new ServerSocket(portClient);
			
			
			while(true) {
				//Accept connection from client and start a client thread
				Socket s1 = s.accept();
				ServerClientThread client = new ServerClientThread(s1, sharedMemory, lock);
				client.start();	
			}

		} catch(ConnectException e) {
			System.out.println("Connection Error: "+ e.getMessage());
		}  catch(BindException e) {
			System.out.println("Bind Error:"+ e.getMessage());
		} catch(SocketException e) {
			System.out.println("Socket Error: "+e.getMessage());
		} catch (NumberFormatException e) {
			System.out.println("Not a valid Port Number: ("+e.getMessage()+")");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}  
		
	}
}	
