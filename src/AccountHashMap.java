/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Aadarsh PC
 */
import java.util.HashMap;
public class AccountHashMap
{
    //Key=Account id and value=account object
    static HashMap<String,Account> accountList = new HashMap<String, Account>();
    
    //Initializer
    static void init()
    {
        Account a=new Account("A.1",1000);
        accountList.put("A.1", a);
    }

    
}
