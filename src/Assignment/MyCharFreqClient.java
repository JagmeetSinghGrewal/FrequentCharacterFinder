/*
 * Jagmeet Singh Grewal
 * MyCharFreqClient
 * Desc: Client program that connects to server
 */
package Assignment;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MyCharFreqClient {
	public static void main(String[] args) {
		//Stop if insufficient parameters
		if(args.length<2) {
			System.out.println("Error: Insufficient Parameters");
			System.out.println("USAGE: java MyCharFreqClient ServerIP PortNumber");
			System.exit(1);
		}
		
		try {
			String message = "";	//Message from Client
			String st = "";		//String message from Server
			Scanner sc = new Scanner(System.in);
			int port = Integer.parseInt(args[1]);
			String host = args[0];
			
			//Establish connection with Server
			Socket s1 = new Socket(host, port);
			DataInputStream dis = new DataInputStream(s1.getInputStream());
			DataOutputStream dos = new DataOutputStream(s1.getOutputStream());

			while(true) {
				//Print Options Menu
				st = new String (dis.readUTF());
				System.out.println(st);				
				
				//Take User Input and send to server
				message = sc.nextLine();
				dos.writeUTF(message);
				dos.flush();
				
				//Receive Reply From Server
				st = new String (dis.readUTF());
				System.out.println(st);
				
				//If Server Reply was Goodbye, session close
				if(st.equals("Goodbye")) {
					break;
				}
			}

			//Close Input, output, scanner and socket
			dos.close();
			dis.close();	
			sc.close();
			s1.close();
			
			
		} catch (UnknownHostException e) {
			System.out.println("Host is Unknown: " +e.getMessage());
		} catch(ConnectException e) {
			System.out.println("Server Not Available");
		} catch(BindException e) {
			System.out.println("Bind Error:"+ e.getMessage());
		} catch(SocketException e) {
			System.out.println("Server can't accept request("+e.getMessage()+")");
		} catch (IOException e) {
			System.out.println("Input output error: ("+e.getMessage()+")");
		} catch (NumberFormatException e) {
			System.out.println("Not a valid Port Number: ("+e.getMessage()+")");
		}
	}
}
