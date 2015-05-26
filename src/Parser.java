import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.Serializable;
import java.util.*;


class Parser {
    
    
	static void init(String s)throws Exception
        {
		//Get the DOM Builder Factory
		DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();

		//Get the DOM Builder
		DocumentBuilder builder = factory.newDocumentBuilder();

		//Load and Parse the XML document
		//document contains the complete XML as a Tree.
		Document document = builder.parse(ClassLoader.getSystemResourceAsStream(s));

		//Iterating through the nodes and extracting the data.
		NodeList nodeList = document.getElementsByTagName("server");
		NodeList clientList = document.getElementsByTagName("client");

		for (int i = 0; i < nodeList.getLength(); i++) {

			//We have encountered an <employee> tag.
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				int numerofServers = Integer.parseInt(node.getAttributes().
						getNamedItem("numberofservers").getNodeValue());
				for(int j =1 ; j <= numerofServers ;j++)
				{
					if( j == numerofServers)

						ServerDetails.successor_server_list.put(j, -1);

					else

						ServerDetails.successor_server_list.put(j, j+1);

				}
				for(int j =1 ; j <= numerofServers ;j++)
				{
					if( j == 1)

						ServerDetails.predecessor_server_list.put(j, -1);
					else				
						ServerDetails.predecessor_server_list.put(j, j-1);

				}


				ServerDetails.printHashMap( ServerDetails.predecessor_server_list);

				NodeList childNodes = node.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node cNode = childNodes.item(j);

					//Identifying the child tag of employee encountered. 
					if (cNode instanceof Element) 
					{
						int id = Integer.parseInt(cNode.getAttributes().
								getNamedItem("id").getNodeValue());
						String name = cNode.getAttributes().
								getNamedItem("name").getNodeValue();
						String thread_name = cNode.getAttributes().
								getNamedItem("threadname").getNodeValue();
						int m_port =Integer.parseInt( cNode.getAttributes().getNamedItem("master_port").getNodeValue());
						int n_port = Integer.parseInt( cNode.getAttributes().getNamedItem("notification_port").getNodeValue());
						int serv_port = Integer.parseInt( cNode.getAttributes().getNamedItem("server_listner_port").getNodeValue());;
						int b_port = Integer.parseInt( cNode.getAttributes().getNamedItem("back_link_port").getNodeValue()); ;
						int c_port = Integer.parseInt( cNode.getAttributes().getNamedItem("client_port").getNodeValue());
						int ext_port = Integer.parseInt( cNode.getAttributes().getNamedItem("extension_port").getNodeValue());;
                                                int count = Integer.parseInt( cNode.getAttributes().getNamedItem("count").getNodeValue());;
						Server a = new Server(id,name ,thread_name, m_port, n_port, serv_port,  b_port,  c_port,  ext_port ,count);
						ServerDetails.server_list.put(id,a);
						ServerDetails.back_ports.put(id, b_port);
						ServerDetails.extension_ports.put(id, ext_port);
						ServerDetails.client_port_list.put(id,c_port);	
                                                System.out.println(ServerDetails.client_port_list.size());
					}
				}
			}

		}
		for (int i = 0; i < clientList.getLength(); i++) {

			//We have encountered an <employee> tag.
			Node node = clientList.item(i);
			if (node instanceof Element) {

				NodeList childNodes = node.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node cNode = childNodes.item(j);

					//Identifying the child tag of employee encountered. 
					if (cNode instanceof Element) {
						String account_no =cNode.getAttributes().getNamedItem("account_no").getNodeValue();
						int balance =Integer.parseInt( cNode.getAttributes().getNamedItem("balance").getNodeValue());
						Account a=new Account(account_no,balance);

						AccountHashMap.accountList.put(account_no, a);

					}
				}
			}

		}




	}
}


class Server 
{
	int server_id;
	String server_name;
	String server_thread_name;
	int master_port;
	int notification_port;
	int server_listener_port;
	int back_link_port;
	int client_port;
	int extension_port; //Port on which I receive updates from tail during extension
        int count;
	public Server(int id,String name ,String thread_name,int m_port, int n_port, int serv_port, int b_port, int c_port, int ext_port ,int count)
	{
		this.server_id=id;
		this.server_name=name;
		this.server_thread_name=thread_name;
		this.master_port=m_port;
		this.notification_port=n_port;//Receive failure notifications from master
		this.server_listener_port=serv_port; //Port on which I listen to my predecessor for updates
		this.back_link_port=b_port;
		this.client_port=c_port;
		this.extension_port=ext_port;
                this.count         = count;
		System.out.print(id + "  " +name + "  " + thread_name + "  " + m_port + "  " +n_port + "  " + serv_port + "  " + b_port + "  " + c_port  + "  " +ext_port );
	}
}

//ALL DETAILS OF ALL SERVERS
class ServerDetails
{
	//Used by thread to get server details from key=server_id && value=Server object;
	static HashMap<Integer,Server> server_list = new HashMap<Integer,Server>();

	//Key - server_id && Value=Port number of successor
	static HashMap<Integer,Integer> successor_server_list = new HashMap<Integer,Integer>();

	//Key - server_id && Value=Port number of predecessor
	static HashMap<Integer,Integer> predecessor_server_list = new HashMap<Integer,Integer>();

	//Key - server_id and Value=Port number on which I listen to clients
	static HashMap<Integer, Integer> client_port_list = new HashMap<Integer, Integer>();

	static HashMap<Integer, Integer> back_ports = new HashMap<Integer, Integer>();

	static HashMap<Integer, Integer> extension_ports = new HashMap<Integer, Integer>();

	static int current_head_server_id=1;
	static int current_tail_server_id=3;

	//Delay in case of config for extending the chain
	static int extender_delay=5000; //Milliseconds

	//Delay while sending updates from tail to new adding server
	static int update_delay_for_extension=10000;//Milliseconds

	static void init()
	{

		/*    Server head=new Server(1,"Head","Head pinger", 6788,6789,6100,7000,6500,7100);
        Server middle = new Server(2,"Middle","Middle pinger",6790,6791,6102,7002,6502,7102);
        Server tail=new Server(3,"Tail","Tail pinger",6792,6793,6104,7004,6504,7104);
        Server extender = new Server(4,"Extender","Extender ping",6794,6795,6106,7006,6506,7106);

        //Inserting servers into respective hashmaps
        server_list.put(1, head);
        server_list.put(2, middle);
        server_list.put(3, tail);
        server_list.put(4, extender);

        //Initializing successor port list
        successor_server_list.put(1,2);
        successor_server_list.put(2,3);
        successor_server_list.put(3,-1);


        //Initializing predecessor port list
        predecessor_server_list.put(1, -1);
        predecessor_server_list.put(2, 1);
        predecessor_server_list.put(3, 2);

        //Initializeing client ports list
        client_port_list.put(1, 6500);
        client_port_list.put(2, 6502);
        client_port_list.put(3, 6504);
        client_port_list.put(4, 6506);

        back_ports.put(1, 7000);
        back_ports.put(2, 7002);
        back_ports.put(3, 7004);
        back_ports.put(4, 7006); 

        extension_ports.put(1, 7100);
        extension_ports.put(2, 7102);
        extension_ports.put(3, 7104);
        extension_ports.put(4, 7106);
		 */
	}


	public static void printHashMap(HashMap<Integer,Integer> m)
	{
		Iterator it = m.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry pair = (Map.Entry)it.next();
			System.out.println(" \n Here " + pair.getKey()+" "+pair.getValue());
		}//end of while
	}//end of printHashMap
}//end of class ServerDetails
