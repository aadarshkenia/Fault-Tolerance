import java.io.*;
import java.net.*;
import java.util.*;

//Thread to check time diff between current time and timestamp
class MonitorTimestamp implements Runnable
{
    private Thread t;
    private String threadname;
    int server_id;

    //Constructor
    public MonitorTimestamp(String s, int id)//Threadname and id
    {
        this.threadname = s;
        this.server_id = id;
    }
    
    public void run()
    {
        try
        {
            LoggerClass lc=null;
            try{
                lc = new LoggerClass("C:/Logs/Master.log");   
            }
            catch(Exception e)
            {}
            
            while(true)
            {
                Thread.sleep(5000);
                if(System.currentTimeMillis()-TimeStampValue.timestamps.get(server_id)>1500)
                {
                    lc.logger.info("SERVER "+server_id+" FAILED.");
                    TimeStampValue.isAlive.put(server_id, false);
                    
                    
                    //INFORM ALL THREADS ABOUT THE FAILURE OF THIS SERVER
                    send_failure_notification();
                    lc.logger.info("Notifying other servers of failure.");
                    //lc.logger.info("Timestamp pred stops listening:"+System.currentTimeMillis());
                    
                    if(server_id==ServerDetails.current_head_server_id)
                    {
                        //Change head in ServerDetails
                        ServerDetails.current_head_server_id = ServerDetails.successor_server_list.get(server_id);
                        //Notify client
                        notify_head_change_to_client();
                        
                    }
                    if(server_id==ServerDetails.current_tail_server_id)
                    {
                        //Change tail in ServerDetails
                        ServerDetails.current_tail_server_id=ServerDetails.predecessor_server_list.get(server_id);
                        //Notify client
                        notify_tail_change_to_client();
                    }
                    
                    break;
                }
            }
        }
        catch(InterruptedException e)
        {
//            lc.logger.info("InterruptedException in run() of MonitorTimestamp thread");
//            e.printStackTrace();
        }
        catch(IOException e)
        {
//            System.out.println(e.getMessage());
//            lc.logger.info("IOException in run() of MonitorTimestamp thread");
//            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            //lc.logger.info("ClassNotFoundException in run() of MonitorTimestamp thread");
            //e.printStackTrace();
        }
    }
    public void start()
    {
        if(t==null)
        {
            t=new Thread(this);
            t.start();
        }
    }
    
    //NOTIFYING SERVER FAILURE TO SERVERS (PREDECESSOR SERVER)
    public void send_failure_notification() throws IOException,ClassNotFoundException
    {
        
        //Send notification to call other ports of other servers
        
        Iterator it = ServerDetails.server_list.entrySet().iterator();
        
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            if((Integer)pair.getKey()==server_id) //Do nothing on my own port
                continue;
            else//Send notifications to all other server ids
            {
                int key=(Integer)pair.getKey();
                Server s=(Server)pair.getValue();
                //lc.logger.info("Notif port: "+s.notification_port);
                Socket tempSocket = new Socket("localhost",s.notification_port);
                ObjectInputStream inFromServer=new ObjectInputStream(tempSocket.getInputStream());
                ObjectOutputStream outToServer=new ObjectOutputStream(tempSocket.getOutputStream());
                
                outToServer.writeObject("SERVER "+server_id+" FAILED.");
                String reply_from_server = (String)inFromServer.readObject();
                if(reply_from_server.equalsIgnoreCase("OK"))
                tempSocket.close();
            }
        }//end of while
        
        
    }//end of send_failure_notification()
    
    public void notify_head_change_to_client()throws IOException
    {
        int new_head=ServerDetails.successor_server_list.get(server_id);
        Socket s=new Socket("localhost",Master.client_port);
        ObjectOutputStream out=new ObjectOutputStream(s.getOutputStream());
        out.writeObject("Head "+new_head+" is new head.");
    }
    
    public void notify_tail_change_to_client() throws IOException
    {
        int new_tail=ServerDetails.predecessor_server_list.get(server_id);
        Socket s=new Socket("localhost",Master.client_port);
        ObjectOutputStream out=new ObjectOutputStream(s.getOutputStream());
        out.writeObject("Tail "+new_tail+" is new tail.");
    }
    
}//end of class MonitorTimestamp






//Thread to receive alive messages from servers and update timestamp value
class MonitorServers implements Runnable 
{
   private int port; //Port num on which connected to servers, set using constructor
   private Thread t;
   private String threadName;
   private int server_id; //Required to know which server's thread id it is
   //Taking this outside so that they are accessible to all methods for writing to this SERVER
   Socket masterSocket = null;
   ObjectOutputStream outToServer = null;
   ObjectInputStream inFromServer = null;
   
   MonitorServers(String name, int num) //Thread name and server id, can get port from server id
   {
       this.threadName = name;
       this.server_id=num;
       this.port=ServerDetails.server_list.get(server_id).master_port;
       //lc.logger.info("Creating " +  threadName );
   }

   public void run() 
   {
       LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/Master.log");   
        }
        catch(Exception e)
        {}
        
        lc.logger.info("Running thread : " +  threadName );
        
        try
        {
            //Creating a ServerSocket for communication to server
            ServerSocket s=new ServerSocket(port);
            Socket masterSocket=s.accept();
            
            // Create the input & output streams to the server
            outToServer = new ObjectOutputStream(masterSocket.getOutputStream());
            inFromServer = new ObjectInputStream(masterSocket.getInputStream());
        }
        catch(IOException e)
        {
            //lc.logger.info("Socket communication error at Master inside MonitorServers.");
        }
    
        try
        {
            
            //**********************************
            if(TimeStampValue.timestamps.get(server_id)==null)
                TimeStampValue.timestamps.put(server_id, System.currentTimeMillis());
            
           
            
            int counter=0;
            while(true)
            {
                //Receiving isAlive messages from servers
                String s=(String)inFromServer.readObject();
                
                
                if(s.equalsIgnoreCase("Extend me") && server_id==4)
                {
                    lc.logger.info("Request for EXTENDING CHAIN from Server "+server_id);
                    doExtensionWork();
                    continue;
                }
                
                
                counter++;
                
                //On getting first ping
                if(counter==1)
                {
                     //Starting thread to monitor time diff with SAME THREADNAME
                        MonitorTimestamp mt=new MonitorTimestamp(threadName,server_id);
                        mt.start();
                        
                        TimeStampValue.isAlive.put(server_id, true);
                }
                
                
                if(TimeStampValue.isAlive.get(server_id)==false)
                    break;
                
                //Set timestamp value to system time now
                TimeStampValue.timestamps.put(server_id, System.currentTimeMillis());
                //lc.logger.info("Received from server : "+s);
                
                String reply="Okay, got it!";
                outToServer.writeObject(reply);
            }
        }
        catch(IOException e)
        {
            //lc.logger.info("IOException at server monitoring thread run method of Monitorserver.");
        }
        catch(ClassNotFoundException e)
        {
            //lc.logger.info("ClassNotFound exception in run() of Pinger ");
        }
        //catch(InterruptedException e)
        //{
        //    lc.logger.info("Interrupted exception in run() of Pinger.");
        //}
      
   }//end of run()
   
   
   public void doExtensionWork()
   {
    try
        {
                LoggerClass lc=null;
                try{
                    lc = new LoggerClass("C:/Logs/Master.log");   
                }
                catch(Exception e)
                {}
            
            
                //Start extension procedure
                
                //Notify other servers
                Iterator it = ServerDetails.server_list.entrySet().iterator();
        
                while (it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    int key = (Integer)pair.getKey();
                    //if(key!=server_id)
                    //{
                        Server val=(Server)pair.getValue();
                        
                        //Send "Extend 4" to this server
                        
                        ObjectOutputStream out = CommHelper.get_Socket_Output(val.notification_port);
                        out.writeObject("Extend "+server_id);
                    //}
                }//end of while
                
                
                //Notify client of new tail
               ObjectOutputStream client_notif = CommHelper.get_Socket_Output(Client.temp_port);
               client_notif.writeObject("Tail "+server_id+" is new tail.");
               lc.logger.info("Tail "+server_id+" is new tail.");
               client_notif.close();
               
               //Change my data structures
               int old_tail = ServerDetails.current_tail_server_id;
               ServerDetails.predecessor_server_list.put(server_id,old_tail);
               ServerDetails.successor_server_list.put(server_id, -1);
               ServerDetails.successor_server_list.put(old_tail, server_id);
               
               ServerDetails.current_tail_server_id = server_id;
               Client.current_tail=server_id;
        
        }
        catch(Exception e)
        {
            //lc.logger.info("Exception inside doExtensionWork()");
            //e.printStackTrace();
        }
   }
   
   
   
   public void start ()
   {
      
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
   }

}//end of class MonitorServers






    public class Master
    {
    //Send notification of head/tail failure on this port as Client listens on this port
    static int client_port = 6400; //Master.log listens on this port    
    
    public static void main(String args[]) throws Exception
   {
       Parser.init(args[0]);
     
       
       MonitorServers head_monitor = new MonitorServers("Head pinger", 1);
       MonitorServers middle_monitor = new MonitorServers("Middle pinger", 2);
       MonitorServers tail_monitor = new MonitorServers("Tail pinger", 3);
     
      
       
       head_monitor.start();
       middle_monitor.start();
       tail_monitor.start();
       
       if(ServerDetails.server_list.size()==4)
       {
        MonitorServers extend_monitor = new MonitorServers("Extend pinger", 4);   // --if
        extend_monitor.start();  //-ff
       }
       
       try{
       while(true)
       {
           Thread.sleep(5000);
           //ServerDetails.printHashMap(ServerDetails.predecessor_server_list);
       }
       }
       catch(Exception e)
       {
           //System.out.println(e.getMessage());
       }
       
   }   
}//end of class Client