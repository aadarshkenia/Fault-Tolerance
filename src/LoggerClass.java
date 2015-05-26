import java.util.logging.*;
import java.io.*;

public class LoggerClass
{
   
    FileHandler fh=null;
    Logger logger=null;
    
    LoggerClass(String s)throws Exception
    {
        fh=new FileHandler(s);
        logger = Logger.getLogger("My log");
        logger.addHandler(fh);
        fh.setFormatter(new SimpleFormatter());
        
    }
    
}
