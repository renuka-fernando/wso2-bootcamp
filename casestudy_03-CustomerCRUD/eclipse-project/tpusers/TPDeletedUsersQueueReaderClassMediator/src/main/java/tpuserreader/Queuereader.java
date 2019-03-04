package tpuserreader;

import org.apache.synapse.MessageContext; 
import org.apache.synapse.mediators.AbstractMediator;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.synapse.MessageContext;

import java.util.Properties;

public class Queuereader extends AbstractMediator { 

	public boolean mediate(MessageContext context) throws NamingException, JMSException, InterruptedException {
		SampleMessageListener.context = context;
		MessageListenerClient messageListenerClient = new MessageListenerClient();
        messageListenerClient.registerSubscribers();
        
        
        
		return true;
	}
}

private class MessageListenerClient {
    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "QueueConnectionFactory";
    String userName = "admin";
    String password = "admin";
    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String CARBON_DEFAULT_HOSTNAME = "localhost";
    private static String CARBON_DEFAULT_PORT = "5674";
    String queueName = "DeletedTpUsers";
    public void registerSubscribers() throws NamingException,InterruptedException, JMSException {
        try {
            InitialContext ctx = initQueue();
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
            QueueConnection queueConnection = connFactory.createQueueConnection();
            queueConnection.start();
            QueueSession queueSession =
                    queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) ctx.lookup(queueName);
            MessageConsumer queueReceiver = queueSession.createConsumer(queue);
            queueReceiver.setMessageListener(new SampleMessageListener(queueReceiver, queueSession, queueConnection));
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    private InitialContext initQueue() throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));
        properties.put("queue." + queueName, queueName);
        properties.put("topic." + topicName, topicName);
        InitialContext ctx = new InitialContext(properties);
        return ctx;
    }
    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'")
                .toString();
    }
 
}

private class SampleMessageListener implements javax.jms.MessageListener {
	private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private MessageConsumer queueReceiver;
    private int count = 0;
    public static MessageContext context;
    
    public SampleMessageListener(MessageConsumer queueReceiver, QueueSession queueSession, QueueConnection queueConnection) {
        this.queueReceiver = queueReceiver;
        this.queueSession = queueSession;
        this.queueConnection = queueConnection;
        System.out.println("Starting Queue Listener....");
    }
    /**
     * Override this method and add the operation which is needed to be done when a message is arrived
     *
     * @param message - the next received message
     */
    @Override
    public void onMessage(Message message) {
        count++;
        TextMessage receivedMessage = (TextMessage) message;
        try {
            System.out.println("Got the message ==> " + receivedMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    public void closeAll(TextMessage receivedMessage) {
        try {
            if (receivedMessage.getText().contains("Queue")) {
                System.out.println("Closing Queue Listener...........");
                queueConnection.close();
                queueSession.close();
                queueReceiver.close();
            } else {
                System.out.println("Closing Topic Listener...........");
                topicConnection.close();
                topicSession.close();
                topicSubscriber.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
