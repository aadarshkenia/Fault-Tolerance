import java.io.Serializable;
import java.util.StringTokenizer;

public class Request implements Serializable
{
    public String request_type;//Get balance or deposit or withdraw  
    public String request_id;//
    public String acc_id;
    public int amount; //Will be -1 for getting balance, else it will be the one entered by client
    
    public Request(String req, String type, int amt)
    {
        this.request_id=req;
        this.request_type = type;
        this.amount=amt;
	set_acc_id();	
    }
    
    void set_acc_id()
    {
        StringTokenizer st=new StringTokenizer(request_id,".");
        String s1=st.nextToken();
        String s2=st.nextToken();
        this.acc_id = s1+"."+s2;
    }
}
