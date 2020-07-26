/*
 * Jagmeet Singh Grewal
 * 444648
 * MyCharFreqWorker
 * Desc: Worker File that computes the average character in a given string
 */
package Assignment;

import java.io.*;
import java.net.*;

public class MyCharFreqWorker {
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

	public static void main(String[] args) {
		//Exit if insufficient parameters passed
		if(args.length  != 2) { 
			System.out.println("Error: Insufficient Parameters");
			System.out.println("USAGE: java MyCharFreqWorker ServerIP PortNumber ");
			System.exit(1);
		}
		
		
		
		Socket s1;
		try {
			String message = "";	//String Message from server
			int port = Integer.parseInt(args[1]);
			String host = args[0];
			//Connect to Server
			s1 = new Socket(host, port);
			DataInputStream dis = new DataInputStream(s1.getInputStream());
			DataOutputStream dos = new DataOutputStream(s1.getOutputStream());
			
			//Process work given by server
			while(true) {
				message = new String(dis.readUTF());	//Receive string to process
				String c = String.valueOf(FrequentCharacterFinder(message));	//process string
				dos.writeUTF(c); //Return to server the result 
				dos.flush();
			}
			
		} catch (UnknownHostException e) {
			System.out.println("Host is Unknown: " +e.getMessage());
		} catch(ConnectException e) {
			System.out.println("Server Not Available");
		} catch(BindException e) {
			System.out.println("Bind Error:"+ e.getMessage());
		} catch(SocketException e) {
			System.out.println("Server can't accept request, error in connection("+e.getMessage()+")");
		} catch (IOException e) {
			System.out.println("Data Streams have lost connection ("+e.getMessage()+")");
		} catch (NumberFormatException e) {
			System.out.println("Not a valid Port Number: ("+e.getMessage()+")");
		} catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}  
		
		
	}
}
