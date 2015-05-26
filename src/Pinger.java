
import java.io.*;
import java.net.*;

//Pinger class to create a thread for pinging master continuously
public class Pinger implements Runnable
{
   private int server_id;
   private int port; 
   private Thread t;
   private String threadName;
   
   Pinger(String name, int port, int id)
   {
       this.server_id=id;
       this.port=port;
       this.threadName = name;
       
   }

    public void run()
    {
        String log_name="";
        if(server_id==4)
            log_name="Extender";
        if(server_id==3)
            log_name="Tail";
        if(server_id==2)
            log_name="MiddleServer";
        if(server_id==1)
            log_name="Head";
        
        LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/"+log_name+".log");   
        }
        catch(Exception e)
        {}
        
       lc.logger.info("Running thread " +  threadName );
       
       //lc.logger.info("size for : "+server_id+" : "+AccountHashMap.accountList.size());

       ObjectOutputStream outToMaster=null;
       ObjectInputStream inFromMaster=null;
      
            try
            {
                
                // Create the Server Socket (Server is the client here)
                Socket masterSocket = new Socket("localhost",port);
                lc.logger.info("Socket Extablished...");
                // Create input and output streams to client
                outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                inFromMaster = new ObjectInputStream(masterSocket.getInputStream());
             }

             catch(IOException e)
             {
                 //lc.logger.info("IOException at Pinger");
             }
            
            try
            {
                if(server_id==4)
                {
                    outToMaster.writeObject("Extend me");
                }
                
                int counter=0;
                int my_counter = ServerDetails.server_list.get(server_id).count;
                
                while(true)
                {
//                    //Adding an intentional failure for head server after sending 3 pings
//                    if(counter==6 && server_id==1)  //   ----  Put if here !!
//                    {
//                        fail_me(); //Changes hashmap accordingly
//                        Thread.sleep(10000);
//                    }
                    
                    my_counter--;
                    
                    if(my_counter==0)
                    {
                        fail_me();
                        Thread.sleep(10000);
                        break;
                    }
                    
                    Thread.sleep(1000);
                    String s=new String("I am Alive");
                    outToMaster.writeObject(s);
                    String reply_from_server = (String)inFromMaster.readObject();
                    //lc.logger.info("Received from Master: "+reply_from_server);
                    counter++;
                    
                }
            }
            catch(IOException e)
            {
                //lc.logger.info("Here");
                //lc.logger.info("IOException in run of Pinger. ");
            }
            catch(InterruptedException e)
            {
               // lc.logger.info("Interrupted exception in run of Pinger ");
            }
            catch(ClassNotFoundException e)
            {
               // lc.logger.info("ClassNotFound exception in run of Pinger ");
            }
    }//end of run method for thread
    
    //I am failing, so change the hashmap acordingly
    public void fail_me()
    {
        //lc.logger.info("Timestamp I fail and set my succ and pred to -1."+System.currentTimeMillis());
        
        //Updating my data structures
        if(server_id==ServerDetails.current_head_server_id)//If I am current head and I am failing
        {
            ServerDetails.current_head_server_id = ServerDetails.successor_server_list.get(server_id);
        }   
        if(server_id==ServerDetails.current_tail_server_id)
        {
            ServerDetails.current_tail_server_id = ServerDetails.predecessor_server_list.get(server_id);
        }
        
        ServerDetails.successor_server_list.put(server_id, -1);
        ServerDetails.predecessor_server_list.put(server_id, -1);
    }
   
    public void start ()
    {
        
      //lc.logger.info("Starting thread : " +  threadName );
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
    }//end of start()

}//end of class Pinger

