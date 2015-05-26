import java.util.logging.*;
import java.io.IOException;

class Test
{
    public static void main(String args[]) throws Exception
    {
       
        Logger logger = Logger.getLogger("MyLog");  
        FileHandler fh;  

        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("C:/Logs/Myfile.log");  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

            // the following statement is used to log any messages  
            logger.log(Level.SEVERE,"My first log");  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  

    logger.info("Hi How r u?");  

}//end of main
        
}//end of class