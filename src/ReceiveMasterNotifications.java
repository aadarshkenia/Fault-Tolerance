
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//Server thread waits for notifications from master here.
public class ReceiveMasterNotifications implements Runnable {
   
   private int server_id; 
   private int port; 
   private Thread t;
   private String threadName;
   
   ReceiveMasterNotifications(String name, int port, int id)
   {
       this.port=port;
       this.threadName = name;
       this.server_id=id;
   }

    public void run()
    {
        LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/MasterNotifications.log");   
        }
        catch(Exception e)
        {}
       lc.logger.info("Running " +  threadName + " for receiving master notifications.");

       ObjectOutputStream outToMaster=null;
       ObjectInputStream inFromMaster=null;
      
            try
            {
                ServerSocket welcomeSocket = new ServerSocket(port);
                // Create the Client Socket
                //lc.logger.info("Receive notification socket established on port "+port);
                
                
                while(true)
                {
                    Socket masterSocket = welcomeSocket.accept();
                    // Create input and output streams to client
                    outToMaster = new ObjectOutputStream(masterSocket.getOutputStream());
                    inFromMaster = new ObjectInputStream(masterSocket.getInputStream());
                    
                    
                    //Thread waits on readObject()
                    String s=(String)inFromMaster.readObject();
                    
                    //GOT FAILED SERVER IS HERE.
                    String temp[]=s.split("\\s+");
                    
                    //Notification for server failure
                    if(temp[0].equalsIgnoreCase("Server"))
                    {
                        int failed_server_id = Integer.parseInt(temp[1]);

                        lc.logger.info("\nSERVER "+failed_server_id+" FAILED.\n");

                        //Change my data structures accordingly
                        if(failed_server_id==ServerDetails.current_head_server_id)
                        {
                            int succ = ServerDetails.successor_server_list.get(failed_server_id);

                            ServerDetails.current_head_server_id=succ;

                            ServerDetails.successor_server_list.put(failed_server_id, -1);

                            ServerDetails.predecessor_server_list.put(succ, -1);
                        }
                        else if(failed_server_id==ServerDetails.current_tail_server_id)
                        {
                            int pred = ServerDetails.predecessor_server_list.get(failed_server_id);

                            ServerDetails.current_tail_server_id=pred;

                            ServerDetails.predecessor_server_list.put(failed_server_id, -1);

                            ServerDetails.successor_server_list.put(pred, -1);
                        }
                        else
                        {   
                            int succ = ServerDetails.successor_server_list.get(failed_server_id);
                            int pred = ServerDetails.predecessor_server_list.get(failed_server_id);
                            ServerDetails.successor_server_list.put(failed_server_id, -1);
                            ServerDetails.predecessor_server_list.put(failed_server_id, -1);

                            ServerDetails.predecessor_server_list.put(succ, pred);
                            ServerDetails.successor_server_list.put(pred, succ);

                        }
                        //Change some pending data structures
                        Client.current_tail=failed_server_id;
                        
                        
                        outToMaster.writeObject("OK.");

                        inFromMaster.close();
                        outToMaster.close();
                    }
                    
                    //Master will send "Extend 4" to servers
                    //NOTE: Newly created tail wont receive this
                    
                    if(temp[0].equalsIgnoreCase("Extend"))
                    {
                        lc.logger.info(" RECEIVED :"+s);
                        //Getting server id of extending server
                        int extender_id=Integer.parseInt(temp[1]);
                        
                        
                        //I am tail and there is to be an extension
                        if(server_id==ServerDetails.current_tail_server_id)
                        {
//                            lc.logger.info("Tail part of extenision. ");

                            ServerDetails.successor_server_list.put(server_id, extender_id);
                            ServerDetails.successor_server_list.put(extender_id, -1);
                            ServerDetails.predecessor_server_list.put(extender_id, server_id);
                            
                            //Giving data structures to new tail on its extension port
                            Socket new_tail_socket=new Socket("localhost",ServerDetails.server_list.get(extender_id).extension_port);
                            ObjectOutputStream out = new ObjectOutputStream(new_tail_socket.getOutputStream());
                            
                            lc.logger.info("Send UPDATE to new server.");
                            out.writeObject(Tail.requests);
                            lc.logger.info("Send UPDATE to new server.");
                            out.writeObject(Tail.sent_requests);
                            lc.logger.info("Send UPDATE to new server.");
                            out.writeObject(Tail.computed_replies);
                            lc.logger.info("Send UPDATE to new server.");
                            out.writeObject(Tail.sent_replies);
                            lc.logger.info("Send UPDATE to new server.");
                            out.writeObject(AccountHashMap.accountList);
                            
                            out.close();
                            
                            //lc.logger.info("Forwarded state to new tail.");
                        }
                        else//For other servers
                        {
                            int current_tail = ServerDetails.current_tail_server_id;
                            ServerDetails.successor_server_list.put(current_tail, extender_id);
                            ServerDetails.successor_server_list.put(extender_id, -1);
                            ServerDetails.predecessor_server_list.put(extender_id, current_tail);
                        }
                    
                        ServerDetails.current_tail_server_id = extender_id;
                        
                        lc.logger.info("Extension complete.");
                    }//if for doing extension work
                    
                    
                    
                    
                }//end of while
             }//end of try
             catch(IOException e)
             {
                 System.out.println(e.getMessage());
                 e.printStackTrace();
                 lc.logger.info("IOException at ReceiveMasterNotifications");
             }
             catch(ClassNotFoundException e)
             {
                System.out.println(e.getMessage());
                lc.logger.info("Class not found notification at ReceiveMasterNotifications.");
             }
                
            
            
     }//end of run method for thread
   
    public void start()
    {
        t=new Thread(this);
        t.start();
   
    }
    
}//end of class ReceiveMasterNotifications
