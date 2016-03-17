package ie.gmit.serverFunctional;

//Package imports
import ie.gmit.clientserver.Customer;
import ie.gmit.clientserver.House;
import ie.gmit.clientserver.Reminder;
import ie.gmit.clientserver.RentableHouse;
import ie.gmit.clientserver.SellableHouse;
import ie.gmit.clientserver.StaffMember;
import ie.gmit.clientserver.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/*Server class contains general methods used by the Server*/
public class Server 
{
	//Instance variables
	public Socket clientSocket;				//socket for connection with the client
	private ObjectOutputStream out;			//input and output streams used for client connection
	private ObjectInputStream in;
	private int signal;						//used to retrieve signals from client indicating what to do
	private User user;						//User object to hold users sent from the client
	private StaffMember staff;				//Used to send and recieve staff member objects
	private User[] oldAndUpdatedUser; 		//User array used to hold old and update users
	private int searchID;					//Holds id used to search for records
	private String message;					//Holds messages from client
	private House house;					//Used to send/recieve house objects to and from the client
	private RentableHouse rHouse;		//Used to send/recieve rentable house objects to and from the client
	private SellableHouse sHouse;		//Used to send/recieve sellable house objects to and from the client
	private Customer customer;			//Used to send/recieve customer objects to and from the client
	private Reminder reminder;
	
	//Constructor for server class accepts a socket for the connection with the client
	public Server(Socket clientSocket)
	{
		//Accept client socket for in and out connection with the client
		this.clientSocket = clientSocket;
		
		try 
		{
			//create input and output streams for the client connection
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
		    in = new ObjectInputStream(clientSocket.getInputStream());
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end class constructor
	
	//used to send signals back to the client, eg 0 or 1 (Success or Unsuccessful)
	public void sendMessage(Object message)
	{
		try
		{
			out.writeObject(message);	//sending the message
			out.flush();				//clear the connection
		}
		catch(IOException e)
		{
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("There is an IO issue with sending a message, you may have entered some incorrect information - please try again");
		}
	}//end sendMessage()
	
	//Used to retrieve signals from the client 
	public int getClientsSignal()
	{
		try 
		{
			signal = (Integer)in.readObject(); //Read the signal from the client
		} 
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return signal;	//return the signal
	}//end getClientsSignal()
	
	//Used to retrieve search id from the client 
	public int getSearchID()
	{
		try 
		{
			searchID = (Integer)in.readObject(); //Read the signal from the client
		} 
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return searchID;	//return the signal
	}//end getSearchID()
	
	//Used to retrieve user objects sent from the client
	public User getUserInformation()
	{
		try 
		{
			user = (User)in.readObject(); //Retrieve the user object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
		
		return user;					//return it
	}//end getUserInformation()
	
	//Used to retrieve multiple user objects from the client, for the old user and user to update it
	public User[] getUserUpdateInformation()
	{
		oldAndUpdatedUser = new User[2]; //Instantiate an array which holds to users
		try 
		{
			oldAndUpdatedUser[0] = (User)in.readObject(); //retrieve old user
			oldAndUpdatedUser[1] = (User)in.readObject(); //retrieve user to update old user
		} 
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return oldAndUpdatedUser; //return the array of old/new user
	}//end getUserUpdateInformation()
	
	//Used to retrieve staff objects sent from the client
	public StaffMember getStaffInformation()
	{
		try 
		{
			staff = (StaffMember)in.readObject(); //Retrieve the staff member object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
			
		return staff;				//return it
	}//end getStaffInformation()
	
	//Used to send staff object to the client
	public void sendStaffInformation(StaffMember staff)
	{
		try 
		{
			out.writeObject(staff);		//sending the message with staff member object
			out.flush();				//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendStaffInformation()
	
	//Used to send a customer object to the client
	public void sendCustomerInformation(Customer customer)
	{
		try 
		{
			out.writeObject(customer);		//sending the message containing customer object
			out.flush();					//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendCustomerInformation()
	
	//Used to close connection with the client
	public void closeConnections()
	{
		try 
		{
			clientSocket.close();		//close the socket
			in.close();					//close input and output streams
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}//end closeConnections()
	
	//Gets the path where the source code is located
	public String getServersFilePath()
	{
		String s;
		Path currentRelativePath = Paths.get("");			  	//Create path object
		s = currentRelativePath.toAbsolutePath().toString(); 	//Get the path
		s = s.replace('\\', '/');							 	//Replace the \ with /
		return s;
	}//end getServersFilePath()
	
	//Create a user folder named after the users, user name
	public String createUserDirectory(String path, String username)
	{
		String usersDirectoryPath = path + "/" + username; 						//Create a path for the user
		File file = new File(usersDirectoryPath);								//File/Directory object
		File welcomeFile = new File(usersDirectoryPath + "/Welcome.txt"); 		//Path for welcome file
		FileWriter writer = null;
		
		//If the directory does not exist
		if(!file.exists())
		{
			file.mkdir();															//Then create it
			welcomeFile.getParentFile().mkdirs(); 									//Ensure that the parent directories exist before writing
			try 
			{
				welcomeFile.createNewFile();										//Create welcome file inside the directory
				writer = new FileWriter(welcomeFile);								//Access the file for writing
				writer.write("Hello " +username+ " welcome to the file server");	//write welcome message
				writer.close();
			} 
			catch (IOException e) 
			{
				//Output an error message thats human readable and one thats more specific 
				System.out.println();
				System.out.println(e.getMessage());
				System.out.println("There is an IO issue creating a directory or file - please try again");
			}
		}
		return usersDirectoryPath;		//Return the path of the directory that was created.
	}//end createUserDirectory()
	
	//get the name of the file uploaded from the client
	public String getUploadedFileName()
	{
		String uploadFileName = "";
		
		try 
		{
			uploadFileName = (String)in.readObject();	//Wait/Get name of file from client
		} 
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return uploadFileName; //return information about the name of the file for upload
	}//end getUploadedFileName()
	
	//get the size of the file uploaded from the client
	public long getUploadedFileSize()
	{
		long fileSize = 0;
		
		try 
		{
			fileSize = (Long)in.readObject();	//Wait/Get size of uploaded file from client
		} 
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return fileSize; //return information about size of uploaded file
	}//end getUploadedFileSize()
	
	//Gets and returns all the files in a directory
	public ArrayList<String> listDirectoriesFiles(File file)
	{
		ArrayList<String> directoryFiles = new ArrayList<String>();		//Create array list of strings to hold the files
			
		for(File f : file.listFiles())									//for each file in the list of files for the directory
		{
			if(f.isFile())
			{
				directoryFiles.add(f.getName());						//Get its name and add it to the array list
			}
		}
			
		return directoryFiles;											//Return the array list
	}
	
	//get the name of the file sent from the client
	public String getDownloadFileName()
	{
		String downloadFileName = "";
			
		try 
		{
			downloadFileName = (String)in.readObject();	//Wait/Get name of file from client
		} 
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
			
		return downloadFileName; //return information about the name of the file for upload
	}//getDownloadFileName()
	
	//Get a message (string) from the client
	public String getClientMessage()
	{
		try 
	 	{
	 		message = (String)in.readObject();//Wait, then receive the message string
		} 
	 	catch(ClassNotFoundException e) 
	 	{
			e.printStackTrace();
		} 
	 	catch (IOException e) 
	 	{
			e.printStackTrace();
		}
	 		
	 	return message;	//return the message
    }//end getClientMessage()
	
	//Gets a House object containing data sent from the client
	public House getHouseInformation()
	{
		try 
		{
			house = (House)in.readObject(); //Retrieve the house object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
		
		return house;					//return it
	}//end getHouseInformation()
	
	//Gets a customer object containing data sent from the client
	public Customer getCustomerInformation()
	{
		try 
		{
			customer = (Customer)in.readObject(); //Retrieve the house object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
			
		return customer;					//return it
	}//end getCustomerInformation()
	
	//send a house object to the client
	public void sendHouseInformation(House house)
	{
		try 
		{
			out.writeObject(house);		//sending the message
			out.flush();				//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendHouseInformation()
	
	//Gets a RentableHouse object containing data sent from the client
	public RentableHouse getRentableHouseInformation()
	{
		try 
		{
			rHouse = (RentableHouse)in.readObject(); //Retrieve the house object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
			
		return rHouse;					//return it
	}//end getRentableHouseInformation()
	
	//send a rentable house object to the client
	public void sendRentableHouseInformation(RentableHouse rHouse)
	{
		try 
		{
			out.writeObject(rHouse);		//sending the message
			out.flush();				//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendRentableHouseInformation()
	
	//Gets a SellableHouse object containing data sent from the client
	public SellableHouse getSellableHouseInformation()
	{
		try 
		{
			sHouse = (SellableHouse)in.readObject(); //Retrieve the house object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
		
		return sHouse;					//return it
	
	}//end getSellableHouseInformation
	
	//send a sellable house object to the client
	public void sendSellableHouseInformation(SellableHouse sHouse)
	{
		try 
		{
			out.writeObject(sHouse);		//sending the message
			out.flush();				//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendSellableHouseInformation()
	
	//Used to retrieve reminder objects sent from the client
	public Reminder getReminderInformation()
	{
		try 
		{
			reminder = (Reminder)in.readObject(); //Retrieve the reminder object
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}
			
		return reminder;					//return it
	}//end getReminderInformation()
	
	//send a reminder object to the client
	public void sendReminderInformation(Reminder reminder)
	{
		try 
		{
			out.writeObject(reminder);		//sending the message
			out.flush();				//clear the connection
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}//end sendSellableHouseInformation()
}//end Server class
