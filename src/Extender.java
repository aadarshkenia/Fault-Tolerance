import java.util.*;
import java.net.*;
import java.io.*;

public class Extender
{
    static int server_id=4;
 
    static HashMap<String, Request> requests = new HashMap<String, Request>();//Key = request id and value=Request obj
    static HashMap<String, Reply> computed_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    static HashMap<String,Request> sent_requests = new HashMap<String,Request>(); //Only req identifiers of requests forwarded to succ 
    //Replies that have been sent by tail to client
    static HashMap<String, Reply> sent_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    
    
    public static void main(String args[])throws Exception
    {
        //Initialize all lists as there is no shared memory
        Parser.init(args[0]);
        
        
        //DELAY AFTER WHICH I TELL MASTER TO INITIATE ME
        Thread.sleep(ServerDetails.extender_delay);

        //Start my threads
        start_threads();
        
       
        //Receive updates from old tail
        receive_updates();
       
       
//        while(true)
//        {
//            Thread.sleep(10000);
//            System.out.println("Request size: "+requests.size());
//            System.out.println("Computed reply size: "+computed_replies.size());
//            System.out.println("Req sent size: "+sent_requests.size());
//            System.out.println("Size of sent_replies hashmap: "+sent_replies.size());
//            printHashMap(computed_replies);
//            System.out.println();
//            printHashMap(Extender.sent_replies);
//            //printHashMap(CommHelper.reply_diff(Extender.computed_replies, Extender.sent_replies));
//        }
        
    }//end of main
    
    
    public static void receive_updates() throws Exception
    {
        //Receive updates from old tail and initialize my data structures
        ServerSocket update_recv_socket = new ServerSocket(ServerDetails.extension_ports.get(server_id));
        Socket s =update_recv_socket.accept();
        
        //System.out.println("Accepted conn for updates from old tail.");
        
        ObjectInputStream in=new ObjectInputStream(s.getInputStream());
        
        Extender.requests = (HashMap<String, Request>)in.readObject();
        System.out.println("Received UPDATE from predecessor.");
        Extender.sent_requests = (HashMap<String, Request>)in.readObject();
        System.out.println("Received UPDATE from predecessor.");
        Extender.computed_replies = (HashMap<String, Reply>)in.readObject();
        System.out.println("Received UPDATE from predecessor.");
        Extender.sent_replies = (HashMap<String, Reply>)in.readObject();
        System.out.println("Received UPDATE from predecessor.");
        AccountHashMap.accountList = (HashMap<String, Account>)in.readObject();
        System.out.println("Received UPDATE from predecessor.");
        
        in.close();
    }
    
    
    public static void start_threads()throws Exception
    {
        Server myself=ServerDetails.server_list.get(server_id);
        //System.out.println(myself);
        int m_port = myself.master_port;
        int master_recv_notifications_port=myself.notification_port;
        
        //THREADS
       Pinger extender_process_pinger=new Pinger("Extender pinger",m_port,server_id);
       extender_process_pinger.start();
    
       ReceiveMasterNotifications extender_recv_notifications =new ReceiveMasterNotifications("Extender receive notifications thread", master_recv_notifications_port,server_id);
       extender_recv_notifications.start();
        
       ClientCommunicationReceive client_recv = new ClientCommunicationReceive(server_id);
       client_recv.start();
       
       ServerCommunicationReceive server_recv = new ServerCommunicationReceive(server_id);
       server_recv.start();
       
       ServerCommunicationSend server_send = new ServerCommunicationSend(server_id);
       server_send.start();
       
       Back_Port_Listener back_listener = new Back_Port_Listener(server_id);
       back_listener.start();
       
       Back_Port_Sender back_sender = new Back_Port_Sender(server_id);
       back_sender.start();
    }
    
    public static void printHashMap(HashMap<String,Reply> m)
    {
        if(m==null)
            return;
        
        Iterator it = m.entrySet().iterator();
        
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                Reply rep=(Reply) pair.getValue();
                System.out.println(pair.getKey()+" "+rep.balance);
            }//end of while
    }//end of printHashMap
}
