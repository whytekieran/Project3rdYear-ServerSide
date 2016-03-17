package ie.gmit.serverMain;

//Package Imports
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*ServerSideApplicationMain class is the main method of the server side program and runs the server application*/
public class ServerSideApplicationMain 
{
	@SuppressWarnings("resource")
	//The main method
	public static void main(String[] args) throws IOException 
	{
		int clientId = 0; 											//Id of the client increments each new connection
		ServerSocket appServerSocket = new ServerSocket(2004,10);	//Creating a server socket
	    
	    //Continuous loop
	    while(true) 
	    {
	      ++clientId;									  //Increment client id
	      Socket clientSocket = appServerSocket.accept(); //Wait for a client trying to connect
	      ClientRequestThread clientThread = new ClientRequestThread(clientSocket, clientId); //Spawn client thread
	      clientThread.start(); //Run the thread, individual thread for each client connection.
	      						//Then loop over and listen again for the next client.
	    }
	}//End Main
}//End Class
