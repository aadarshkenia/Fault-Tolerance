import java.io.*;
import java.util.*;

//Gives important functionalities to client process
//Like reading requests from files etc

public class ClientUtility
{
    public static Request[] request_reader(String file) 
    {
        String line=null;
	int length=0;		
	String current_directory=System.getProperty("user.dir");
	String new_directory=current_directory+"\\"+"config"+"\\"+file;//FIle name is an argument to func
        
        Request r[]=null;
        try{
        BufferedReader reader = new BufferedReader(new FileReader(new_directory));
	
        StringTokenizer st=null;
        length=Integer.parseInt(reader.readLine());
        r=new Request[length];

        for(int i=0;i<length;i++)
        {
            line=reader.readLine();
            //Parse the line and process it
            line=line.substring(1,line.length()-1);
            //System.out.println(line);
            st=new StringTokenizer(line,",");
            String req_id = st.nextToken();//Newly formed
            String req_type = st.nextToken();
            String amt = st.nextToken();
            //NOTE: seq no is incremented inside the request constructor

            r[i]=new Request(req_id,req_type,Integer.parseInt(amt));
		
        }//end of for  loop
	
        }
        
        catch(IOException e)
        {
            System.out.println("IOException in requestreader().");
        }
        
	return r;
	}//end of request_reader()
    
}
