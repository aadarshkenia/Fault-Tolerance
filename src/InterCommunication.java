
import java.io.*;
import java.net.*;
import java.util.*;

/*Activate listeners on all ports-> that is for each of the servers, start its client port on which it listens to
incoming clients and also start server_listener ports.*/


class ClientCommunicationReceive implements Runnable
{
    int server_id;
    Thread t=null;
    public ClientCommunicationReceive(int id)
    {
        this.server_id=id;
    }
    
    public void run()
    {
         //Start listening on client port
    try
    {
        //Listen on this port for client requests
        ServerSocket ss_client=new ServerSocket(ServerDetails.client_port_list.get(server_id));
        //Note: Request wont come here unless client sends it to a head, but every server listens here continously
        
       
        while(true)
        {
           
            ObjectInputStream inFromClient=CommHelper.get_ServerSocket_Input(ss_client); //CHANGE 1
            
            Request req = (Request)inFromClient.readObject();
          
            //lc.logger.info("Request from client: \nAccount number:"+req.acc_id+" Request ID:"+req.request_id+" Type:"+req.request_type+" Amount:"+req.amount);
            
//            if(server_id>2)
//            {
//                lc.logger.info("Cant add to hashmap ryt now, not designed for failure");
//            }
            
            if(server_id==ServerDetails.current_head_server_id)
            {
            
                if(server_id==1)
                {

                    //If i have received same request earlier ignore it
                    if(Head.requests.containsKey(req.request_id))
                        continue;


                    //Add request to hashmap of received requests
                    Head.requests.put(req.request_id, req);


                    //Thread.sleep(1000);
                    //Add reply to hashmap of computed computed_replies
                    Reply rep = RequestProcessor.processRequest(req);
                    Head.computed_replies.put(req.request_id, rep);
                }

                if(server_id==2)
                {
                    if(MiddleServer.requests.containsKey(req.request_id))
                        continue;


                    //Add request to hashmap of received requests
                    MiddleServer.requests.put(req.request_id, req);


                    //Thread.sleep(1000);
                    //Add reply to hashmap of computed computed_replies
                    Reply rep = RequestProcessor.processRequest(req);
                    MiddleServer.computed_replies.put(req.request_id, rep);
                }
            }
            
            
        }//end of while
    }//end of try
    
    catch(Exception e)
    {
        e.printStackTrace();
    }
    
    }//end of run()
    
    public void start()
    {
        
        t=new Thread(this);
        t.start();
    }
}//end of class ClientCommunicationReceive






class ServerCommunicationReceive implements Runnable
{
    private int server_id;
    Thread t=null;
    
    public ServerCommunicationReceive(int id) 
    {
        this.server_id=id;
    }
    
    public void run()
    {
    //Start listening on server ports
        
    try
    {
        //Listening on this port - ALL SERVERS
        ServerSocket ss_server=new ServerSocket(ServerDetails.server_list.get(server_id).server_listener_port);
        //Input stream from incoming socket connection
        
        
        while(true)
        {
            ObjectInputStream inFromPred=CommHelper.get_ServerSocket_Input(ss_server);//CHANGE 2

            //Receive sent{} of predecessor
            Request req = (Request)inFromPred.readObject();
            //lc.logger.info("Received request from pred. ");
            
            //Add request to hashmap of received requests and add reply to hashmap 
            if(server_id==2)
            {
                //Check if request already there
                if(!MiddleServer.requests.containsKey(req.request_id))
                {
                    LoggerClass lc=null;
                    try{
                        lc = new LoggerClass("C:/Logs/MiddleServer.log");   
                    }
                    catch(Exception e)
                    {}
                    
                    lc.logger.info("Receiving request from : "+req.request_id);
                    Reply rep = RequestProcessor.processRequest(req);
                    MiddleServer.requests.put(req.request_id, req);
                    MiddleServer.computed_replies.put(req.request_id, rep);
                }
            }
            if(server_id==3)
            {
                if(!Tail.requests.containsKey(req.request_id))
                {
                    LoggerClass lc=null;
                    try{
                        lc = new LoggerClass("C:/Logs/Tail.log");   
                    }
                    catch(Exception e)
                    {}
                    lc.logger.info("Receiving request from : "+req.request_id);
                    Reply rep = RequestProcessor.processRequest(req);
                    Tail.requests.put(req.request_id,req);
                    Tail.computed_replies.put(req.request_id, rep);
                }
            }
             if(server_id==4)
            {
                if(!Extender.requests.containsKey(req.request_id))
                {
                    LoggerClass lc=null;
                    try{
                        lc = new LoggerClass("C:/Logs/Extender.log");   
                    }
                    catch(Exception e)
                    {}
                    lc.logger.info("Receiving request from : "+req.request_id);
                    Reply rep = RequestProcessor.processRequest(req);
                    Extender.requests.put(req.request_id,req);
                    Extender.computed_replies.put(req.request_id, rep);
                }
            }

        }

    }   
    catch(Exception e)
    {
        //lc.logger.info("Exception in run() of ServerCommunication Receive.");
        e.printStackTrace();
    }
    
    }//end of run()
    
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
}//end of class ServerCommunicationReceive



class ServerCommunicationSend implements Runnable
{
    private int server_id;
    Thread t=null;
    
    public ServerCommunicationSend(int id)
    {
        this.server_id=id;
    }
    
    public void run()
    {
        
            try
            {
                
                while(true)
                {
                    //SENDING TO CLIENT
                    if(server_id==ServerDetails.current_tail_server_id)//I am tail
                    {
                        //lc.logger.info("I am tail");
                        //I dont send frequently, computation is expensive, I check once in a while.
                        Thread.sleep(1000);

                        //Compute replies to be sent
                        HashMap<String,Reply> toBeSent=null;
                        if(server_id==4)
                        {
                             toBeSent = CommHelper.reply_diff(Extender.computed_replies, Extender.sent_replies);
                        }
                        
                        if(server_id==3)
                        {
                             toBeSent = CommHelper.reply_diff(Tail.computed_replies, Tail.sent_replies);
                        }
                        if(server_id==2)
                        {
                            toBeSent = CommHelper.reply_diff(MiddleServer.computed_replies, MiddleServer.sent_replies);
                        }

                        if(toBeSent!=null)
                        {

                            serial_sender(Client.client_server_listener_port, toBeSent);

                            //Once sent, add these replies to sent_replies set
                            if(server_id==3)
                                CommHelper.add_reply_diff(toBeSent, Tail.sent_replies);
                            if(server_id==2)
                                CommHelper.add_reply_diff(toBeSent, MiddleServer.sent_replies);
                            if(server_id==4)
                                CommHelper.add_reply_diff(toBeSent, Extender.sent_replies);
                        }
                    }//end of if for tail
                    
                    else //For head or middle server
                    {
                     //System.out.println();
                        
                        HashMap<String, Request> toBeSent=null;

                        if(server_id==1)
                        {
                            toBeSent = CommHelper.request_diff(Head.requests, Head.sent_requests);
                        }//For each reply in toBeSent, send it to client
                        if(server_id==2)
                        {
                            toBeSent = CommHelper.request_diff(MiddleServer.requests, MiddleServer.sent_requests);
                        }
                        if(server_id==3)
                        {
                            toBeSent = CommHelper.request_diff(Tail.requests, Tail.sent_requests);
                        }

                        if(toBeSent!=null)
                        { 

                            int succ_id=ServerDetails.successor_server_list.get(server_id);
                            //Get output stream of my successor
                            
                            if(succ_id!=-1)
                            {
                                serial_sender(ServerDetails.server_list.get(succ_id).server_listener_port, toBeSent);


                                //Once sent, add these repquets to sent_requests set
                                if(server_id==1)
                                {
                                    CommHelper.add_request_diff(toBeSent, Head.sent_requests , Head.sent_replies);
                                }
                                if(server_id==2)
                                {
                                    CommHelper.add_request_diff(toBeSent, MiddleServer.sent_requests , MiddleServer.sent_replies);
                                }
                                if(server_id==3)
                                {
                                    CommHelper.add_request_diff(toBeSent, Tail.sent_requests , Tail.sent_replies);
                                }
                            }
                        }
                        
                        //Send once in a while, the forward requests
                        Thread.sleep(1000);
                        
                    }//end of else for head or middle server
                    
                }//end of while
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
     
    }//end of run
    
    //Send objects in specified hashmap serially with delay of 100ms.
    public void serial_sender(int port, HashMap r1)
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
        
        Iterator it = r1.entrySet().iterator();
        try
        {
            while (it.hasNext())
            {
                ObjectOutputStream outToSuccessor = CommHelper.get_Socket_Output(port);
                
                Map.Entry pair = (Map.Entry)it.next();
                outToSuccessor.writeObject(pair.getValue());
                //lc.logger.info("Sent request from server "+server_id);
                if(port==Client.client_server_listener_port)
                {
                    Reply sent_to_client = (Reply)pair.getValue();
                    lc.logger.info("Reply sent to client.");
                    lc.logger.info("\n "+sent_to_client.request_id+" Status:"+sent_to_client.status+" Balance:"+sent_to_client.balance);
                }
                Thread.sleep(2000);
            }//end of while
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
    

}//end of class ServerCommunicationSend



//CLASS CommHelper**************************************************************************
class CommHelper
{
    //Connect to this port and get its ObjectInputStream
    static ObjectInputStream get_Socket_Input(int port)
    {
        ObjectInputStream in=null;
        try{
            Socket s=new Socket("localhost",port);
            in= new ObjectInputStream(s.getInputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return in;
    }
    //Connect to this port and get its ObjectOutputStream
    static ObjectOutputStream get_Socket_Output(int port)
    {
        ObjectOutputStream out=null;
        try{
            Socket s=new Socket("localhost",port);
            out= new ObjectOutputStream(s.getOutputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return out;
    }
    
    static ObjectInputStream get_ServerSocket_Input(ServerSocket ss)
    {
        ObjectInputStream in=null;
        try{
            Socket s=ss.accept();
            in= new ObjectInputStream(s.getInputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return in;
    }
    
    static ObjectOutputStream get_ServerSocket_Output(ServerSocket ss)
    {
        ObjectOutputStream out=null;
        try{
            Socket s=ss.accept();
            out= new ObjectOutputStream(s.getOutputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return out;
    }
    
    
    //COMPUTING SET DIFFERENCES
    
    //Returns r1-r2.
    static HashMap<String,Request> request_diff(HashMap<String,Request> r1, HashMap<String,Request> r2)
    {
        //Compare r1, r2 and return r1-r2 set.
        HashMap<String,Request> ans=new HashMap<>();
        
        Iterator it = r1.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String r1_key=(String)pair.getKey();
            
            
            if(!r2.containsKey(r1_key))
                ans.put(r1_key, r1.get(r1_key));
        }//end of while
        
        if(ans.size()==0)
            return null;
        
        return ans;
    }//end of request_diff
    
    //Return r1-r2
    static HashMap<String,Reply> reply_diff(HashMap<String, Reply> r1, HashMap<String, Reply> r2)
    {
        //Compare r1, r2 and return r1-r2 set.
        HashMap<String,Reply> ans=new HashMap<>();
        
        Iterator it = r1.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String r1_key=(String)pair.getKey();
            if(!r2.containsKey(r1_key))
                ans.put(r1_key, r1.get(r1_key));
        }//end of while
        
        if(ans.size()==0)
            return null;
        return ans;
    }
    
    //Add toAdd to Original set
    static void add_reply_diff(HashMap<String,Reply> toAdd, HashMap<String, Reply> orig)
    {
        Iterator it = toAdd.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String key=(String)pair.getKey();
            orig.put(key, toAdd.get(key));
        }//end of while
    }
    
    //Add toAdd to Original set 
    static void add_request_diff(HashMap<String,Request> toAdd, HashMap<String, Request> orig, HashMap <String, Reply> check)
    {
        Iterator it = toAdd.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String key=(String)pair.getKey();
            
            
            if(check.containsKey(key))
                orig.put(key, toAdd.get(key));
            
                
        }//end of while
    }
    
    //Return requests in r1 that do not have identifiers in r2
    static HashMap<String,Request> request_reply_diff(HashMap<String, Request> r1, HashMap<String, Reply> r2)
    {
        HashMap<String,Request> ans=null;
        
        Iterator it = r1.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            String r1_key=(String)pair.getKey();
            if(!r2.containsKey(r1_key))
                ans.put(r1_key, r1.get(r1_key));
        }//end of while
        
        if(ans.size()==0)
            return null;
        return ans;
    }
    
}//end of CommHelper class


//THREAD CLASS FOR LISTENING ON BACKPORTS FOR INCOMING REPLY SETS (Hashmaps)
class Back_Port_Listener implements Runnable
{
   private int server_id;
   Thread t=null;

    public Back_Port_Listener(int id) {
        this.server_id=id;
    }
    
    public void run()
    {
        try{
        //Listen  on back ports
        ServerSocket ss=new ServerSocket(ServerDetails.back_ports.get(server_id));
        
        while(true)
        {
            ObjectInputStream in = CommHelper.get_ServerSocket_Input(ss);
            //lc.logger.info("Received a back ack.");
            
            if(server_id==1)
            {
                HashMap temp_replies = (HashMap<String, Reply>)in.readObject();
                if(CommHelper.reply_diff(Head.sent_replies,temp_replies)!=null)
                    Head.sent_replies = temp_replies;
            }
            if(server_id==2)
            {
                HashMap temp_replies = (HashMap<String, Reply>)in.readObject();
                if(CommHelper.reply_diff(MiddleServer.sent_replies,temp_replies)!=null)
                    MiddleServer.sent_replies = temp_replies;
            }
//            if(server_id>2)
//            {
//                lc.logger.info("Not handling for servers other than head and MS now.");
//                break;
//            }
            in.close();
        }
    
        }
        catch(Exception e)
        {
//            e.printStackTrace();
//            lc.logger.info("Error inside run() of Back_Port_Listener.");
//            System.out.println(e.getMessage());
        }
    
    
    }//end of run()
    
    
    public void start()
    {
        t=new Thread(this);
        t.start();
    }
   
}//end of class Back_Port_Listener



//CLASS Back_Port_Sender for sending messages backwards.
class Back_Port_Sender implements Runnable
{
   private int server_id;
   Thread t=null;

    public Back_Port_Sender(int id) {
        this.server_id=id;
    }
    
    public void run()
    {
        try{
        
        while(true)
        {
            
            int my_pred_id = ServerDetails.predecessor_server_list.get(server_id);
            
            
            //If I am head, I dont send acknowledgement to anyone
            if(my_pred_id==-1)
            {
                
                break;
            }
            //How often to send?
            Thread.sleep(2000);
            
            //Connect to my predecessor backport
            ObjectOutputStream out = CommHelper.get_Socket_Output(ServerDetails.back_ports.get(my_pred_id));
            
            if(server_id==4)
            {
                out.writeObject(Extender.sent_replies);
                //lc.logger.info("Send back ack.");
            }
            if(server_id==3)
            {
                out.writeObject(Tail.sent_replies);
                //lc.logger.info("Send back ack.");
            }
            if(server_id==2)
            {
                out.writeObject(MiddleServer.sent_replies);
                //lc.logger.info("Send back ack.");
            }

            //lc.logger.info("Sent back acks to server:"+my_pred_id);
            
            out.close();
            
        }//end of while
        
        
    
        }//end of try
        catch(Exception e)
        {
//            e.printStackTrace();
//            lc.logger.info("Error inside run() of Back_Port_Sender.");
//            System.out.println(e.getMessage());
        }
    }//end of run
    
    
    public void start()
    {
        t=new Thread(this);
        t.start();
                
    }
}//end of class Back_Port_Sender





class InterCommunication
{
//    public static void main(String args[])
//    {
//        lc.logger.info("Hi");
//    }
}