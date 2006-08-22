/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.support;

import java.util.* ;
import java.io.* ;

import org.apache.log4j.Logger;

/**
 *  The support library class of static methods 
 *  for the worklet service 
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 04/07/2006
 */

public class Library {
	
	// various file paths to the service installation & repository files
	public static String wsHomeDir ;
    public static String wsRepositoryDir ;
    public static String wsLogsDir ;
    public static String wsWorkletsDir ;
    public static String wsRulesDir ;
    public static String wsSelectedDir ;
    public static boolean wsPersistOn ;
    public static boolean wsInitialised ;

    public static final String newline = System.getProperty("line.separator");

    private static Logger _log = Logger.getLogger("au.edu.qut.yawl.worklet.support.Library"); 
    

//===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the persistence value
     * read in from web.xml
     * @param setting - true or false as specified in web.xml
     */
     public static void setPersist(boolean setting) {
        wsPersistOn = setting ;
     }

 //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the path to the worklet
     * repository as read in from web.xml
     * @param dir - the path value specified in web.xml
     */
    public static void setRepositoryDir(String dir) {
        dir = dir.replace('\\', '/' );             // switch slashes
        if (! dir.endsWith("/")) dir += "/";       // make sure it has ending slash

        // set the repository dir and the sub-dirs
        wsRepositoryDir = dir ;
        wsLogsDir       = wsRepositoryDir + "logs/" ;
        wsWorkletsDir   = wsRepositoryDir + "worklets/" ;
        wsRulesDir      = wsRepositoryDir + "rules/" ;
        wsSelectedDir   = wsRepositoryDir + "selected/" ;
    }

//===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the actual local file path to
     * the worklet service (as read from the servlet context)
     * @param dir - the local path value to the root of the worklet service
     */
	public static void setHomeDir(String dir) {
        wsHomeDir = dir ;
	}

//===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set a flag when the service has
     * completed initialisation (to prevent multi-initialisations)
     */
    public static void setServicetInitialised() {
        wsInitialised = true ;
    }
	

//===========================================================================//
	
    /** removes the ddd_ part from the front or rear of a taskid */
	public static String getTaskNameFromId(String tid) {
		if (tid.length() == 0) return null ;            // no string passed
        if (tid.indexOf('_') == -1) return tid ;        // no change required

        String[] split = tid.split("_");

        // find out which side has the decomp'd taskid
        char c = tid.charAt(0);

        if (Character.isDigit(c))                      // if tid starts with a digit
           return split[1] ;                           // return name after the '_'
        else
           return split[0] ;                           // return name before the '_'
	}
	
//===========================================================================//
	
	/** returns a string of characters of length 'len' */  	
   	public static String getSepChars(int len) {
   		StringBuffer sb = new StringBuffer(len) ;
	    for (int i=0;i<len;i++) sb.append('/') ;
	    return sb.toString() ;
   	}
   	
//===========================================================================//
	
    /**
     *  converts the contents of a file to a String
     *  @param fName the name of the file
     *  @return the String representing the file's contents
     */ 	
   	public static String FileToString(String fName) {
       String fLine ;
       StringBuffer result = new StringBuffer();

       try {
       	  if (! fileExists(fName)) return null ;     // don't go further if no file
       	  
          FileReader fread = new FileReader(fName); 
          BufferedReader bufread = new BufferedReader( fread );

          fLine = bufread.readLine() ;        // read first line
          while( fLine != null ) {            
             result.append(fLine) ;
             fLine = bufread.readLine() ;     // read next line
          }
          bufread.close();
          fread.close();
          return result.toString() ; 
       }
       catch( FileNotFoundException fnfe ) {
          _log.error( "File not found! - " + fName, fnfe ) ;
          return null ;
       }
       catch( IOException ioe ) {
          _log.error( "IO Exception when reading file - " + fName, ioe ) ;
          return null ;
       }
   }
   	
//===========================================================================//

   /** returns true if the file is found */	
   public static boolean fileExists(String fName) {
      File f = new File(fName) ;
      return f.exists() ;        	
   }

//===========================================================================//

    /** returns a list of objects as a String of csv's */
    public static String listItems(List list) {
        String s = "" ;
        for (Object o : list) {
            s += o + ", ";
        }
        return s ;
    }

//===========================================================================//

    /** appends a formatted line with the passed title and value to the StringBuffer */
    public static StringBuffer appendLine(StringBuffer s, String title, String item){
        if (title == null) title = "null" ;
        if (item == null) item = "null" ;
        s.append(title); s.append(": "); s.append(item); s.append(newline);
        return s ;
    }

//===========================================================================//

    /** appends an XML formatted line with the passed tag and value to the StringBuffer */
    public static StringBuffer appendXML(StringBuffer s, String tag, String value) {
        String open  = '<' + tag + '>' ;
        String close = "</" + tag + '>';

        // replace all <'s and &'s with unmarkedup equivalents
        if (value.indexOf('&') > -1) value = value.replaceAll("&", "&amp;");
        if (value.indexOf('<') > -1) value = value.replaceAll("<", "&lt;");

        s.append(open); s.append(value); s.append(close);
        return s;
    }

   
//===========================================================================//
//===========================================================================//
	
}  // ends

