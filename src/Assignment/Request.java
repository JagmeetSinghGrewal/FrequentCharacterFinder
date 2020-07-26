/*
 * Jagmeet Singh Grewal
 * 444648
 * Request
 * Desc: Request Object
 */
package Assignment;

import java.util.LinkedList;

public class Request {
	public enum Status {PROCESSING, NEW, DONE};
	
	//Request Properties
	public String password;		
	public Status requestStatus;
	public String message;		//Client String
	public char output;		//Final Result
	public int numOfWorkers = 0;	//Number of workers processing this request
	public LinkedList<Character> buffer = new LinkedList<Character>();	//Buffer to hold partial results if multiple workers involved
	
	//Constructor 
	public Request(String password, Status status, String message) {
		this.message = message;
		this.password = password;
		this.requestStatus = status;
	}
}
