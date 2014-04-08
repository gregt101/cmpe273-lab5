package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;
public class BookRepository implements BookRepositoryInterface {
    /** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
    private final ConcurrentHashMap<Long, Book> bookInMemoryMap;

    /** Never access this key directly; instead use generateISBNKey() */
    private long isbnKey;
    private String ApolloUser;
    private String ApolloPassword;
    private String ApolloHost;
    private String ApolloPort;
    private String StompQueueName;
    private String LibraryName;

    public BookRepository() {
	bookInMemoryMap = seedData();
	isbnKey = 0;
    }

    private ConcurrentHashMap<Long, Book> seedData(){
	ConcurrentHashMap<Long, Book> bookMap = new ConcurrentHashMap<Long, Book>();
	Book book = new Book();
	book.setIsbn(1);
	book.setCategory("computer");
	book.setTitle("Java Concurrency in Practice");
	try {
	    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
	} catch (MalformedURLException e) {
	    // eat the exception
	}
	bookMap.put(book.getIsbn(), book);

	book = new Book();
	book.setIsbn(2);
	book.setCategory("computer");
	book.setTitle("Restful Web Services");
	try {
	    book.setCoverimage(new URL("http://goo.gl/ZGmzoJ"));
	} catch (MalformedURLException e) {
	    // eat the exception
	}
	bookMap.put(book.getIsbn(), book);

	return bookMap;
    }

    /**
     * This should be called if and only if you are adding new books to the
     * repository.
     * 
     * @return a new incremental ISBN number
     */
    private final Long generateISBNKey() {
	// increment existing isbnKey and return the new value
	return Long.valueOf(++isbnKey);
    }

    /**
     * This will auto-generate unique ISBN for new books.
     */
    @Override
    public Book saveBook(Book newBook) {
	checkNotNull(newBook, "newBook instance must not be null");
	// Generate new ISBN
	Long isbn = generateISBNKey();
	newBook.setIsbn(isbn);
	// TODO: create and associate other fields such as author
	Status status = Status.available;
	// Finally, save the new book into the map
        String title =  newBook.getTitle();
        Book book = bookInMemoryMap.get(title);
        if  (book != null){
                book.setStatus(status);
                return book;
        }
        else{
		bookInMemoryMap.putIfAbsent(isbn, newBook);
		return newBook;
	}
    }

    /**
     * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
     */
    @Override
    public Book getBookByISBN(Long isbn) {
	checkArgument(isbn > 0,
		"ISBN was %s but expected greater than zero value", isbn);
	return bookInMemoryMap.get(isbn);
    }

    @Override
    public List<Book> getAllBooks() {
	return new ArrayList<Book>(bookInMemoryMap.values());
    }

    /*
     * Delete a book from the map by the isbn. If the given ISBN was invalid, do
     * nothing.
     * 
     * @see
     * edu.sjsu.cmpe.library.repository.BookRepositoryInterface#delete(java.
     * lang.Long)
     */
    @Override
    public void delete(Long isbn) {
	bookInMemoryMap.remove(isbn);
    }

    @Override
    public void producer(Long isbn) throws JMSException{
//		LibraryServiceConfiguration configuration = new LibraryServiceConfiguration();
System.out.println("one");
    	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
System.out.println("two");
    	factory.setBrokerURI("tcp://" + ApolloHost + ":" + ApolloPort);
//factory.setBrokerURI("tcp://54.193.56.218:61613");

System.out.println("three");
    	Connection connection = factory.createConnection(ApolloUser, ApolloPassword);
//Connection connection = factory.createConnection("admin", "password");

System.out.println("four");
    	connection.start();

System.out.println("five");
    	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

System.out.println("six");
    	Destination destination = new StompJmsDestination(StompQueueName);

System.out.println("seven");
    	MessageProducer producer = session.createProducer(destination);

System.out.println("eight");
    	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

System.out.println("nine");
    	System.out.println("Sending messages to " + StompQueueName + "...");

System.out.println("ten");
    	String data = LibraryName + ":" + isbn;

System.out.println("eleven");
    	TextMessage msg = session.createTextMessage(data);

System.out.println("twelve");
    	msg.setLongProperty("id", System.currentTimeMillis());

System.out.println("thirteen");
    	producer.send(msg);

System.out.println("forteen");
    	connection.close();
    }
    @Override
    public void getConfiguration(LibraryServiceConfiguration configuration){
	ApolloUser = configuration.getApolloUser();
	ApolloPassword = configuration.getApolloPassword();
	ApolloHost = configuration.getApolloHost();
	ApolloPort = configuration.getApolloPort();
	StompQueueName = configuration.getStompQueueName();
	LibraryName = configuration.getLibraryName();
  }
}
