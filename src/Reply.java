

import java.io.Serializable;

public class Reply implements Serializable{
    public String request_id;//Corresponding id for the request to which you send this reply
    public String status;
    public int balance;
    
    //Method definitions
    public Reply(String a, String b, int c)
    {
        this.request_id=a;
        this.status=b;
        this.balance=c;
    }
	
    public Reply()
    {
        
    }
}
