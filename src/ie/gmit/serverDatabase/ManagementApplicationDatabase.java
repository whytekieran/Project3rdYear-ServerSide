//Package
package ie.gmit.serverDatabase;

//Package Imports
import ie.gmit.serverFunctional.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

//Class that is used for connecting to the application database and provides methods for
//working with the database
public class ManagementApplicationDatabase 
{
	//Instance Variables
	private Connection con;
	private PreparedStatement stmnt;
	private ResultSet results;
	private Server server;
	
	//Constructor
	public ManagementApplicationDatabase(Server server)
	{
		//Initialize the current server object (Holds the current connection with the client)
		this.server = server;
		
		try
		{
			//Create a connection object for the database
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/managementsystem", "root", "");
		}
		catch(SQLException e)
		{
			System.out.println("There has been a problem connecting to the database");
		}
		catch(Exception e)
		{
			System.out.println("Something wrong");
		}
	}//end constructor
	
	//Used for executing SQL select insert, update and delete statments
	public void executeStatement(String statement)
	{
		try
		{
			stmnt.executeUpdate(statement);//execute the statement
		}
		catch (SQLException e)
		{
			//send not okay signal to client
			server.sendMessage("0");
		}

	}//end executeStatement method
	
	//Used for inserting new record then retrieving its generated primary key
	public String insertThenGetKey(String statement)
	{
		String userID = "-1";
		
		try
		{
			//prepare statement
			stmnt = con.prepareStatement(statement, new String[] { "Id" });
		    stmnt.executeUpdate(); //execute it
		    
		    results = stmnt.getGeneratedKeys();//get the generated key
		    if (null != results && results.next())//if its not null 
		    {
		         Long pkTemp = results.getLong(1);//get the new generated id
		         userID = Long.toString(pkTemp);
		    }
		}
		catch (SQLException e)
		{
			//send not okay signal to client
			server.sendMessage("0");
		}
		return userID;
	}//end insertThenGetKey()

	//used for executing SQL select statements, returns a result set
	public ResultSet getResults(String statement)
	{
		try
		{
			stmnt = con.prepareStatement(statement);//Prepare the statement
			results = stmnt.executeQuery();         //execute it
		}
		catch (SQLException e)
		{
			//send not okay signal to client
			server.sendMessage("0");
		}
		return results;
	}//end getResults Method
	
	//Closes the database conection
	public void closeDB()
	{
		try
		{
	       if(stmnt!=null)
	       {
	    	   stmnt.close();
	       }
	    }
		catch(SQLException se)
		{
			JOptionPane.showMessageDialog(null, "The database statements were not closed, database still open",
					"Error", JOptionPane.ERROR_MESSAGE);
 		}
		try
		{
	       if(con!=null)
	       {
	    	   con.close();
	       }
		}
	    catch(SQLException se)
   	    {
	    	JOptionPane.showMessageDialog(null, "The database statements were not closed, database still open",
					"Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
}
