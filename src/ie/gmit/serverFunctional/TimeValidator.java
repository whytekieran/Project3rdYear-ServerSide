package ie.gmit.serverFunctional;

import java.util.regex.*;

public class TimeValidator 
{
	private Pattern pattern;
	private Matcher matcher;
	private static final String TIME12HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	public TimeValidator()
	{
		pattern = Pattern.compile(TIME12HOURS_PATTERN);
	}

	public boolean validateTime(final String time)
	{        
		matcher = pattern.matcher(time);
		return matcher.matches();            
	}
}
