import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class MiddleServer
{
    static int server_id = 2;
    static int my_pred ;
    
    static HashMap<String, Request> requests = new HashMap<String, Request>();//Key = request id and value=Request obj
    static HashMap<String, Reply> computed_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    static HashMap<String, Request> sent_requests = new HashMap<String, Request>();//Key=request id and value=Reply obj
    //Replies that have been sent by tail to client
    static HashMap<String, Reply> sent_replies = new HashMap<String, Reply>();//Key=request id and value=Reply obj
    
    public static void main(String args[])throws Exception
    {
        //Initialize all lists as there is no shared memory
        Parser.init(args[0]);
        
        my_pred=ServerDetails.predecessor_server_list.get(server_id);
        
        //Get current server object and its details
        Server myself=ServerDetails.server_list.get(server_id);
        int m_port = myself.master_port;
        int master_recv_notifications_port=myself.notification_port;
        
        Pinger middle_process_pinger=new Pinger("Middle pinger",m_port,server_id);
        middle_process_pinger.start();
        
        ReceiveMasterNotifications middle_recv_notifications =new ReceiveMasterNotifications("Middle receive notifications thread", master_recv_notifications_port,server_id);
        middle_recv_notifications.start();
        
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
       
//       while(true)
//        {
//            Thread.sleep(10000);
//            System.out.println("Request size: "+requests.size());
//            System.out.println("Computed reply size: "+computed_replies.size());
//            System.out.println("Req sent size: "+sent_requests.size());
//            System.out.println("Size of sent_replies hashmap: "+sent_replies.size());
//            printHashMap(computed_replies);
//            System.out.println();
//            printHashMap(MiddleServer.sent_replies);
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
//THREAD FOR COMMUNICATION BETWEEN MIDDLE SERVER AND ITS PREDECESSOR

