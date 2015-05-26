import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Tail
{
    static int server_id = 3;
    
    static HashMap<String, Request> requests = new HashMap<String, Request>();//Key = request id and value=Request obj
    static HashMap<String, Reply> sent_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    static HashMap<String, Reply> computed_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    static HashMap<String, Request> sent_requests = new HashMap<String, Request>();//Key=request id and value=Reply obj
    
    public static void main(String args[])throws Exception
    {
        //Initialize all lists as there is no shared memory
        Parser.init(args[0]);
        
        //Get current server object and its details
        Server myself=ServerDetails.server_list.get(server_id);
        int m_port = myself.master_port;
        int master_recv_notifications_port=myself.notification_port;
        
        Pinger tail_process_pinger=new Pinger("Tail pinger",m_port,server_id);
        tail_process_pinger.start();
        
        ReceiveMasterNotifications tail_recv_notifications =new ReceiveMasterNotifications("Tail receive notifications thread", master_recv_notifications_port,server_id);
        tail_recv_notifications.start();
     
       ClientCommunicationReceive client_recv = new ClientCommunicationReceive(server_id);
       client_recv.start();
       
       ServerCommunicationReceive server_recv = new ServerCommunicationReceive(server_id);
       server_recv.start();
       
       ServerCommunicationSend server_send = new ServerCommunicationSend(server_id);
       server_send.start();
       
       
       Back_Port_Sender back_sender = new Back_Port_Sender(server_id);
       back_sender.start();
       
       
//       System.out.println("Actual tail ref: "+sent_replies);
//       while(true)
//        {
//            Thread.sleep(5000);
//            System.out.println("Request size: "+requests.size());
//            System.out.println("Computed reply size: "+computed_replies.size());
//            System.out.println("Req sent size: "+sent_replies.size());
//            System.out.println("Size of sent_replies hashmap: "+sent_replies.size());
//            printHashMap(computed_replies);
//            System.out.println();
//            printHashMap(Tail.sent_replies);
//        }
       
    }//end of main
    
    
    public static void printHashMap(HashMap<String,Reply> m)
    {
        Iterator it = m.entrySet().iterator();
        
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                Reply rep=(Reply) pair.getValue();
                System.out.println(pair.getKey()+" "+rep.balance);
            }//end of while
    }//end of printHashMap
}
