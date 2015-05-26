import java.io.Serializable;
import java.util.HashMap;

public class TimeStampValue
{
    //timestamps hashmap with (server-id, time) values
    //isAlive hashmap with (threadname, true/false) values to close the parent thread MonitorServers
    static HashMap<Integer,Long> timestamps = new HashMap<Integer,Long>();
    static HashMap<Integer,Boolean> isAlive = new HashMap<Integer,Boolean>();
    
    static void init()
    {
        timestamps.put(1,System.currentTimeMillis());
        timestamps.put(2,System.currentTimeMillis());
        timestamps.put(3,System.currentTimeMillis());
        isAlive.put(1,true);
        isAlive.put(2,true);
        isAlive.put(3,true);
    }
    
    long get_timestamp(String threadname)
    {
        return (long)timestamps.get(threadname);
    }
    
    boolean get_isAlive(String threadname)
    {
        return (boolean)isAlive.get(threadname);
    }
}
