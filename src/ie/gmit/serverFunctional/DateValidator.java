//Package
package ie.gmit.serverFunctional;

//Package Imports
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//Class used to validate dates (make sure a date is actually a valid date) and also used
//to make sure a date (start and end) entered by the user does not overlap with another
//date (start and end) which is used with rent of a house
public class DateValidator 
{
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	//Checks if a date actually exists
	public boolean isValidDate(String date) 
	{
		//Create simple date format and specify its date format
	    df.setLenient(false);
	    
	    try 
	    {
	      //Try and parse the argument to the method as a date, return if it was true or false
	      df.parse(date.trim());
	    } 
	    catch(ParseException e) //If there is a problem parsing the string as a date
	    {
	      return false; //return false
	    }
	    
	    return true; //if the date is parsed successfully its valid and can return true
	    
	}//end isValidDate()
	
	//Method used to make sure a start and end date end by the user does not overlap with one that exists
	public boolean noRentDateOverlapping(String startDateEntered, String endDateEntered, String startDateDatabase,
			String endDateDatabase)
	{
		//Create boolean for the answer, a date formatter, and four date objects.
		boolean noDateOverlap;
		Date startDateE = null;
		Date endDateE = null;
	    Date startDateD = null; 
	    Date endDateD = null; 
		
		try 
		{
			//Initialize the dates
			startDateE = df.parse(startDateEntered);
			endDateE = df.parse(endDateEntered);
		    startDateD = df.parse(startDateDatabase); 
		    endDateD = df.parse(endDateDatabase); 
		} 
		catch(ParseException e) 
		{
			e.printStackTrace();
		} 
	    
	    if((startDateE.after(startDateD) && startDateE.before(endDateD)) || 
	    		(endDateE.after(startDateD) && endDateE.before(endDateD)))
	    {
	    	//If start date or the end date are inside the existing start and end date we have overlap
	    	noDateOverlap = false;
	    }
	    else if(startDateE.before(startDateD) && endDateE.after(endDateD))
	    {
	    	//If start date entered is before start date existing and end date entered is after end date
	    	//existing then there is overlap
	    	noDateOverlap = false;
	    }
	    else if(startDateE.equals(startDateD) && endDateE.equals(endDateD))
	    {
	    	//If a start and end date equal an existing start and end date there is overlap
	    	noDateOverlap = false;
	    }
	    else//otherwise
	    {
	    	//No dates overlap
	    	noDateOverlap = true;
	    }
	    
	    return noDateOverlap; //return the result
	}//end isStartOrEndBetween()
	
	//checks if two dates are equal
	public boolean dateEquals(String date1, String date2)
	{
		Date compareDate1 = null;
		Date compareDate2 = null;
		
		try 
		{
			//Initialize the dates
			compareDate1 = df.parse(date1);
			compareDate2 = df.parse(date2);
		} 
		catch(ParseException e) 
		{
			e.printStackTrace();
		}
		
		if(compareDate1.equals(compareDate2))
		{
			return true;
		}
		else
		{
			return false;
		}
	}//end dateEquals
}//end class DateValidator
