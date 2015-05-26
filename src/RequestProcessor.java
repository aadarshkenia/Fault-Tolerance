//ONLY FOR PROCESSING CLIENT UPDATE OPERATIONS

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class RequestProcessor
{
  
   //PROCESSING ONLY UPDATE REQUESTS HERE AS OF NOW
   public static Reply processRequest(Request req)
   {
       Reply rep=null;
                    
            //Account does not exist, create one and return balance
            if(AccountHashMap.accountList.get(req.acc_id)==null)
            {
                AccountHashMap.accountList.put(req.acc_id, new Account(req.acc_id,0));
                rep=new Reply(req.request_id,"PROCESSED",0);
                return rep;
              
            }

            //Account exists, so get account
            Account acc = AccountHashMap.accountList.get(req.acc_id);

            int choice=0;

            if(req.request_type.equalsIgnoreCase("Deposit"))
            {
                choice=1;
            }

            if(req.request_type.equalsIgnoreCase("Withdraw"))
            {
                choice=2;
            }
            //*********************        

            switch(choice)
            {
                case 1://DEPOSIT
                {

                    if(req.amount>=0)//Amount to be deposited is not negative
                    {
                        acc.balance = acc.balance + req.amount;
                        rep=new Reply(req.request_id,"PROCESSED",acc.balance);
                        break;
                    }
                    else
                    {
                        rep=new Reply(req.request_id,"UNPROCESSED",acc.balance);
                        break;
                    }

                }
                case 2://WITHDRAW
                {

                    if(req.amount<=acc.balance)//Amount withdrawn more than balance
                    {
                        acc.balance=acc.balance-req.amount;
                        rep = new Reply(req.request_id,"PROCESSED",acc.balance);
                    }
                    else
                    {
                        rep=new Reply(req.request_id,"UNPROCESSED",acc.balance);
                    }
                    break;
                }
            }//end of switch case
            return rep;    
   }//end of ProcessRequest()
   
}//end of class RequestProcessor
