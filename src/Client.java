import java.net.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
public class Client
{
    static String file_name;
     
    static int current_head=1;
    static int current_tail=3;
    static int client_server_listener_port=6600;//Listen from servers(tail) on this port
    final static int master_listener_port=6400;//Listen from master on this port
    static int temp_port=6402;
    
    static HashMap<String,Boolean> reply_received=new HashMap<String,Boolean>(); //Track of what replies have been received
    
    
    public static void main(String args[])throws Exception
    {
        Client.file_name = args[1];
        
        //Initializer server details
        Parser.init(args[0]);
        
        ClientRequestSender client_sender_thread=new ClientRequestSender("Request sender thread");
        client_sender_thread.start();
        
        ListenToMasterNotification master_listener = new ListenToMasterNotification("Master listener thread");
        master_listener.start();
        
        ClientReplyReceiver client_reply_rec_thread = new ClientReplyReceiver("Reply rec thread");
        client_reply_rec_thread.start();
        
        Temp_Port_Receiver rec = new Temp_Port_Receiver();
        rec.start();
        
     
    }//end of main()
    
}//end of class Client

//Sends requests to head
class ClientRequestSender implements Runnable
{
    
    
    Thread t=null;
    private String threadname;
    ClientRequestSender(String name)
    {
        this.threadname=name;
    }
    
    public void run()
    {
        LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/Client.log");   
        }
        catch(Exception e)
        {}
        
        Request r[];
        r=ClientUtility.request_reader(Client.file_name);
        
        //Send requests to server
        Socket clientSocketSend=null;//Socket to send requests to head
        
        ObjectOutputStream outSend;
        //System.out.println(r.length);
        int i=0;
        
        while(i<r.length)
        {
            int current_head_client_port=ServerDetails.client_port_list.get(Client.current_head);
            int current_tail_client_port=ServerDetails.client_port_list.get(Client.current_tail);
            
            try
            {
                //Connect to head socket for sending request
                
                clientSocketSend = new Socket("localhost",current_head_client_port);
                outSend=new ObjectOutputStream(clientSocketSend.getOutputStream());
                //Sending out request on Head port
                
                outSend.writeObject(r[i]);
                lc.logger.info("Request sent to Server "+Client.current_head);
                lc.logger.info("Account no: "+r[i].acc_id+" Type:"+r[i].request_type+" Amount:"+r[i].amount);
                Client.reply_received.put(r[i].request_id, false); //Adding to hashmap
                
                //IMPORTANT: Wait for this duration i.e TIMEOUT
                Thread.sleep(8000);
                
                
                if(Client.reply_received.get(r[i].request_id)==true)
                {
                    i++;
                }
                    //clientSocket.close();
            }//end of try
            catch(IOException e)
            {
                lc.logger.info("IOException in Client class main method.");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            catch(InterruptedException e)
            {
                lc.logger.info("InterruptedException in Client class main method.");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            
        }//end of for loop
    }//end of run
    
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
}//end of class ClientRequestSender

class ListenToMasterNotification implements Runnable
{
    private String threadname;
    Thread t=null;
    
    public ListenToMasterNotification(String name)
    {
        this.threadname=name;
    }
    
    public void run()
    {
        LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/Client.log");   
        }
        catch(Exception e)
        {}
        
        try
        {
        //Listen on this port to master
            ServerSocket ss=new ServerSocket(Client.master_listener_port);
            
            while(true)
            {
                Socket s=ss.accept();
                ObjectInputStream inFromMaster=new ObjectInputStream(s.getInputStream());
                ObjectOutputStream outToMaster=new ObjectOutputStream(s.getOutputStream());
                String str=(String)inFromMaster.readObject();
                lc.logger.info("Received from master: "+str);
                String str_char[]=str.split("\\s+");
                String head_or_tail = str_char[0];
                int head_or_tail_id = Integer.parseInt(str_char[1]);
                
                if(head_or_tail.equalsIgnoreCase("Head"))
                {
                    Client.current_head=head_or_tail_id;
                }
                
                
                if(head_or_tail.equalsIgnoreCase("Tail"))
                    Client.current_tail=head_or_tail_id;
                
                s.close();
            }
        }//end of try
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }//end of catch
        
    }//end of run
    
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
    
}//end of class ListenToMasterNotification

class ClientReplyReceiver implements Runnable
{
    Thread t=null;
    private String threadname;

    public ClientReplyReceiver(String name)
    {
        this.threadname=name;
    }
    
    public void run()
    {
        LoggerClass lc=null;
        try{
            lc = new LoggerClass("C:/Logs/Client.log");   
        }
        catch(Exception e)
        {}
        try
        {
            //Listen on port for replies coming from tail and display them
            ServerSocket ss=new ServerSocket(Client.client_server_listener_port);
            while(true)
            {
            Socket s=ss.accept();
            ObjectInputStream inReply = new ObjectInputStream(s.getInputStream());
            //Get reply
            Reply rep=(Reply)inReply.readObject();
            
            
//            if(Client.reply_received.containsKey(rep.request_id))
//                continue;
            
            //mark reply as received in the hashmap
            Client.reply_received.put(rep.request_id, true);
            
            lc.logger.info("REPLY FROM TAIL:");
            lc.logger.info("Request id: "+rep.request_id+" Status:"+rep.status+" Balance:"+rep.balance);
            }//end of while
        }//end of try
        catch(Exception e)
        {
            lc.logger.info("Exception in run() of ClientReplyReceiver.");
            e.printStackTrace();
        }
    }
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
}


class Temp_Port_Receiver implements Runnable
{
Thread t=null;
 public void run()
 {
     while(true)
     {
         try
         {
             ServerSocket ss=new ServerSocket(Client.temp_port);
             Socket s=ss.accept();
             ObjectInputStream in = new ObjectInputStream(s.getInputStream());
             String str=(String)in.readObject();
             System.out.println(str);
             
             String temp[]=str.split("\\s+");
             int new_tail=Integer.parseInt(temp[1]);
             Client.current_tail=new_tail;
             
             s.close();
             ss.close();
             Thread.sleep(2000);
         }
         catch(Exception e)
         {
             
             e.printStackTrace();
         }
     }
 }
 public void start()
 {
     t=new Thread(this);
     t.start();
 }
 


}