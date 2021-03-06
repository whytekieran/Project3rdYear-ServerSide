//Package
package ie.gmit.serverMain;

//Package imports
import ie.gmit.clientserver.Customer;
import ie.gmit.clientserver.House;
import ie.gmit.clientserver.Reminder;
import ie.gmit.clientserver.RentableHouse;
import ie.gmit.clientserver.SellableHouse;
import ie.gmit.clientserver.StaffMember;
import ie.gmit.clientserver.User;
import ie.gmit.serverDatabase.ManagementApplicationDatabase;
import ie.gmit.serverFunctional.DateValidator;
import ie.gmit.serverFunctional.Server;
import ie.gmit.serverFunctional.TimeValidator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/*ClientRequestThread class extends thread and hence is a thread. Each time a client makes a connection
 with the server the server main spawns off one of these threads. There is one thread for each
 client connection*/
public class ClientRequestThread extends Thread
{
	//Class instance variables
	private boolean authenticated = false;			//Boolean for authentication
	private int signal;								//Holds signals sent from the client
	private int id;									//Holds the clients id
	private User user;								//Holds User data sent from the client
	private User[] oldAndUpdatedUser;				//User array used to hold old and new user (for an update)
	private boolean running = true;					//Controls the do-while loop 
	private boolean userCreate = true;				//Sets when user creation is allowed
	private boolean updateUser = false;				//Sets when update of user is allowed
	private boolean deleteUser = false;				//Sets when deletion of user is allowed
	private boolean foundStaffMember = false;		//Holds if staff member was found or not
	private boolean staffMemberExists = false;		//Holds result of staff member check
	private Server server;							//Server object has general functions used by server
	private String userID;							//Holds user id
	private int searchID;							//Used for search particular rows
	private boolean staffDeletion = false;			//boolean for staff deletion allowed
	private String username;						//Holds a username
	private String serversFilePath;					//Holds the path to this source code location (server)
	private String userRootDirectoryPath;			//Path for users root directory
	private int proceed;							//Determines if server can carry on with task
	private String uploadName;						//Holds uploaded file name
	private long uploadSize;						//Holds upload file size
	private String uploadPath;						//Holds upload file path
	private String downloadName;					//Holds name of file for download
	private ArrayList<StaffMember> staffMembers;	//List of staff members
	private ArrayList<String> files;				//Holds list of files in users online directory
	private String downloadPath;					//Holds path of file for download
	private boolean downloadAllowed = false;		//Determines if a download is allowed
	private String userDirName;						//Name of users root directory (equals username)
	private String compFileUploadName;				//Path of file for upload from computer
	private long compFileUploadSize;				//Path of file for upload from computer
	private boolean receiveCompFile = false;		//True if we can recieve file from computer
	private String onlineFileSendName;				//Name of online file for sending
	private boolean userExist;						//true if a user exists
	private String onlineFileRemoveName;			//Name of online file to be removed
	private ManagementApplicationDatabase db;		//Database Application object for using apps db
	private boolean foundHouse = false;				//True if house is found
	private boolean houseDeletion = false;			//True if house can be deleted
	private ArrayList<House> houses;				//List of houses
	private boolean houseExists = false;			//True if house exists
	private boolean rentHouseExists;				//Booleans for transactions
	private boolean buyEstateAgentExists;
	private boolean rentEstateAgentExists;
	private boolean rentCustExists;
	private boolean buyCustExists;
	private boolean noRentDateOverlap;
	private int agentID;							//Holds estate agent id
	private int custID;								//Holds customer id
	private boolean startDateValid;					//makes sure start date is a valid date
	private boolean endDateValid;					//makes sure end date is a valid date
	private boolean buyHouseExists;					//True if a buy house exists
	private DateValidator dateValidator = new DateValidator(); //Used to validate dates and check for date overlap
	private TimeValidator timeValidator = new TimeValidator(); //Used to validate times in 24hr format
	private boolean rentTransactionExists = false;		//True if rent transaction exists
	private boolean sellTransactionExists;				//True if sell transaction exists
	private boolean sellHouseDelete = false;			//True if sell house can be deleted
	private boolean rentHouseDelete = false;			//true if rent house can be deleted
	private boolean rHouseExists = false;				//True if rent house exists
	private ArrayList<RentableHouse> rHouses;			//List of rent houses
	private ArrayList<Integer> rentIDs;					//Lists of rent, buy, customer, agent ids
	private ArrayList<Integer> estateAgentIDs;
	private ArrayList<Integer> custIDs;
	private ArrayList<Integer> buyIDs;
	private ArrayList<SellableHouse> sHouses;			//List of sellable houses
	private boolean sHouseExists = false;				//True if sellable house exists
	private boolean custDelete = false;					//True if customer can be deleted
	private boolean foundCustomer = false;				//True if customer was found
	private boolean foundViewCust = false;				//True if found specific customer in view
	private ArrayList<Customer> customers;				//List of customers
	private boolean staffExists = false;				//True if specific staff member exists
	private boolean reminderDateValid;					//True if reminder date is a valid date
	private boolean reminderTimeValid;					//True if reminder time is a valid time
	private boolean foundReminder = false;				//True if reminder was found
	private ArrayList<Reminder> remindersList;		    //List of reminders
	private boolean validSearchDate;					//Determines if date used for searching is valid
	private String houseType;							//Holds type of house being searched for r or b
	private String fName;								//Hold first names sent from client for searching
	private String LName;								//Hold last names sent from client for searching
	private String address;								//Holds address sent from client for searching
	private String county;								//Holds county sent from client for searching
	private String operator;							//Holds =, < and > operations 
	private StaffMember staff;							//Holds staff members data
	private House house;								//Holds house data
	private RentableHouse rHouse;						//Holds rentable house data
	private SellableHouse sHouse;						//Holds sellable house data
	private Customer customer;							//Holds customer data
	private Reminder reminder; 							//Holds reminder data
	private boolean isWhitespace = false;				//True if a word contains whitespace
	private Scanner fileReader;							//Used to read txt files
	ArrayList<Reminder> reminderList;
	Reminder rListMember;
	private PrintWriter fileWriter;						//Used to write to file
	
	//SQL Variables
	private Connection con;							//Connection object to get a connection
	private PreparedStatement statement;			//Used to form an SQL statement
	private ResultSet result;						//Holds results of a select statement
	
	//Constructor for ClientRequestThread accepts socket and id for the client
	ClientRequestThread(Socket clientSocket, int id) 
	{
		  this.id = id; 									//set client id
		  System.out.println("Client "+id+" Connected");    //output a connected message
		  server = new Server(clientSocket); 				//pass client socket into server object (provides server functionality)
		  db = new ManagementApplicationDatabase(server);
	}

	/*The run method is like the main method of the thread, whenever we call .start() on a thread this 
	  method is executed. So when we get a client connection and its been set up.........*/
	@SuppressWarnings("resource")
	public void run()
	{
		do
		{
			try
			{ 
				//Listen for a signal from the client, indicates the task to be done
				signal = server.getClientsSignal();
				
				//Perform a switch on the signal
				switch(signal)
				{
					//RECEIVE/AUTHENTICATE THE AUTHENTICATION INFO (SERVER SIDE)
					case 1:
						user = server.getUserInformation(); //Get user object from the client
						result = db.getResults("SELECT Username, Password, EmploymentType FROM users");//execute select statement and return results
					
						/*Loop over the results and see if any match the User authentication information sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(user.getUsername()) && result.getString(2).equalsIgnoreCase(user.getPassword())
									&& result.getString(3).equalsIgnoreCase(Character.toString(user.getStaffType())))
							{
								//if we find a match it means we have a valid user authentication
								authenticated = true; //authentication is now true
								break;	
							}
						}
						
						if(authenticated == true) //If authenticated send 1 back to the client.
						{
							username = user.getUsername();
							//Gets the path of the current file(this code file)
							serversFilePath = server.getServersFilePath();
							//Creates a new directory for the user if none already exist and also add a Welcome.txt file into it
							//then returns the path of the users online file directory which it created.
							userRootDirectoryPath = server.createUserDirectory(serversFilePath, username);
							
							int rows = 0;//open the reminder file for reading
							fileReader = new Scanner(new FileReader(userRootDirectoryPath + "/Reminder.txt"));
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//date formatter
							Date todaysDate = new Date();//date object
							
							while(fileReader.hasNext())///loop over file rows
							{
								rListMember = new Reminder();
									
								rListMember.setReminderID(fileReader.nextInt());//get data 
								rListMember.setSubject(fileReader.next());
								rListMember.setDate(fileReader.next());
								rListMember.setTime(fileReader.next());
								String[] words = fileReader.nextLine().split("\\s");//multi word desc is split into array
								String desc = "";
								for(int i = 0; i < words.length; ++i)
								{
									desc += words[i]+" ";//array combined into single string
								}
								rListMember.setDesc(desc);//add single string as the description
									
								if(rListMember.getDate().equals(dateFormat.format(todaysDate)))//if reminder found for today increment counter
								{						
									++rows;
								}
								else//otherwise move to next iteration
								{
									continue;
								}
							}//end read while loop
								
							fileReader.close();//close reader
						
							//Send success signal
							server.sendMessage("1");
							//Send the amount of reminders found for today
							server.sendMessage(rows);
						}
						else if(authenticated == false)//If not send 0 and end loop so connection terminates
						{
							//send fail signal
							server.sendMessage("0");
						}
						break;
					case 2:
						running = false;	//Set loop control variable to false so loop breaks and program ends
						break;
						//CREATE A NEW USER (SERVER SIDE)
					case 3:
						user = server.getUserInformation(); //Get user object from the client
						result = db.getResults("SELECT Username FROM users");//execute select statement and return results
						/*Loop over the results and see if any match the user name sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(user.getUsername()))
							{
								//if we find a match it means we have a user name like this already
								userCreate = false; //so we can not create a user
								break;	
							}
							else
							{
								userCreate = true; //otherwise we can create this user
							}
						}
						
						result = db.getResults("SELECT Id, EmploymentType FROM staff");//execute select statement and return results
					    
						/*Loop over the results and see if any match the user name sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(user.getStaffID())) &&
									result.getString(2).equalsIgnoreCase(Character.toString(user.getStaffType())))
							{
								//if we find a match it means we have a user name like this already
								staffExists = true; //so we can not create a user
								break;	
							}
							else
							{
								staffExists = false; //otherwise we can create this user
							}
						}
						
						if(userCreate == true && staffExists == true)
						{
							db.executeStatement("INSERT INTO users (StaffID, Username, Password, EmploymentType) VALUES "
										+ "('"+user.getStaffID()+"', '"+user.getUsername()+"', '"+user.getPassword()+"', '"+Character.toString(user.getStaffType())+"')");
							
					    	
					    	server.sendMessage("1");
						}
					    else
					    {
					    	//send signal non-success back to client
					    	server.sendMessage("0");
					    }
						break;
						//UPDATING USER INFORMATION (SERVER SIDE)
					case 4:
						//Get user objects from the client returned as an array
						oldAndUpdatedUser = server.getUserUpdateInformation();//gets an array with two elements (old and new user data)
						result = db.getResults("SELECT Username, Password, EmploymentType FROM users");//execute select statement and return results
						
					    /*Loop over the results and see if anything matches the old User information sent
						  from the client*/
					    while(result.next())
						{
					    	//If we get a complete match we know this user exists
							if(result.getString(1).equalsIgnoreCase(oldAndUpdatedUser[0].getUsername()) && 
									result.getString(2).equalsIgnoreCase(oldAndUpdatedUser[0].getPassword()) &&
									result.getString(3).equalsIgnoreCase(Character.toString(oldAndUpdatedUser[0].getStaffType())))
							{
								updateUser = true; //If the user exists we can update the user
								break;			   //so break the loop
							}
							else //otherwise
							{
								updateUser = false; //we have not found a match and we cannot do an update
							}
						}
						
					    //If a match was found and update is possible
					    if(updateUser == true)
					    {
					    	//execute the statement
					    	db.executeStatement("UPDATE users SET Username = '" +oldAndUpdatedUser[1].getUsername()+ "', Password = '" +oldAndUpdatedUser[1].getPassword()+ "', "
									+ "EmploymentType = '" +Character.toString(oldAndUpdatedUser[1].getStaffType())+ "' WHERE Username = '" +oldAndUpdatedUser[0].getUsername()+"'");
					    	
						    //send signal of success back to the client
						    server.sendMessage("1");
					    }
					    else if(updateUser == false)//otherwise
					    {
					    	//send a fail signal back to the client
					    	server.sendMessage("0");
					    }
						break;
						//DELETING A USER (SERVER SIDE)
					case 5:
						user = server.getUserInformation();  //Get user object from the client
					    result = db.getResults("SELECT Username, Password, EmploymentType FROM users");//execute select statement and return results
					    
					    /*Loop over the results and see if anything matches the delete user information sent
						  from the client*/
					    while(result.next())
						{
					    	//We we get a match
							if(result.getString(1).equalsIgnoreCase(user.getUsername()) && 
									result.getString(2).equalsIgnoreCase(user.getPassword()) &&
									result.getString(3).equalsIgnoreCase(Character.toString(user.getStaffType())))
							{
								deleteUser = true;   //The user exists and can be deleted
								break;				 //break the loop
							}
							else //otherwise
							{
								deleteUser = false; //No match so this user does not exist, can not be deleted
							}
						}
					    
					    //If match was found and a delete is possible
					    if(deleteUser == true)
					    {
					    	//execute the statement
					    	db.executeStatement("DELETE FROM users WHERE Username = '" +user.getUsername()+ "'");
						    
						    //send success signal back to client
						    server.sendMessage("1");
					    }
					    else if(deleteUser == false)//otherwise
					    {
					    	//send fail signal back to the client
					    	server.sendMessage("0");
					    }
						break;
					case 6:
						//CREATING A STAFF MEMBER (SERVER SIDE)
						staff = server.getStaffInformation();       //Get user object from the client
					
						//Execute query that will insert new staff member and return his/her generated id (PK)
					    userID = db.insertThenGetKey("INSERT INTO staff (FirstName, LastName, Address, PPS, Salary, EmploymentType) VALUES "
								+ "('"+staff.getFirstName()+"', '"+staff.getLastName()+"', "+ "'"+staff.getAddress()+"', "
					    		  + "'"+staff.getPps()+"', "+ "'"+staff.getSalary()+"', "+ "'"+Character.toString(staff.getStaffType())+"')");
					    
					    if (Integer.parseInt(userID) != -1)//if its not null 
					    {
					    	 server.sendMessage(userID);//send it back to the client
							 server.sendMessage("1");  //then send success message
					    }
					    else//otherwise
					    {
					    	server.sendMessage("unsuccessful");//send messages indicating no success
					    	server.sendMessage("0");
					    }
						break;
						//FINDING A STAFF MEMBER(SERVER SIDE)
					case 7:
						staff = new StaffMember();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff");//execute select statement and return results
						
					    /*Loop over the results and see if anything matches the search id information sent
						  from the client*/
					   while(result.next())
						{
					    	//If we get a complete match we know this staff member exists
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//So add the found staff members data to a StaffMember object
								staff.setFirstName(result.getString(2));
								staff.setLastName(result.getString(3));
								staff.setAddress(result.getString(4));
								staff.setPps(result.getString(5));
								staff.setSalary(Double.parseDouble(result.getString(6)));
								staff.setStaffType(result.getString(7).charAt(0));
								foundStaffMember = true; //If the we can send back staff information
								break;			   //break the loop
							}
							else //otherwise
							{
								foundStaffMember = false; //we have not found a match and we cannot do an update
							}
						}
					    
					    if(foundStaffMember == true)//if we found a staff member
					    {
					    	server.sendMessage("1");					//send okay signal
					    	server.sendStaffInformation(staff);	//send the staff member
					    }
					    else if(foundStaffMember == false)//otherwise
					    {
					    	server.sendMessage("0"); //send not okay signal
					    }
						break;
					case 8:
						//UPDATING A STAFF MEMBER (SERVER SIDE)
						searchID = server.getSearchID();
						staff = server.getStaffInformation();
				
						//execute the statement
						db.executeStatement("UPDATE staff SET FirstName = '" +staff.getFirstName()+ "', LastName = '" +staff.getLastName()+ "', "
								+ "Address = '" +staff.getAddress()+ "', PPS = '" +staff.getPps()+ "', Salary = '"
					    		+staff.getSalary()+ "', EmploymentType = '" +Character.toString(staff.getStaffType())+ "' WHERE Id = '" +searchID+"'");//execute select statement and return results
					
					    //send okay signal
					    server.sendMessage("1");
					    break;
					case 9:
						//DELETING A STAFF MEMBER (SERVER SIDE)
						searchID = server.getSearchID();
						result = db.getResults("SELECT Id FROM staff");//execute select statement and return results
						
						/*Loop over the results and see if any match the staff member for deletion sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a staff member with this id
								staffDeletion = true; //delete is now allowed
								break;	
							}
							else
							{
								staffDeletion = false;//delete not allowed
							}
						}
					    
						if(staffDeletion == true)//if delete is allowed
					    {
						    db.executeStatement("DELETE FROM staff WHERE Id ="+searchID);
						    
						    server.sendMessage("1");//send okay signal
					    }
					    else if(staffDeletion == false)//otherwise
					    {
					    	server.sendMessage("0");//send not okay signal
					    }
						break;
					case 10:
						//SHOW ALL STAFF MEMBERS (SERVER SIDE)
						staffMembers = new ArrayList<StaffMember>();//array list to hold all staff members
						result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							staff = new StaffMember();
							
							staff.setId(Integer.parseInt(result.getString(1)));
							staff.setFirstName(result.getString(2));
							staff.setLastName(result.getString(3));
							staff.setAddress(result.getString(4));
							staff.setPps(result.getString(5));
							staff.setSalary(Double.parseDouble(result.getString(6)));
							staff.setStaffType(result.getString(7).charAt(0));
						
							staffMembers.add(staff);//add to list
						}
						
						server.sendMessage(staffMembers);//send back the array list of staff to the client
						break;
					case 11:
						//SEARCH FOR A STAFF MEMBER (SERVER SIDE)
						searchID = server.getSearchID();				   //Get search id from client
						staff = new StaffMember();  //object to hold found staff member
						
						result = db.getResults("SELECT Id FROM staff");//execute select statement and return results
						
						/*Loop over the results and see if any match the staff member for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a staff member with this id
								staffMemberExists = true; //staff member exists
								break;	
							}
							else//otherwise
							{
								staffMemberExists = false;//staff member does not exist
							}
						}
						
						if(staffMemberExists == true)//if the staff member exists
						{
							//execute query
							result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff WHERE Id="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into staff member object
							staff.setId(Integer.parseInt(result.getString(1)));
							staff.setFirstName(result.getString(2));
							staff.setLastName(result.getString(3));
							staff.setAddress(result.getString(4));
							staff.setPps(result.getString(5));
							staff.setSalary(Double.parseDouble(result.getString(6)));
							staff.setStaffType(result.getString(7).charAt(0));
							
							server.sendMessage("1");						//send okay signal
							server.sendStaffInformation(staff);	//send the object
						}
						else if(staffMemberExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 12:
						//RECIEVING UPLOADED FILE (SERVER SIDE)
						//Listen for a signal from the client, indicates the task to be done
						proceed = server.getClientsSignal();
						InputStream inStream;
						
						if(proceed == 1)
						{
							uploadName = server.getUploadedFileName();							//Receive name of the uploaded file from the client.
							uploadSize =  server.getUploadedFileSize();							//Receive size of the uploaded file from the client.
							serversFilePath = server.getServersFilePath();							//Get file path of the server
							uploadPath = serversFilePath + "/" + username + "/" +uploadName;	//Create path for the uploaded file
							
							byte[] fileByteArray = new byte[(int) uploadSize];			    //Create array of bytes the same length as the file.
							inStream = server.clientSocket.getInputStream();						//Open an input stream from the client
							FileOutputStream fileOutStream = new FileOutputStream(uploadPath);				//FileOutputStream to write bytes
							BufferedOutputStream buffedOutStream = new BufferedOutputStream(fileOutStream);	//Pass that into BufferedOutputStream (bytes to characters)
							int bytesRead = inStream.read(fileByteArray, 0, fileByteArray.length);		//Read bytes from server and pass into byte array,
							buffedOutStream.write(fileByteArray, 0, bytesRead);					//Write these bytes (buffered) to file.
							buffedOutStream.close();								//Close the BufferedOutputStream
							
							server.sendMessage("1");
						}
						break;
					case 13:
					case 16:
					case 18:
					case 26:
						//SENDING ALL FILE NAMES IN THE USERS ONLINE DIRECTORY (SERVER SIDE)
						//Create file object using the users online directory path
						File usersDirectory = new File(userRootDirectoryPath);
						//Pass it into a method that lists and returns all the files in the directory 
						//in an array list.
						files = server.listDirectoriesFiles(usersDirectory);
						//send array list of file names (strings) to the client
						server.sendMessage(files);
						break;
					case 14:
						//DOWNLOADING A FILE FROM THE USERS ONLINE DIRECTORY (SERVER SIDE)
						//Get name of file for download from the client
						downloadName = server.getDownloadFileName();
						//Create file object using the users online directory path
						File userDirectory = new File(userRootDirectoryPath);
						//Pass it into a method that lists and returns all the files in the directory 
						//in an array list.
						files = server.listDirectoriesFiles(userDirectory);
						
						for(int i = 0; i < files.size(); ++i)
						{
							if(downloadName.equals(files.get(i)))
							{
								downloadAllowed = true;
								break;
							}
							else
							{
								downloadAllowed = false;
							}
						}
						
						if(downloadAllowed == true)
						{
							server.sendMessage("1");
							downloadPath = serversFilePath + "/" + username + "/" +downloadName;
							File downloadFile = new File(downloadPath);		//Create file object for download
							server.sendMessage(downloadFile.length());		//Send its length to the client
							server.sendMessage(downloadFile.getName());		//Send its name to the client
							byte[] fileByteArray = new byte[(int) downloadFile.length()];					//Make byte array thats file length in size
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));	//Create buffered input stream for the file
						    bis.read(fileByteArray, 0, fileByteArray.length);						//Read file into an array of bytes
						    OutputStream os = server.clientSocket.getOutputStream();					//Get an output stream for the client
						    os.write(fileByteArray, 0, fileByteArray.length);						//Write array of bytes to the client
						    os.flush();
						}
						else if(downloadAllowed == false)
						{
							server.sendMessage("0");
						}
						break;
					case 15:
						//RECIEVING A FILE FROM USERS PC TO ANOTHER USER(SERVER SIDE)
						userDirName = server.getClientMessage();
						compFileUploadName = server.getUploadedFileName();
						compFileUploadSize = server.getUploadedFileSize();
						result = db.getResults("SELECT Username FROM users");	//execute statement to retrieve user names
						/*Loop over the results and see if any match the staff member for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equals(userDirName))
							{
								//if we find a match it means we have a directory with this name
								receiveCompFile = true; //staff member exists
								break;	
							}
							else//otherwise
							{
								receiveCompFile = false;//staff member does not exist
							}
						}
						
						if(receiveCompFile == true)
						{
							server.sendMessage("1");
							
							uploadPath = serversFilePath + "/" + userDirName + "/" +compFileUploadName;
							byte[] fileByteArray = new byte[(int) compFileUploadSize];			    //Create array of bytes the same length as the file.
							inStream = server.clientSocket.getInputStream();						//Open an input stream from the client
							FileOutputStream fileOutStream = new FileOutputStream(uploadPath);				//FileOutputStream to write bytes
							BufferedOutputStream buffedOutStream = new BufferedOutputStream(fileOutStream);	//Pass that into BufferedOutputStream (bytes to characters)
							int bytesRead = inStream.read(fileByteArray, 0, fileByteArray.length);		//Read bytes from server and pass into byte array,
							buffedOutStream.write(fileByteArray, 0, bytesRead);					//Write these bytes (buffered) to file.
							buffedOutStream.close();								//Close the BufferedOutputStream
						}
						else if(receiveCompFile == false)
						{
							server.sendMessage("0");
						}
						break;
					case 17:
						//COPYING FILE ON THE CURRENT USERS ONLINE DIRECTORY TO ANOTHER USERS ONLINE DIRECTORY (SERVER SIDE)
						userDirName = server.getClientMessage();				//get message from client
						onlineFileSendName = server.getUploadedFileName();		//get file name from client
							
						Class.forName("com.mysql.jdbc.Driver");
						//Open a connection to the database
						con = DriverManager.getConnection("jdbc:mysql://localhost:3306/managementsystem", "root", "");
						//prepare statement
						statement = con.prepareStatement("SELECT Username FROM users");
						//Execute the Query and get the returned results
						result = statement.executeQuery();
							
						/*Loop over the results and see if any match the staff member for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equals(userDirName))
							{
								//if we find a match it means we have a staff member with this id
								userExist = true; //staff member exists
								break;	
							}
							else//otherwise
							{
								userExist = false;//staff member does not exist
							}
						}
						
						File onlineFile = new File(userRootDirectoryPath +"/"+onlineFileSendName);
						
						if(onlineFile.exists() && userExist == true)
						{
							byte[] fileByteArray = new byte[(int) onlineFile.length()];					//Make byte array thats file length in size
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(onlineFile));	//Create buffered input stream for the file
						    bis.read(fileByteArray, 0, fileByteArray.length);						//Read file into an array of bytes
						    
						    FileOutputStream fileOutStream = new FileOutputStream(serversFilePath+"/"+userDirName+"/"+onlineFileSendName);	//Create FileOutputStream set to directory specified by client (user name of other user)	
							BufferedOutputStream buffedOutStream = new BufferedOutputStream(fileOutStream);		//Pass that into BufferedOutputStream (bytes to characters)
							buffedOutStream.write(fileByteArray, 0, fileByteArray.length);								//Write these bytes (buffered) to file.
							buffedOutStream.close();				
						    
						    server.sendMessage("1");
						}
						else
						{
							server.sendMessage("0");
						}
					break;
					case 19:
						//REMOVING A FILE FROM USERS ONLINE DIRECTORY (SERVER SIDE)
						onlineFileRemoveName = server.getUploadedFileName(); //get file name from client
						
						int strLen = onlineFileRemoveName.length(); //get its length
						
						if(strLen > 3)//makes sure there is actually a file
						{
							//create file object from users directory using file name
							File removeFile = new File(userRootDirectoryPath +"/"+onlineFileRemoveName);
							
							if(removeFile.exists())//if file exists
							{
								removeFile.delete();	//delete it
								server.sendMessage("1"); //send success message
							}
							else
							{
								server.sendMessage("0"); //send no success
							}
						}
						else
						{
							server.sendMessage("0");//send no success 
						}
						break;
					case 20:
						//CREATING A NEW HOUSE, RECIEVING HOUSE DATA TO THE SERVER (SERVER SIDE)
						house = new House(); //Create house object
						house = server.getHouseInformation(); //Get house object from client
						
						//Make sure user has entered information into the fields provided
						if(house.getStreet().length() == 0 || house.getTown().length() == 0 
								|| house.getCounty().length() == 0)
						{
							server.sendMessage("0");//if he/she hasnt send failure message
						}
						else //otherwise
						{
						
							//Execute query that will insert new house
							db.executeStatement("INSERT INTO house (Street, Town, County, BuyOrRent) VALUES "
									+ "('"+house.getStreet()+"', '"+house.getTown()+"', "+ "'"+house.getCounty()+"', "
									+ "'"+Character.toString(house.getRentOrSale())+"')");
					    
							server.sendMessage("1");
						}
						break;
					case 21:
						//SEARCHING FOR A PARTICULAR HOUSE
						house = new House();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house");//execute select statement and return results
						
					    /*Loop over the results and see if anything matches the search id information sent
						  from the client*/
					   while(result.next())
						{
					    	//If we get a complete match we know this house exists
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//So add the found house data to a StaffMember object
								house.setStreet(result.getString(2));
								house.setTown(result.getString(3));
								house.setCounty(result.getString(4));
								house.setRentOrSale(result.getString(5).charAt(0));
								
								foundHouse = true; //If the we can send back house information
								break;			   //break the loop
							}
							else //otherwise
							{
								foundHouse = false; //we have not found a match and we cannot do an update
							}
						}
					    
					    if(foundHouse == true)//if we found a house
					    {
					    	server.sendMessage("1");					//send okay signal
					    	server.sendHouseInformation(house);
					    }
					    else if(foundHouse == false)//otherwise
					    {
					    	server.sendMessage("0"); //send not okay signal
					    }
						break;
					case 22:
						//UPDATING HOUSE (SERVER SIDE)
						house = new House();
						searchID = server.getSearchID(); //get search id from client
						house = server.getHouseInformation(); //get house object from client with update data
				
						//execute the statement
						db.executeStatement("UPDATE house SET Street = '" +house.getStreet()+ "', Town = '" +house.getTown()+ "', "
								+ "County = '" +house.getCounty()+ "', BuyOrRent = '" +Character.toString(house.getRentOrSale())+ 
								"' WHERE Id = '" +searchID+"'");//execute select statement and return results
					
					    //send okay signal
					    server.sendMessage("1");
					case 23:
						//DELETING A HOUSE (SERVER SIDE)
						searchID = server.getSearchID();	//get search id from client
						result = db.getResults("SELECT Id FROM house");//execute select statement and return results
						
						/*Loop over the results and see if any match the house for deletion sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a house with this id
								houseDeletion = true; //delete is now allowed
								break;	
							}
							else
							{
								houseDeletion = false;//delete not allowed
							}
						}
					    
						if(houseDeletion == true)//if delete is allowed
					    {
						    db.executeStatement("DELETE FROM house WHERE Id ="+searchID);
						    
						    server.sendMessage("1");//send okay signal
					    }
					    else if(houseDeletion == false)//otherwise
					    {
					    	server.sendMessage("0");//send not okay signal
					    }
						break;
					case 24:
						//SHOW ALL HOUSES (SERVER SIDE)
						houses = new ArrayList<House>();//array list to hold all houses
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							house = new House();
							
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							houses.add(house);//add to list
						}
						
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 25:
						//SEARCH FOR A HOUSE (SERVER SIDE)
						searchID = server.getSearchID();				   //Get search id from client
						house = new House();  //object to hold found house
						
						result = db.getResults("SELECT Id FROM house");//execute select statement and return results
						
						/*Loop over the results and see if any match the house for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a house with this id
								houseExists = true; //house exists
								break;	
							}
							else//otherwise
							{
								houseExists = false;//house does not exist
							}
						}
						
						if(houseExists == true)//if the house exists
						{
							//execute query
							result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house WHERE Id="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into house object
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							server.sendMessage("1");						//send okay signal
							server.sendHouseInformation(house);	//send the object
						}
						else if(houseExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 27:
						//CREATE A RENT TRANSACTION (SERVER SIDE)
						//booleans used for validation
						startDateValid = false;
						endDateValid = false;
						noRentDateOverlap = false;
						rentHouseExists = false;
						rentEstateAgentExists = false;
						rentCustExists = false;
						agentID = server.getClientsSignal(); //gets agent id from client
						custID = server.getClientsSignal(); //get customer id from client
						rHouse = server.getRentableHouseInformation(); //get rentable house from client
						
						result = db.getResults("SELECT BuyOrRent FROM house WHERE Id="+rHouse.getId());//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("r"))
						{
							rentHouseExists = true; //rent house exists
						}
						else//otherwise
						{
							rentHouseExists = false;//rent house does not exist
						}
						
						result = db.getResults("SELECT EmploymentType FROM staff WHERE Id="+agentID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("e"))
						{
							//if we find a match it means we have a estate agent with this id
							rentEstateAgentExists = true; //estate agent exists
						}
						else//otherwise
						{
							rentEstateAgentExists = false;//estate agent does not exist
						}
						
						result = db.getResults("SELECT CustomerID FROM customer WHERE CustomerID="+custID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase(Integer.toString(custID)))
						{
							//if we find a match it means we have a customer with this id
							rentCustExists = true; //customer exists
						}
						else//otherwise
						{
							rentCustExists = false;//customer does not exist
						}
						
						startDateValid = dateValidator.isValidDate(rHouse.getFromDate());
						endDateValid = dateValidator.isValidDate(rHouse.getToDate());
						if(startDateValid && endDateValid)
						{
							result = db.getResults("SELECT HouseID, StartDate, EndDate FROM renthouses");//execute select statement and return results
							/*Loop over the results and make sure that the rent dates entered by the user
							  do not overlap with rent dates for the house they want to rent.*/
							while(result.next())
							{
								if(result.getString(1).equalsIgnoreCase(Integer.toString(rHouse.getId())))
								{
									noRentDateOverlap = dateValidator.noRentDateOverlapping(rHouse.getFromDate(), rHouse.getToDate(), 
																		result.getString(2), result.getString(3));
								}
								else//otherwise we didnt find any transaction like this already
								{
									noRentDateOverlap = true;//so definatly will be true
								}
							}
							
							//if all the validation passed
							if(rentHouseExists && rentEstateAgentExists && rentCustExists && noRentDateOverlap)
							{
								//execute insert query
								db.executeStatement("INSERT INTO renthouses (HouseID, StartDate, EndDate, MonthlyRate, EstateAgentID, CustomerID) VALUES "
										+ "('"+rHouse.getId()+"', '"+rHouse.getFromDate()+"', '"+rHouse.getToDate()+"', '"+rHouse.getRate()+"', '"+agentID+"', '"+custID+"')");
								//send back okay message
								server.sendMessage("1");
							}
							else//or
							{
								//send not okay message
								server.sendMessage("0");
							}
						}
						else
						{
							//send not oaky message
							server.sendMessage("0");
						}
						break;
					case 28:
						//CREATE A BUY TRANSACTION (SERVER SIDE)
						//booleans for validation
						buyHouseExists = false;
						buyCustExists = false;
						buyEstateAgentExists = false;
						sHouse = new SellableHouse();
						agentID = server.getClientsSignal(); //gets agent id from client
						custID = server.getClientsSignal(); //get customer id from client
						sHouse =  server.getSellableHouseInformation(); //get sellable house from client
						
						result = db.getResults("SELECT BuyOrRent FROM house WHERE Id="+sHouse.getId());//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("b"))
						{
							buyHouseExists = true; //buy house exists
						}
						else//otherwise
						{
							buyHouseExists = false;//buy house does not exist
						}
						
						result = db.getResults("SELECT EmploymentType FROM staff WHERE Id="+agentID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("e"))
						{
							//if we find a match it means we have a estate agent with this id
							buyEstateAgentExists = true; //estate agent exists
						}
						else//otherwise
						{
							buyEstateAgentExists = false;//estate agent does not exist
						}
						
						result = db.getResults("SELECT CustomerID FROM customer WHERE CustomerID="+custID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase(Integer.toString(custID)))
						{
							//if we find a match it means we have a customer with this id
							buyCustExists = true; //customer exists
						}
						else//otherwise
						{
							buyCustExists = false;//customer does not exist
						}
						
						//if all validation passes
						if(buyHouseExists && buyEstateAgentExists && buyCustExists)
						{
							//do the appropriate insert and send success to client
							db.executeStatement("INSERT INTO sellhouses (HouseID, Cost, EstateAgentID, CustomerID) VALUES "
									+ "('"+sHouse.getId()+"', '"+sHouse.getCost()+"', '"+agentID+"', '"+custID+"')");
							server.sendMessage("1");
						}
						else//or
						{
							//send unsuccessful message
							server.sendMessage("0");
						}
						break;
					case 29:
						//SEARCHING FOR A PARTICULAR RENT TRANSACTION
						rHouse = new RentableHouse();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT RentID FROM renthouses");//execute select statement and return results
		
						//Loop over the results and see if any match the rent transaction for retrieval sent
						  //from the client
						while(result.next())
						{
							//if match is found
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a rent transaction with this id
								rentTransactionExists = true; //rent transaction exists
								break;	
							}
							else//otherwise
							{
								rentTransactionExists = false;//rent transaction does not exist
							}
						}
						
						//if the rent transaction exists
						if(rentTransactionExists == true)//if the staff member exists
						{
							//execute query
							result = db.getResults("SELECT HouseID, StartDate, EndDate, MonthlyRate, EstateAgentID, CustomerID FROM renthouses WHERE RentID="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into staff member object
							
							//pass the data into the appropiate objects
							rHouse.setId(Integer.parseInt(result.getString(1)));
							rHouse.setFromDate(result.getString(2));
							rHouse.setToDate(result.getString(3));
							rHouse.setRate(Double.parseDouble(result.getString(4)));
							agentID = Integer.parseInt(result.getString(5));
							custID = Integer.parseInt(result.getString(6));
							
							//send them back to the client
							server.sendMessage("1");						//send okay signal
							server.sendRentableHouseInformation(rHouse);
							server.sendMessage(agentID);
							server.sendMessage(custID);
						}
						else if(staffMemberExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 30:
						//UPDATING A RENT TRANSACTION(SERVER SIDE)
						//booleans for validation
						startDateValid = false;
						endDateValid = false;
						noRentDateOverlap = false;
						rentHouseExists = false;
						rentEstateAgentExists = false;
						rentCustExists = false;
						rHouse = new RentableHouse();//rent house object
						searchID = server.getClientsSignal(); //gets transaction id from client 
						rHouse = server.getRentableHouseInformation(); //get rentable house from client
						agentID = server.getClientsSignal(); //gets agent id from client
						custID = server.getClientsSignal(); //get customer id from client
						
						result = db.getResults("SELECT BuyOrRent FROM house WHERE Id="+rHouse.getId());//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("r"))
						{
							
							rentHouseExists = true; //rent house exists
						}
						else//otherwise
						{
							rentHouseExists = false;//rent house does not exist
						}
						
						result = db.getResults("SELECT EmploymentType FROM staff WHERE Id="+agentID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("e"))
						{
							//if we find a match it means we have a estate agent with this id
							rentEstateAgentExists = true; //estate agent exists
						}
						else//otherwise
						{
							rentEstateAgentExists = false;//estate agent does not exist
						}
						
						result = db.getResults("SELECT CustomerID FROM customer WHERE CustomerID="+custID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase(Integer.toString(custID)))
						{
							//if we find a match it means we have a customer with this id
							rentCustExists = true; //customer exists
						}
						else//otherwise
						{
							rentCustExists = false;//customer does not exist
						}
						
						//validating the dates entered by the user
						startDateValid = dateValidator.isValidDate(rHouse.getFromDate());
						endDateValid = dateValidator.isValidDate(rHouse.getToDate());
						
						//if they are both valid dates
						if(startDateValid && endDateValid)
						{
							result = db.getResults("SELECT HouseID, StartDate, EndDate FROM renthouses");//execute select statement and return results
							/*Loop over the results and make sure that the rent dates entered by the user
							  do not overlap with rent dates for the house they want to rent.*/
							while(result.next())
							{
								//if we find a rent transaction
								if(result.getString(1).equalsIgnoreCase(Integer.toString(rHouse.getId())))
								{
									//check for overlap
									noRentDateOverlap = dateValidator.noRentDateOverlapping(rHouse.getFromDate(), rHouse.getToDate(), 
																		result.getString(2), result.getString(3));
								}
								else//otherwise
								{
									//definatly no overlap
									noRentDateOverlap = true;
								}
							}
							
							//if all validation passes
							if(rentHouseExists && rentEstateAgentExists && rentCustExists && noRentDateOverlap)
							{
								//execute the statement to update rent house transaction and send okay signal
								db.executeStatement("UPDATE renthouses SET HouseID = '" +rHouse.getId()+ "', StartDate = '" +rHouse.getFromDate()+ "', "
										+ "EndDate = '" +rHouse.getToDate()+ "', MonthlyRate = '" +rHouse.getRate()+ "', EstateAgentID = '" +agentID+ "', CustomerID = '" +custID
										+ "' WHERE RentID = '" +searchID+"'");//execute select statement and return results
								server.sendMessage("1");
							}
							else//otherwise
							{
								//send not okay signal
								server.sendMessage("0");
							}
						}
						else
						{
							//send not okay signal
							server.sendMessage("0");
						}
						break;
					case 31:
						//SEARCH FOR A PARTICULAR BUY TRANSACTION (SERVER SIDE)
						sellTransactionExists = false;//makes sure transaction sell exists
						sHouse = new SellableHouse();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT BuyID FROM sellhouses");//execute select statement and return results
		
						//Loop over the results and see if any match the rent transaction for retrieval sent
						//from the client
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a rent transaction with this id
								sellTransactionExists = true; //sell transaction exists
								break;	
							}
							else//otherwise
							{
								sellTransactionExists = false;//sell transaction does not exist
							}
						}
						
						if(sellTransactionExists == true)//if the sell transaction exists
						{
							//execute query
							result = db.getResults("SELECT HouseID, Cost, EstateAgentID, CustomerID FROM sellhouses WHERE BuyID="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into sell house object with two variables for customer & agent id
							
							sHouse.setId(Integer.parseInt(result.getString(1)));
							sHouse.setCost(Double.parseDouble(result.getString(2)));
							agentID = Integer.parseInt(result.getString(3));
							custID = Integer.parseInt(result.getString(4));
							
							server.sendMessage("1");						//send okay signal
							server.sendSellableHouseInformation(sHouse);
							server.sendMessage(agentID);
							server.sendMessage(custID);
						}
						else if(staffMemberExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 32:
						//UPDATE A BUY TRANSACTION (SERVER SIDE)
						//booleans for validation
						buyHouseExists = false;
						buyCustExists = false;
						buyEstateAgentExists = false;
						sHouse = new SellableHouse(); //sellable house object
						searchID = server.getClientsSignal(); //gets transaction id from client 
						sHouse =  server.getSellableHouseInformation(); //get sellable house from client
						agentID = server.getClientsSignal(); //gets agent id from client
						custID = server.getClientsSignal(); //get customer id from client
						
						result = db.getResults("SELECT BuyOrRent FROM house WHERE Id="+sHouse.getId());//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("b"))
						{
							buyHouseExists = true; //buy house exists
						}
						else//otherwise
						{
							buyHouseExists = false;//buy house does not exist
						}
						
						result = db.getResults("SELECT EmploymentType FROM staff WHERE Id="+agentID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase("e"))
						{
							//if we find a match it means we have a estate agent with this id
							buyEstateAgentExists = true; //estate agent exists
						}
						else//otherwise
						{
							buyEstateAgentExists = false;//estate agent does not exist
						}
						
						result = db.getResults("SELECT CustomerID FROM customer WHERE CustomerID="+custID);//execute select statement and return results
						result.next();
						if(result.getString(1).equalsIgnoreCase(Integer.toString(custID)))
						{
							//if we find a match it means we have a customer with this id
							buyCustExists = true; //customer exists
						}
						else//otherwise
						{
							buyCustExists = false;//customer does not exist
						}
						
						//If all validation passes
						if(buyHouseExists && buyEstateAgentExists && buyCustExists)
						{
							//execute the statement
							db.executeStatement("UPDATE sellhouses SET HouseID = '" +sHouse.getId()+ "', Cost = '" +sHouse.getCost()+ "', "
									+ "EstateAgentID = '" +agentID+ "', CustomerID = '" +custID
									+ "' WHERE BuyID = '" +searchID+"'");//execute select statement and return results
							server.sendMessage("1");//send okay signal to client
						}
						else//otherwise
						{
							server.sendMessage("0");//send unsuccessful signal
						}
						break;
					case 33:
						//DELETING A SELL HOUSE TRANSACTION(SERVER SIDE)
						searchID = server.getSearchID(); //get search id
						result = db.getResults("SELECT BuyID FROM sellhouses");//execute select statement and return results
						
						/*Loop over the results and see if any match the sell house for deletion sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a house with this id
								sellHouseDelete = true; //delete is now allowed
								break;	
							}
							else
							{
								sellHouseDelete = false;//delete not allowed
							}
						}
					    
						if(sellHouseDelete == true)//if delete is allowed
					    {
							//execute delete satement
						    db.executeStatement("DELETE FROM sellhouses WHERE BuyID ="+searchID);
						    
						    server.sendMessage("1");//send okay signal
					    }
					    else if(sellHouseDelete == false)//otherwise
					    {
					    	server.sendMessage("0");//send not okay signal
					    }
						break;
					case 34:
						//DELETING A RENT HOUSE TRANSACTION(SERVER SIDE)
						searchID = server.getSearchID();//get search id from client
						result = db.getResults("SELECT RentID FROM renthouses");//execute select statement and return results
						
						/*Loop over the results and see if any match the rent house for deletion sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a rent house with this id
								rentHouseDelete = true; //delete is now allowed
								break;	
							}
							else
							{
								rentHouseDelete = false;//delete not allowed
							}
						}
					    
						if(rentHouseDelete == true)//if delete is allowed
					    {
							//execute delete statement
						    db.executeStatement("DELETE FROM renthouses WHERE RentID ="+searchID);
						    
						    server.sendMessage("1");//send okay signal
					    }
					    else if(rentHouseDelete == false)//otherwise
					    {
					    	server.sendMessage("0");//send not okay signal
					    }
						break;
					case 35:
						//VIEWING ALL RENT HOUSE TRANSACTION(SERVER SIDE)
						rentIDs = new ArrayList<Integer>();//array list to hold rent ids
						rHouses = new ArrayList<RentableHouse>();//array list to hold all rentable house objects
						estateAgentIDs = new  ArrayList<Integer>();//array list to hold estate agent ids
						custIDs = new ArrayList<Integer>();//array list to hold customer ids
						
						result = db.getResults("SELECT RentID, HouseID, StartDate, EndDate, MonthlyRate, EstateAgentID, CustomerID FROM renthouses");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list and int array lists for several different ids
						while(result.next())
						{
							rHouse = new RentableHouse();
							
							rentIDs.add(Integer.parseInt(result.getString(1)));
							rHouse.setId(Integer.parseInt(result.getString(2)));
							rHouse.setFromDate(result.getString(3));
							rHouse.setToDate(result.getString(4));
							rHouse.setRate(Double.parseDouble(result.getString(5)));
							estateAgentIDs.add(Integer.parseInt(result.getString(6)));
							custIDs.add(Integer.parseInt(result.getString(7)));
							
							rHouses.add(rHouse);//add to list
						}
						
						server.sendMessage(rentIDs);//send an array list of rent ids to the client
						server.sendMessage(rHouses);//send back the array list of rent house transactions to the client
						server.sendMessage(estateAgentIDs);//send an array list of estate agent ids to the client
						server.sendMessage(custIDs);//send an array list of customer id to the client
						break;
					case 36:
						//SEARCH FOR A RENT TRANSACTION (SERVER SIDE)
						searchID = server.getSearchID();				   //Get search id from client
						rHouse = new RentableHouse();  //object to hold found rent house
						
						result = db.getResults("SELECT RentID FROM renthouses");//execute select statement and return results
						
						/*Loop over the results and see if any match the rent house for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a rent house with this id
								rHouseExists = true; //house exists
								break;	
							}
							else//otherwise
							{
								rHouseExists = false;//house does not exist
							}
						}
						
						//if true the specific row is added to rent house object and variables then sent to client
						if(rHouseExists == true)//if the house exists
						{
							//execute query
							result = db.getResults("SELECT RentID, HouseID, StartDate, EndDate, MonthlyRate, EstateAgentID, CustomerID FROM renthouses WHERE RentID="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into house object
							int rentID = Integer.parseInt(result.getString(1));
							rHouse.setId(Integer.parseInt(result.getString(2)));
							rHouse.setFromDate(result.getString(3));
							rHouse.setToDate(result.getString(4));
							rHouse.setRate(Double.parseDouble(result.getString(5)));
							int estateAgentID = Integer.parseInt(result.getString(6));
							int custID = Integer.parseInt(result.getString(7));
							
							server.sendMessage("1");						//send okay signal
							server.sendMessage(rentID);
							server.sendRentableHouseInformation(rHouse);	//send the object
							server.sendMessage(estateAgentID);
							server.sendMessage(custID);
						}
						else if(houseExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 37:
						//VIEWING ALL BUY HOUSE TRANSACTIONS(SERVER SIDE)
						buyIDs = new ArrayList<Integer>();//array list to hold rent ids
						sHouses = new ArrayList<SellableHouse>();//array list to hold all rentable house objects
						estateAgentIDs = new  ArrayList<Integer>();//array list to hold estate agent ids
						custIDs = new ArrayList<Integer>();//array list to hold customer ids
						
						result = db.getResults("SELECT BuyID, HouseID, Cost, EstateAgentID, CustomerID FROM sellhouses");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							sHouse = new SellableHouse();
							
							buyIDs.add(Integer.parseInt(result.getString(1)));
							sHouse.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							estateAgentIDs.add(Integer.parseInt(result.getString(4)));
							custIDs.add(Integer.parseInt(result.getString(5)));
							sHouses.add(sHouse);//add to list
						}
						
						server.sendMessage(buyIDs);//send an array list of rent ids to the client
						server.sendMessage(sHouses);//send back the array list of rent house transactions to the client
						server.sendMessage(estateAgentIDs);//send an array list of estate agent ids to the client
						server.sendMessage(custIDs);//send an array list of customer id to the client
						break;
					case 38:
						//SEARCH FOR A BUY TRANSACTION (SERVER SIDE)
						searchID = server.getSearchID();				   //Get search id from client
						sHouse = new SellableHouse();  //object to hold found house
						
						result = db.getResults("SELECT BuyID FROM sellhouses");//execute select statement and return results
						
						/*Loop over the results and see if any match the house for retrieval sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a house with this id
								sHouseExists = true; //house exists
								break;	
							}
							else//otherwise
							{
								sHouseExists = false;//house does not exist
							}
						}
						
						if(sHouseExists == true)//if the house exists
						{
							//execute query
							result = db.getResults("SELECT BuyID, HouseID, Cost, EstateAgentID, CustomerID FROM sellhouses WHERE BuyID="+searchID);//execute select statement and return results
							result.next();//move to start of retrieved record
							//Pass the data into house object
							int buyID = Integer.parseInt(result.getString(1));
							sHouse.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							int estateAgentID = Integer.parseInt(result.getString(4));
							int custID = Integer.parseInt(result.getString(5));
							
							server.sendMessage("1");						//send okay signal
							server.sendMessage(buyID);
							server.sendSellableHouseInformation(sHouse);	//send the object
							server.sendMessage(estateAgentID);
							server.sendMessage(custID);
						}
						else if(houseExists == false)//otherwise
						{
							server.sendMessage("0");	//send not okay signal
						}
						break;
					case 39:
						//CREATING A CUSTOMER (SERVER SIDE)
						customer = server.getCustomerInformation();       //Get user object from the client
						
						//Make sure user has entered information into the fields provided
						if(customer.getfName().length() == 0 || customer.getlName().length() == 0 
								|| customer.getAddress().length() == 0)
						{
							server.sendMessage("0");//if he/she hasnt send failure message
						}
						else //otherwise
						{
							//Execute query that will insert new customer
							//this method will throw an exception and signal client if not valid
						    db.executeStatement("INSERT INTO customer (FirstName, LastName, Address) VALUES "
									+ "('"+customer.getfName()+"', '"+customer.getlName()+"', "+ "'"+customer.getAddress()+"')");
					    
							server.sendMessage("1");//send okay signal
						}    
						break;
					case 40:
						//DELETING A CUSTOMER (SERVER SIDE)
						searchID = server.getSearchID();
						result = db.getResults("SELECT CustomerID FROM customer");//execute select statement and return results
						
						/*Loop over the results and see if any match the customer for deletion sent
						  from the client*/
						while(result.next())
						{
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								//if we find a match it means we have a customer with this id
								custDelete = true; //delete is now allowed
								break;	
							}
							else
							{
								custDelete = false;//delete not allowed
							}
						}
					    
						if(custDelete == true)//if delete is allowed
					    {
						    db.executeStatement("DELETE FROM customer WHERE CustomerID ="+searchID);
						    server.sendMessage("1");//send okay signal
					    }
					    else if(custDelete == false)//otherwise
					    {
					    	server.sendMessage("0");//send not okay signal
					    }
						break;
					case 41:
						//FINDING A CUSTOMER (SERVER SIDE)
						customer = new Customer();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer");//execute select statement and return results
						
					    /*Loop over the results and see if anything matches the search id information sent
						  from the client*/
					   while(result.next())
						{
					    	//If we get a complete match we know this customer exists
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								customer.setfName(result.getString(2));
								customer.setlName(result.getString(3));
								customer.setAddress(result.getString(4));
								
								foundCustomer = true; //If the we can send back customer information
								break;			   //break the loop
							}
							else //otherwise
							{
								foundCustomer = false; //we have not found a match and we cannot do an update
							}
						}
					    
					    if(foundCustomer == true)//if we found a customer
					    {
					    	server.sendMessage("1");					//send okay signal
					    	server.sendCustomerInformation(customer);
					    }
					    else if(foundCustomer == false)//otherwise
					    {
					    	server.sendMessage("0"); //send not okay signal
					    }
						break;
					case 42:
						//UPDATING CUSTOMER(SERVER SIDE)
						customer = new Customer();//customer object
						searchID = server.getSearchID();//get search id from client
						customer = server.getCustomerInformation(); //get customer object from client
				
						//execute the statement
						db.executeStatement("UPDATE customer SET FirstName = '" +customer.getfName()+ "', LastName = '" +customer.getlName()+ "', "
								+ "Address = '" +customer.getAddress()+ 
								"' WHERE CustomerID = '" +searchID+"'");//execute select statement and return results
					
					    //send okay signal
					    server.sendMessage("1");
						break;
					case 43:
						//VIEW ALL CUSTOMERS
						customers = new ArrayList<Customer>();//array list to hold all customers
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer");//execute select statement and return results
						
						//Loop over the results and add each customers data to an object which is then
						//added to a list
						while(result.next())
						{
							customer = new Customer();
							
							customer.setCustID(Integer.parseInt(result.getString(1)));
							customer.setfName(result.getString(2));
							customer.setlName(result.getString(3));
							customer.setAddress(result.getString(4));
						
							customers.add(customer);//add to list
						}
						
						server.sendMessage(customers);//send back the array list of customers to the client
						break;
					case 44:
						//SEARCH FOR A PARTICULAR CUSTOMER
						customer = new Customer();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer");//execute select statement and return results
						
					    /*Loop over the results and see if anything matches the search id information sent
						  from the client*/
					   while(result.next())
						{
					    	//If we get a complete match we know this customer exists
							if(result.getString(1).equalsIgnoreCase(Integer.toString(searchID)))
							{
								customer.setCustID(Integer.parseInt(result.getString(1)));
								customer.setfName(result.getString(2));
								customer.setlName(result.getString(3));
								customer.setAddress(result.getString(4));
								
								foundViewCust = true; //If the we can send back customer information
								break;			   //break the loop
							}
							else //otherwise
							{
								foundViewCust = false; //we have not found a match and we cannot do an update
							}
						}
					    
					    if(foundViewCust == true)//if we found a customer
					    {
					    	server.sendMessage("1");					//send okay signal
					    	server.sendCustomerInformation(customer);
					    }
					    else if(foundViewCust == false)//otherwise
					    {
					    	server.sendMessage("0"); //send not okay signal
					    }
						break;
					case 45:
						//CREATING A NEW REMINDER
						reminder = server.getReminderInformation();//get reminder object from the client
						reminderDateValid = false;
						reminderTimeValid = false;
						reminderDateValid = dateValidator.isValidDate(reminder.getDate());//make sure that and time are valid dates and times
						reminderTimeValid = timeValidator.validateTime(reminder.getTime());
						String subject = reminder.getSubject();
						isWhitespace = subject.contains(" ");//subject can only be one word
						
						//if date and time are valid and subject is one word
						if(reminderDateValid == true && reminderTimeValid == true && isWhitespace == false)
						{
							int rows = 0;//open the reminder file for reading
							fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));

							while(fileReader.hasNextLine()) //count how many rows we have in the file
							{                                
								fileReader.nextLine(); //advance the inputstream
								++rows;
							}

							File file = new File(serversFilePath + "/" + username + "/Reminder.txt");//create file object
							//true = append file
				    		FileWriter fileWriter = new FileWriter(file ,true);//creater writer for file appending
				    		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
				    		String formatStr = "%-4d %-25s %-10s %-5s %-100s%n";///format string then append data
				    		bufferWriter.write(String.format(formatStr, ++rows, reminder.getSubject(), reminder.getDate(), reminder.getTime(), reminder.getDesc()));
							
							bufferWriter.close();//close read and writes to the file
							fileReader.close();
							
							server.sendMessage("1");//send okay signal
						}
						else//if validation fails
						{
							server.sendMessage("0");//not okay
						}
						break;
					case 46:
						//FINDING A REMINDER (SERVER SIDE)
						reminder = new Reminder();//create object to hold the data
						searchID = server.getSearchID();//get search id from the client
						
						//open file for reading
						fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));
						
						while(fileReader.hasNext())//loop over rows
						{
							reminder.setReminderID(fileReader.nextInt());
							reminder.setSubject(fileReader.next());
							reminder.setDate(fileReader.next());
							reminder.setTime(fileReader.next());
							String[] words = fileReader.nextLine().split("\\s");
							String desc = "";
							for(int i = 0; i < words.length; ++i)
							{
								desc += words[i]+" ";
							}
							reminder.setDesc(desc);
							
							if(reminder.getReminderID() == searchID)//if one of the rows matchs the id
							{						
								foundReminder = true; //If the we can send back reminder information
								break;			   //break the loop
							}
							else
							{
								foundReminder = false; //we have not found a match and we cannot do an update
							}
						}
						
						if(foundReminder == true && isWhitespace == false)//if we found a reminder
					    {
					    	server.sendMessage("1");					//send okay signal
					    	server.sendReminderInformation(reminder);	//send the reminder data
					    }
					    else if(foundReminder == false || isWhitespace == true)//otherwise
					    {
					    	server.sendMessage("0"); //send not okay signal
					    }
						
						fileReader.close();//close the file reader
						break;
					case 47:
						//UPDATING A REMINDER (SERVER SIDE)
						searchID = server.getSearchID();//get search id from client
						reminder = server.getReminderInformation(); //get reminder object from client
						reminderList = new ArrayList<Reminder>();	//holds list of reminders from the file

						reminderDateValid = false;
						reminderTimeValid = false;
						reminderDateValid = dateValidator.isValidDate(reminder.getDate());//validate date
						reminderTimeValid = timeValidator.validateTime(reminder.getTime());//validate time

						//if date/time are valid
						if(reminderDateValid == true && reminderTimeValid == true)
						{
							//open file for reading
							fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));

							while(fileReader.hasNext())//loop over rows
							{
								rListMember = new Reminder();
								
								rListMember.setReminderID(fileReader.nextInt());
								rListMember.setSubject(fileReader.next());
								rListMember.setDate(fileReader.next());
								rListMember.setTime(fileReader.next());
								String[] words = fileReader.nextLine().split("\\s");
								String desc = "";
								for(int i = 0; i < words.length; ++i)
								{
									desc += words[i]+" ";
								}
								rListMember.setDesc(desc);
								
								if(rListMember.getReminderID() == searchID)//if we find the correct id
								{						
									rListMember.setSubject(reminder.getSubject());//we set its data todata sent from client
									rListMember.setDate(reminder.getDate());
									rListMember.setTime(reminder.getTime());
									rListMember.setDesc(reminder.getDesc());
									
									reminderList.add(rListMember);//add to list
									continue;			   	  //move to next interation of the loop
								}
								
								reminderList.add(rListMember);//add to list if no id found
							}//end read while loop
							
							fileReader.close();//close reader

							//open file for writing
							fileWriter = new PrintWriter(serversFilePath + "/" + username + "/Reminder.txt");
							int size = reminderList.size();///get lists size, holds all data from the file
							int count = 0;
							
							while(count < size)//loop list size amount times
							{
								rListMember = new Reminder();
								rListMember = reminderList.get(count);
								
								//write list containing updated data to the file
								fileWriter.printf("%-4d %-25s %-10s %-5s %-100s%n",
										rListMember.getReminderID(), rListMember.getSubject(), rListMember.getDate(), rListMember.getTime(), rListMember.getDesc());
							
								++count;
							}//end write file loop
							
							fileWriter.close();//close file writer
					
							//send okay signal
						    server.sendMessage("1");
						}
						else
						{
							//send unsuccessful signal
							server.sendMessage("0");
						}
						break;
					case 48:
						//DELETING A REMINDER (SERVER SIDE)
						searchID = server.getSearchID();//get search id from client
						reminderList = new ArrayList<Reminder>();//hold all rows from file

						//open file reader
						fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));

						while(fileReader.hasNext())//loop over file rows
						{
							rListMember = new Reminder();//object
							
							rListMember.setReminderID(fileReader.nextInt());//pass data to object
							rListMember.setSubject(fileReader.next());
							rListMember.setDate(fileReader.next());
							rListMember.setTime(fileReader.next());
							String[] words = fileReader.nextLine().split("\\s");//multi word desc passed into array
							String desc = "";
							for(int i = 0; i < words.length; ++i)
							{
								desc += words[i]+" ";//create string from array of words
							}
							rListMember.setDesc(desc);
							
							if(rListMember.getReminderID() == searchID)//if we find correct id
							{						
								rListMember.setReminderID(0);//set deleted data
								rListMember.setSubject("Deleted");
								rListMember.setDate("Deleted");
								rListMember.setTime("Deleted");
								rListMember.setDesc("Deleted");
								reminderList.add(rListMember);//add to list
							}
							else if(rListMember.getReminderID() != searchID)//otherwise just add the data to the list
							{
								reminderList.add(rListMember);
							}
						}//end read while loop
						
						fileReader.close();//close reader

						//open writer
						fileWriter = new PrintWriter(serversFilePath + "/" + username + "/Reminder.txt");
						int size = reminderList.size();//get list size
						int count = 0;
						
						while(count < size)//loop list size times
						{
							rListMember = new Reminder();
							rListMember = reminderList.get(count);//get each member from list
							
							//write updated data to the file
							fileWriter.printf("%-4d %-25s %-10s %-5s %-100s%n",
									rListMember.getReminderID(), rListMember.getSubject(), rListMember.getDate(), rListMember.getTime(), rListMember.getDesc().trim());
						
							++count;
						}//end write file loop
						
						fileWriter.close();//close writer
				
						//send okay signal
					    server.sendMessage("1");
					    break;
					case 49:
						//GET TODAYS REMINDERS (SERVER SIDE)
						remindersList = new ArrayList<Reminder>();//list to hold all found reminders
						String todaysDate = server.getClientMessage();//get todays date sent from the client
						
						//open file for reading
						fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));
							
						while(fileReader.hasNext())///loop over file rows
						{
							rListMember = new Reminder();
								
							rListMember.setReminderID(fileReader.nextInt());//get data 
							rListMember.setSubject(fileReader.next());
							rListMember.setDate(fileReader.next());
							rListMember.setTime(fileReader.next());
							String[] words = fileReader.nextLine().split("\\s");
							String desc = "";
							for(int i = 0; i < words.length; ++i)
							{
								desc += words[i]+" ";
							}
							rListMember.setDesc(desc);
								
							if(rListMember.getDate().equals(todaysDate))//if data matchs, add object to list
							{						
								remindersList.add(rListMember);
							}
							else//otherwise move to next iteration
							{
								continue;
							}
						}//end read while loop
							
						fileReader.close();//close reader
							
						server.sendMessage(remindersList);
						break;
					case 50:
						//GET REMINDERS FOR A CERTAIN DATE (SERVER SIDE)
						remindersList = new ArrayList<Reminder>();//list to hold all found reminders
						String date = server.getClientMessage();//get todays date sent from the client
						
						validSearchDate = dateValidator.isValidDate(date);//makes sure date sent from client is
						
						if(validSearchDate == true)//if date is valid search date
						{
							//open file for reading
							fileReader = new Scanner(new FileReader(serversFilePath + "/" + username + "/Reminder.txt"));
							
							while(fileReader.hasNext())//loop over file rows
							{
								rListMember = new Reminder();//object
								
								rListMember.setReminderID(fileReader.nextInt());//get data and add to object
								rListMember.setSubject(fileReader.next());
								rListMember.setDate(fileReader.next());
								rListMember.setTime(fileReader.next());
								String[] words = fileReader.nextLine().split("\\s");//if desc has more than one word split and add to array
								String desc = "";
								for(int i = 0; i < words.length; ++i)
								{
									desc += words[i]+" ";//then create single string from array
								}
								rListMember.setDesc(desc);
								
								if(rListMember.getDate().equals(date))//if date matches search date
								{						
									remindersList.add(rListMember);//we can add object to the list
								}
								else//otherwise
								{
									continue;//next iteration
								}
							}//end read while loop
							
							fileReader.close();//close file reader
							
							server.sendMessage("1");//send okay signal
							server.sendMessage(remindersList);//send list of found reminder objects
						}
						else//if search date isnt valid
						{
							server.sendMessage("0");//send not okay signal
						}
						break;
					case 51:
						//SEARCH STAFF BY FIRST NAME (SERVER SIDE)
						staffMembers = new ArrayList<StaffMember>();//array list to hold all staff members
						fName = server.getClientMessage();//get search first name sent from the client
						result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff WHERE FirstName LIKE '"+fName+"%'");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							StaffMember member = new StaffMember();
							
							member.setId(Integer.parseInt(result.getString(1)));
							member.setFirstName(result.getString(2));
							member.setLastName(result.getString(3));
							member.setAddress(result.getString(4));
							member.setPps(result.getString(5));
							member.setSalary(Double.parseDouble(result.getString(6)));
							member.setStaffType(result.getString(7).charAt(0));
						
							staffMembers.add(member);//add to list
						}
						
						server.sendMessage(staffMembers);//send back the array list of staff to the client
						break;
					case 52:
						//SEARCH STAFF BY LAST NAME (SERVER SIDE)
						staffMembers = new ArrayList<StaffMember>();//array list to hold all staff members
						LName = server.getClientMessage();//get search first name sent from the client
						result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff WHERE LastName LIKE '"+LName+"%'");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							StaffMember member = new StaffMember();
							
							member.setId(Integer.parseInt(result.getString(1)));
							member.setFirstName(result.getString(2));
							member.setLastName(result.getString(3));
							member.setAddress(result.getString(4));
							member.setPps(result.getString(5));
							member.setSalary(Double.parseDouble(result.getString(6)));
							member.setStaffType(result.getString(7).charAt(0));
						
							staffMembers.add(member);//add to list
						}
						
						server.sendMessage(staffMembers);//send back the array list of staff to the client
						break;
					case 53:
						//SEARCH STAFF BY EMPLOYMENT TYPE (SERVER SIDE)
						staffMembers = new ArrayList<StaffMember>();//array list to hold all staff members
						String empType =  server.getClientMessage();//get search emp type sent from the client
						result = db.getResults("SELECT Id, FirstName, LastName, Address, PPS, Salary, EmploymentType FROM staff WHERE EmploymentType='"+empType+"'");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							StaffMember member = new StaffMember();
							
							member.setId(Integer.parseInt(result.getString(1)));
							member.setFirstName(result.getString(2));
							member.setLastName(result.getString(3));
							member.setAddress(result.getString(4));
							member.setPps(result.getString(5));
							member.setSalary(Double.parseDouble(result.getString(6)));
							member.setStaffType(result.getString(7).charAt(0));
						
							staffMembers.add(member);//add to list
						}
						
						server.sendMessage(staffMembers);//send back the array list of staff to the client
						break;
					case 54:
						//SEARCH FOR ALL RENTABLE HOUSES (SERVER SIDE)
						houseType = server.getClientMessage();
						houses = new ArrayList<House>();//array list to hold all houses
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house WHERE BuyOrRent='"+houseType+"'");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							house = new House();
							
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							houses.add(house);//add to list
						}
						
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 55:
						//SEARCH FOR ALL BUYABLE HOUSES (SERVER SIDE)
						houseType = server.getClientMessage();
						houses = new ArrayList<House>();//array list to hold all houses
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house WHERE BuyOrRent='"+houseType+"'");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							house = new House();
							
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							houses.add(house);//add to list
						}
						
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 56:
						//SEARCH FOR HOUSES BY TOWN NAME(SERVER SIDE)
						String town = server.getClientMessage();
						houses = new ArrayList<House>();//array list to hold all houses
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house WHERE Town LIKE '"+town+"%'");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							house = new House();
							
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							houses.add(house);//add to list
						}
						
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 57:
						//SEARCH FOR HOUSES BY COUNTY NAME(SERVER SIDE)
						county = server.getClientMessage();
						houses = new ArrayList<House>();//array list to hold all houses
						result = db.getResults("SELECT Id, Street, Town, County, BuyOrRent FROM house WHERE County LIKE '"+county+"%'");//execute select statement and return results
						
						//Loop over the results and add each houses data to an object which is then
						//added to a list
						while(result.next())
						{
							house = new House();
							
							house.setId(Integer.parseInt(result.getString(1)));
							house.setStreet(result.getString(2));
							house.setTown(result.getString(3));
							house.setCounty(result.getString(4));
							house.setRentOrSale(result.getString(5).charAt(0));
							
							houses.add(house);//add to list
						}
						
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 58:
						//SEARCH CUSTOMER BY FIRST NAME (SERVER SIDE)
						customers = new ArrayList<Customer>();//array list to hold all customers
						fName = server.getClientMessage();//get search first name sent from the client
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer WHERE FirstName LIKE '"+fName+"%'");//execute select statement and return results
						
						//Loop over the results and add each customers data to an object which is then
						//added to a list
						while(result.next())
						{
							customer = new Customer();
							
							customer.setCustID(Integer.parseInt(result.getString(1)));
							customer.setfName(result.getString(2));
							customer.setlName(result.getString(3));
							customer.setAddress(result.getString(4));
						
							customers.add(customer);//add to list
						}
						
						server.sendMessage(customers);//send back the array list of staff to the client
						break;
					case 59:
						//SEARCH CUSTOMER BY LAST NAME (SERVER SIDE)
						customers = new ArrayList<Customer>();//array list to hold all customers
						LName = server.getClientMessage();//get search last name sent from the client
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer WHERE LastName LIKE '"+LName+"%'");//execute select statement and return results
						
						//Loop over the results and add each customers data to an object which is then
						//added to a list
						while(result.next())
						{
							customer = new Customer();
							
							customer.setCustID(Integer.parseInt(result.getString(1)));
							customer.setfName(result.getString(2));
							customer.setlName(result.getString(3));
							customer.setAddress(result.getString(4));
						
							customers.add(customer);//add to list
						}
						
						server.sendMessage(customers);//send back the array list of staff to the client
						break;
					case 60:
						//SEARCH CUSTOMER BY ADDRESS (SERVER SIDE)
						customers = new ArrayList<Customer>();//array list to hold all customers
						address = server.getClientMessage();//get search address sent from the client
						result = db.getResults("SELECT CustomerID, FirstName, LastName, Address FROM customer WHERE Address LIKE '%"+address+"%'");//execute select statement and return results
						
						//Loop over the results and add each customers data to an object which is then
						//added to a list
						while(result.next())
						{
							customer = new Customer();
							
							customer.setCustID(Integer.parseInt(result.getString(1)));
							customer.setfName(result.getString(2));
							customer.setlName(result.getString(3));
							customer.setAddress(result.getString(4));
						
							customers.add(customer);//add to list
						}
						
						server.sendMessage(customers);//send back the array list of staff to the client
						break;
					case 61:
						//SEARCH FOR BUY TRANSACTIONS IN A CERTAIN COUNTY (SERVER SIDE)
						sHouses = new ArrayList<SellableHouse>();//list to hold sellable house objects(data)
						houses = new ArrayList<House>();//list to hold house objects(data)
						estateAgentIDs = new ArrayList<Integer>();//list to hold estate agent ids
						custIDs = new ArrayList<Integer>();//list to hold customer ids
						county = server.getClientMessage();//get search county sent from the client
						//example using joins in the sql queries
						result = db.getResults("SELECT sellhouses.BuyID, sellhouses.HouseID, sellhouses.Cost, sellhouses.EstateAgentID, sellhouses.CustomerID, house.Street, house.Town, house.County" +
												" FROM sellhouses" +
												" INNER JOIN house" +
												" ON house.County='"+county+"' AND house.Id = sellhouses.HouseID");//execute select statement and return results
						
						//Loop over the results and add data to objects which are then
						//added to lists
						while(result.next())
						{
							sHouse = new SellableHouse();
							house = new House();
							
							sHouse.setId(Integer.parseInt(result.getString(1)));
							house.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							estateAgentIDs.add(Integer.parseInt(result.getString(4)));
							custIDs.add(Integer.parseInt(result.getString(5)));
							house.setStreet(result.getString(6));
							house.setTown(result.getString(7));
							house.setCounty(result.getString(8));
							
							sHouses.add(sHouse);//add to list of sellable houses
							houses.add(house);//add to list of houses
						}
						
						server.sendMessage(sHouses);//send back the array list of sellable houses (trans) to the client
						server.sendMessage(houses);//send back the array list of houses to the client
						server.sendMessage(estateAgentIDs);//send back the array list of houses to the client
						server.sendMessage(custIDs);//send back the array list of houses to the client
						break;
					case 62:
						//SEARCH FOR BUY TRANSACTIONS BY COST (>, <, =) (SERVER SIDE)
						sHouses = new ArrayList<SellableHouse>();//list to hold sellable house objects(data)
						buyIDs = new ArrayList<Integer>();//array list to hold rent ids
						estateAgentIDs = new  ArrayList<Integer>();//array list to hold estate agent ids
						custIDs = new ArrayList<Integer>();//array list to hold customer ids
						operator = server.getClientMessage();
						String cost = server.getClientMessage();
						result = db.getResults("SELECT BuyID, HouseID, Cost, EstateAgentID, CustomerID FROM sellhouses WHERE Cost "+operator+" "+cost);//execute select statement and return results
						
						//Loop over the results and add data to objects which are then
						//added to lists
						while(result.next())
						{
							sHouse = new SellableHouse();
							
							buyIDs.add(Integer.parseInt(result.getString(1)));
							sHouse.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							estateAgentIDs.add(Integer.parseInt(result.getString(4)));
							custIDs.add(Integer.parseInt(result.getString(5)));
							sHouses.add(sHouse);//add to list
						}
						
						server.sendMessage(buyIDs);//send an array list of buy ids to the client
						server.sendMessage(sHouses);//send back the array list of buy house transactions to the client
						server.sendMessage(estateAgentIDs);//send an array list of estate agent ids to the client
						server.sendMessage(custIDs);//send an array list of customer id to the client
						break;
						//SEARCH FOR BUY TRANSACTIONS INVOLVING A CERTAIN CUSTOMER (SERVER SIDE)
					case 63:
						buyIDs = new ArrayList<Integer>();
						sHouses = new ArrayList<SellableHouse>();	//list to hold sellable house objects(data)
						customers = new ArrayList<Customer>();			//list to hold house objects(data)
						estateAgentIDs = new ArrayList<Integer>();	//list to hold estate agent ids
						id = server.getSearchID();					//get search id from the client
						
						//example using joins in the sql queries
						result = db.getResults("SELECT sellhouses.BuyID, sellhouses.HouseID, sellhouses.Cost, sellhouses.EstateAgentID, sellhouses.CustomerID, customer.FirstName, customer.LastName, customer.Address" +
						" FROM sellhouses" +
						" INNER JOIN customer" +
						" ON sellhouses.CustomerID="+id+" AND sellhouses.CustomerID = customer.CustomerID;");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							sHouse = new SellableHouse();
							customer = new Customer();
							
							buyIDs.add(Integer.parseInt(result.getString(1)));
							sHouse.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							estateAgentIDs.add(Integer.parseInt(result.getString(4)));
							customer.setCustID(Integer.parseInt(result.getString(5)));
							customer.setfName(result.getString(6));
							customer.setlName(result.getString(7));
							customer.setAddress(result.getString(8));
							
							sHouses.add(sHouse);//add to list of sellable houses
							customers.add(customer);//add to list of associated customers
						}
						
						server.sendMessage(buyIDs);
						server.sendMessage(sHouses);//send back the array list of sellable houses (trans) to the client
						server.sendMessage(customers);//send back the array list of houses to the client
						server.sendMessage(estateAgentIDs);//send back the array list of houses to the client
						break;
					case 64:
						//SEARCH FOR BUY TRANSACTIONS INVOLVING A CERTAIN ESTATE AGENT(SERVER SIDE)
						buyIDs = new ArrayList<Integer>();						//list to hold buy ids
						sHouses = new ArrayList<SellableHouse>();				//list to hold sellable house objects(data)
						staffMembers  = new ArrayList<StaffMember>();			//list to hold staff member objects(data)
						custIDs = new ArrayList<Integer>();
						id = server.getSearchID();								//get search id from the client
						
						//example using joins in the sql queries
						result = db.getResults("SELECT sellhouses.BuyID, sellhouses.HouseID, sellhouses.Cost, sellhouses.CustomerID, sellhouses.EstateAgentID, staff.FirstName, staff.LastName, staff.Address"+
												" FROM sellhouses"+
												" INNER JOIN staff"+
												" ON sellhouses.EstateAgentID="+id+" AND sellhouses.EstateAgentID = staff.Id;");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							sHouse = new SellableHouse();
							staff = new StaffMember();
							
							buyIDs.add(Integer.parseInt(result.getString(1)));
							sHouse.setId(Integer.parseInt(result.getString(2)));
							sHouse.setCost(Double.parseDouble(result.getString(3)));
							custIDs.add(Integer.parseInt(result.getString(4)));
							staff.setId(Integer.parseInt(result.getString(5)));
							staff.setFirstName(result.getString(6));
							staff.setLastName(result.getString(7));
							staff.setAddress(result.getString(8));
							
							
							sHouses.add(sHouse);//add to list of sellable houses
							staffMembers.add(staff);//add to list of associated estate agent (staff member)
						}
						
						server.sendMessage(buyIDs);//send list of buy ids
						server.sendMessage(sHouses);//send back the array list of sellable houses (trans) to the client
						server.sendMessage(custIDs);//send back the array list of customer ids to the client
						server.sendMessage(staffMembers);//send back the array list of staff members
						break;
					case 65:
						//SEARCH FOR RENT TRANSACTIONS IN A CERTAIN COUNTY (SERVER SIDE)
						rHouses = new ArrayList<RentableHouse>();//list to hold sellable house objects(data)
						houses = new ArrayList<House>();//list to hold house objects(data)
						county = server.getClientMessage();//get search county sent from the client
						//example using joins in the sql queries
						result = db.getResults("SELECT renthouses.RentID, renthouses.StartDate, renthouses.EndDate, renthouses.MonthlyRate, renthouses.HouseID, house.Street, house.Town, house.County"+
												" FROM renthouses"+
												" INNER JOIN house"+
												" ON house.County='"+county+"' AND house.Id = renthouses.HouseID;");//execute select statement and return results
						
						//Loop over the results and add data to objects which are then
						//added to lists
						while(result.next())
						{
							rHouse = new RentableHouse();
							house = new House();
							
							rHouse.setId(Integer.parseInt(result.getString(1)));
							rHouse.setFromDate(result.getString(2));
							rHouse.setToDate(result.getString(3));
							rHouse.setRate(Double.parseDouble(result.getString(4)));
							house.setId(Integer.parseInt(result.getString(5)));
							house.setStreet(result.getString(6));
							house.setTown(result.getString(7));
							house.setCounty(result.getString(8));
							
							rHouses.add(rHouse);//add to list of sellable houses
							houses.add(house);//add to list of houses
						}
						
						server.sendMessage(rHouses);//send back the array list of sellable houses (trans) to the client
						server.sendMessage(houses);//send back the array list of houses to the client
						break;
					case 66:
						//SEARCH FOR RENT TRANSACTIONS BY COST (>, <, =) (SERVER SIDE)
						rHouses = new ArrayList<RentableHouse>();//list to hold rentable house objects(data)
						rentIDs = new ArrayList<Integer>();//array list to hold rent ids
						estateAgentIDs = new  ArrayList<Integer>();//array list to hold estate agent ids
						custIDs = new ArrayList<Integer>();//array list to hold customer ids
						operator = server.getClientMessage();
						String rate = server.getClientMessage();
						result = db.getResults("SELECT RentID, HouseID, StartDate, EndDate, MonthlyRate, EstateAgentID, CustomerID FROM renthouses WHERE MonthlyRate "+operator+" "+rate);//execute select statement and return results
						
						//Loop over the results and add data to objects which are then
						//added to lists
						while(result.next())
						{
							rHouse = new RentableHouse();
							
							rentIDs.add(Integer.parseInt(result.getString(1)));
							rHouse.setId(Integer.parseInt(result.getString(2)));
							rHouse.setFromDate(result.getString(3));
							rHouse.setToDate(result.getString(4));
							rHouse.setRate(Double.parseDouble(result.getString(5)));
							estateAgentIDs.add(Integer.parseInt(result.getString(6)));
							custIDs.add(Integer.parseInt(result.getString(7)));
							rHouses.add(rHouse);//add to list
						}
						
						server.sendMessage(rentIDs);//send an array list of rent ids to the client
						server.sendMessage(rHouses);//send back the array list of rent house transactions to the client
						server.sendMessage(estateAgentIDs);//send an array list of estate agent ids to the client
						server.sendMessage(custIDs);//send an array list of customer id to the client
						break;
					case 67:
						//SEARCH FOR RENT TRANSACTIONS INVOLVING A CERTAIN CUSTOMER (SERVER SIDE)
						rentIDs = new ArrayList<Integer>();
						rHouses = new ArrayList<RentableHouse>();	//list to hold rentable house objects(data)
						customers = new ArrayList<Customer>();			//list to hold house objects(data)
						id = server.getSearchID();					//get search id from the client
						
						//example using joins in the sql queries
						result = db.getResults("SELECT renthouses.RentID, renthouses.HouseID, renthouses.StartDate, renthouses.EndDate, renthouses.MonthlyRate,"+
											   " renthouses.CustomerID, customer.FirstName, customer.LastName, customer.Address"+
											   " FROM renthouses"+
											   " INNER JOIN customer"+
											   " ON renthouses.CustomerID="+id+" AND renthouses.CustomerID = customer.CustomerID;");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							rHouse = new RentableHouse();
							customer = new Customer();
							
							rentIDs.add(Integer.parseInt(result.getString(1)));
							rHouse.setId(Integer.parseInt(result.getString(2)));
							rHouse.setFromDate(result.getString(3));
							rHouse.setToDate(result.getString(4));
							rHouse.setRate(Double.parseDouble(result.getString(5)));
							customer.setCustID(Integer.parseInt(result.getString(6)));
							customer.setfName(result.getString(7));
							customer.setlName(result.getString(8));
							customer.setAddress(result.getString(9));
							
							rHouses.add(rHouse);//add to list of rentable houses
							customers.add(customer);//add to list of associated customers
						}
						
						server.sendMessage(rentIDs);//send list of rent IDs back to client
						server.sendMessage(rHouses);//send back the array list of rentable houses (trans) to the client
						server.sendMessage(customers);//send back the array list of houses to the client
						break;
					case 68:
						//SEARCH FOR RENT TRANSACTIONS INVOLVING A CERTAIN ESTATE AGENT(SERVER SIDE)
						rentIDs = new ArrayList<Integer>();						//list to hold rent ids
						rHouses = new ArrayList<RentableHouse>();				//list to hold rentable house objects(data)
						staffMembers  = new ArrayList<StaffMember>();			//list to hold staff member objects(data)
						id = server.getSearchID();								//get search id from the client
						
						//example using joins in the sql queries
						result = db.getResults("SELECT renthouses.RentID, renthouses.HouseID, renthouses.StartDate, renthouses.EndDate, renthouses.MonthlyRate, renthouses.EstateAgentID,"+
											   " staff.FirstName, staff.LastName, staff.Address"+
											   " FROM renthouses"+
											   " INNER JOIN staff"+
											   " ON renthouses.EstateAgentID="+id+" AND renthouses.EstateAgentID = staff.Id;");//execute select statement and return results
						
						//Loop over the results and add each staff members data to an object which is then
						//added to a list
						while(result.next())
						{
							rHouse = new RentableHouse();
							staff = new StaffMember();
							
							rentIDs.add(Integer.parseInt(result.getString(1)));
							rHouse.setId(Integer.parseInt(result.getString(2)));
							rHouse.setFromDate(result.getString(3));
							rHouse.setToDate(result.getString(4));
							rHouse.setRate(Double.parseDouble(result.getString(5)));
							staff.setId(Integer.parseInt(result.getString(6)));
							staff.setFirstName(result.getString(7));
							staff.setLastName(result.getString(8));
							staff.setAddress(result.getString(9));
							
							
							rHouses.add(rHouse);//add to list of rent houses
							staffMembers.add(staff);//add to list of associated estate agent (staff member)
						}
						
						server.sendMessage(rentIDs);//send list of rent ids
						server.sendMessage(rHouses);//send back the array list of rent houses (trans) to the client
						server.sendMessage(staffMembers);//send back the array list of staff members
						break;
				}//end switch
			}//end try block
			catch(ClassNotFoundException e) //Exceptions for various types
			{
				e.printStackTrace();
				server.sendMessage("0");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				server.sendMessage("0");
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
				server.sendMessage("0");
			}
			catch(ClassCastException e)
			{
				server.sendMessage("0");
			}
		}
		while(running);
	
		//If the do-while loop is broken we can output a simple client logged off message
		System.out.println("Client " +id+ " logged off");
		
		//Lastly close the connections 
		db.closeDB();
		server.closeConnections();
	}//End run
}//End class
