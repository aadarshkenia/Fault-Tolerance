
// ONLY Queries processed here, updates processed elsewhere

import java.io.*;
import java.net.*;


public class QueryProcessor implements Runnable
{
   private int port; 
   private Thread t;
   private String threadName;
   
   QueryProcessor(String name, int port)
   {
       this.port=port;
       threadName = name;
       System.out.println("Creating " +  threadName + " for serving client QUERIES");
   }

   //Client requests will be accepted and processed after creating a server socket here
    public void run()
    {
       ObjectOutputStream outToClient=null;
       ObjectInputStream inFromClient=null;
      
            try
            {
                
                // Create the Server Socket (Server is the client here)
                ServerSocket s=new ServerSocket(port);
                Socket socket=s.accept();
                // Create input and output streams to client
                outToClient = new ObjectOutputStream(socket.getOutputStream());
                inFromClient = new ObjectInputStream(socket.getInputStream());
             
                while(true)
                {
                    Request r=(Request)inFromClient.readObject();
                    
                    Reply rep=null;
                    
                    //Account does not exist, create one and return balance
                    if(AccountHashMap.accountList.get(r.acc_id)==null)
                    {
                        AccountHashMap.accountList.put(r.acc_id, new Account(r.acc_id,0));
                        rep=new Reply(r.request_id,"PROCESSED",0);
                        outToClient.writeObject(rep);
                        continue;
                    }
                    
                    //Account exists, so get account
                    Account acc = AccountHashMap.accountList.get(r.acc_id);
                    
                    rep =  new Reply(r.request_id,"PROCESSED",acc.balance);
                    
                    outToClient.writeObject(rep);
                    
                }//end of while loop
            }//end of try
            catch(IOException e)
            {
                System.out.println("IOException inside run() of query processor.");
            }
            catch(ClassNotFoundException e)
            {
                System.out.println("ClassNotFoundException inside run() of query processor.");    
            }
                
     }//end of run method for thread
   
   public void start ()
   {
      System.out.println("Starting thread : " +  threadName );
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
   }//end of start()

}