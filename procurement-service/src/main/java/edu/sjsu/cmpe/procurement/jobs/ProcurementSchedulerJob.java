package edu.sjsu.cmpe.procurement.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.ProcurementService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.procurement.ProcurementService;
import edu.sjsu.cmpe.procurement.domain.ShippedBookList;
//import edu.sjsu.cmpe.procurement.domain.BookOrder;
import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import edu.sjsu.cmpe.procurement.domain.Book;

import java.util.List;
import java.util.ArrayList;

/**
 * This job will run at every 5 minutes.
 */
@Every("5mn")
public class ProcurementSchedulerJob extends Job {
//    private final Logger log = LoggerFactory.getLogger(getClass());
public static String apolloPublisher;
   @Override
    public void doJob()  {
        startConsumer();
	getPublisherBook();
    }
        public static void getPublisherBook(){
        Client client = Client.create();
        WebResource webResource = client.resource("http://54.193.56.218:9000/orders/45284");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        //ShippedBook response = webResource.accept("application/json").type("application/json").get(ShippedBook.class);
        if (response.getStatus() != 200) {throw new RuntimeException("Failing - HTTP Error Code : " + response.getStatus());}
        String output = response.getEntity(String.class);
        System.out.println("Server Output: \n"+output);
        ObjectMapper map = new ObjectMapper();
        try{ ShippedBookList shippedBookList = map.readValue(output,ShippedBookList.class);
                for (int i = 0; i < shippedBookList.getShippedBookList().size(); i++) {
                        System.out.println(shippedBookList.getShippedBookList().get(i)+ "   ");
		postTopic(shippedBookList);
           }
	}
        catch (Exception e) { e.printStackTrace(); }
     }
     public void startConsumer()
     {
	try{ consumers(); }
	catch (Exception e) { e.printStackTrace(); }
     }
     public void consumers() throws JMSException
     {
	String user = "admin";
	String password ="password";
	String host = "54.193.56.218";
	int port =61613;
	String destination = "/queue/45284.book.orders";
	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
	factory.setBrokerURI("tcp://" + host + ":" + port);
	Connection connection = factory.createConnection(user, password);
	connection.start();
	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	Destination dest = new StompJmsDestination(destination);
	MessageConsumer consumer = session.createConsumer(dest);
	System.currentTimeMillis();
	System.out.println("Wait for messages from queue... ");
	long wait = 5000;
	List isbns = new ArrayList<Long>();
	while(true) {
	    Message msg = consumer.receive(wait);
	    if(msg instanceof  TextMessage) {
		String body = ((TextMessage) msg).getText();
		String splt[] = body.split(":");
		isbns.add(Long.parseLong(splt[1]));
		System.out.println("Received TextMessage = " + body);
	    }
           else if (msg instanceof StompJmsMessage) {
		StompJmsMessage smsg = ((StompJmsMessage) msg);
		String body = smsg.getFrame().contentAsString();
		System.out.println("Received StompJmsMessage = " + body);
	   }
	   else break;
	}
	connection.close();
	if (isbns!=null && isbns.size()>0) postPublisher(isbns);
     }
    public void postPublisher(List<Long> isbns)
    {
	try {
		Client client = Client.create();
		WebResource webResource = client.resource("http://54.193.56.218:9000/orders");
		String input = "{\"id\":\"45284\",\"order_book_isbns\":"+isbns+"}";
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
		}
		System.out.println("Output posted to Server");
		String output = response.getEntity(String.class);
		System.out.println(output);
	  } catch (Exception e) { e.printStackTrace(); }
    }

public static void postTopic(ShippedBookList shippedBookList) throws JMSException
{
	String destination;
	String data;
	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
//        apolloPublisher = "tcp://" + configuration.getJerseyClientConfiguration().getApolloHost() + ":" + configuration.getJerseyClientConfiguration().getApolloPort();
	String user = "admin";
	String password =  "password";
	String host = "54.193.56.218";
	String port = "61613";
	factory.setBrokerURI("tcp://" + host + ":" + port);
//	factory.setBrokerURI(apolloPublisher);
//	connection = factory.createConnection(ProcurementService.user,ProcurementService.password);
	Connection connection = factory.createConnection(user,password);
System.out.println("one");

	connection.start();
System.out.println("two");
	Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
System.out.println("three");

	for(int i=0;i<shippedBookList.getShippedBookList().size();i++){
		Book book = shippedBookList.getShippedBookList().get(i);
		data = book.getIsbn()+": \""+book.getTitle()+"\" : \""+book.getCategory()+"\" : \""+book.getCoverimage()+"\"";
	destination = "";
System.out.println("four");

        if(book.getCategory().toLowerCase().contains("comics")||
    	   book.getCategory().toLowerCase().contains("computer")||
	   book.getCategory().toLowerCase().contains("management")||
	   book.getCategory().toLowerCase().contains("selfimprovement"))
//	    	destination = configuration.getStompTopicPrefix() + book.getCategory();
                destination = "/topic/45284.book." + book.getCategory();
	try
	{
System.out.println("five");
		if (destination!="") {
System.out.println("six");
 
		Destination dest = new StompJmsDestination(destination);
		MessageProducer producer = session.createProducer(dest);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		TextMessage msg = session.createTextMessage(data);
		msg.setLongProperty("id", System.currentTimeMillis());
		producer.send(msg);
		}
	} catch (Exception e) { e.printStackTrace(); }
	
	connection.close();
}//*/

    }

}
