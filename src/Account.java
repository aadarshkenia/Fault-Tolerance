import java.io.Serializable;
public class Account implements Serializable
{
	public String acc_id;
        public int balance;
        
        Account(String id, int bal)
        {
            this.acc_id=id;
            this.balance=bal;
        }
        Account()
        {
            
        }
}