package cz.chalupa.switchyardintegration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;
import java.util.zip.ZipFile;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;

import org.codehaus.jackson.JsonNode;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import static org.testng.Assert.*;

@Test(groups = "integration")
public class RoutingTest extends Arquillian {

	private static final Logger logger = LoggerFactory.getLogger(RoutingTest.class);

	private static final String JAR_FILE = "target/switchyard-integration.jar";
	private static final File ACTIVEMQ_DATA_DIR = new File(System.getProperty("java.io.tmpdir"), "activemq-data");
	private static final long MEMORY_STORE_USAGE_LIMIT = 209715200;
	private static final long TEMP_STORE_USAGE_LIMIT = 209715200;

	private static final String AMQ_INFLOW_QUEUE = "AMQInflowQueue";
	private static final String AMQ_OUTBOUND_QUEUE = "AMQOutboundQueue";

	private static final String CSV_FILE = "test.csv";

	private static String XML_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<message>\n"
			+ "    <id>11</id>\n"
			+ "    <processDate>2012-01-10T12:12:02</processDate>"
			+ "    <body>Message Body</body>\n"
			+ "</message>\n";
	private static Document XML = TestUtils.createXMLDocument(XML_PAYLOAD);

	private static final String JSON_PAYLOAD = "{\"id\":11,\"processDate\":1326193922000,\"body\":\"Message Body\"}";
	private static final JsonNode JSON = TestUtils.createJsonNode(JSON_PAYLOAD);

	@Deployment
	public static Archive<?> createTestArchive() throws Exception {
		File artifact = new File(JAR_FILE);
		try {
			return ShrinkWrap.create(ZipImporter.class, artifact.getName())
					.importFrom(new ZipFile(artifact))
					.as(JavaArchive.class)
					.addClass(TestUtils.class)
					.addAsResource("developer.properties");
		} catch (Exception e) {
			throw new RuntimeException(JAR_FILE + " not found. Run 'mvn package'", e);
		}
	}

	@Resource(mappedName = "/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = "HQInflowQueue")
	private Destination inflowQueue;

	@Resource(mappedName = "HQOutboundQueue")
	private Destination outboundQueue;

	private BrokerService broker;

	@BeforeClass()
	public void startActiveMQBroker() throws Exception {
		broker = new BrokerService();
		broker.setBrokerName("default");
		broker.setUseJmx(false);
		broker.setPersistent(false);
		broker.setDataDirectoryFile(ACTIVEMQ_DATA_DIR);
		try {
			broker.addConnector(ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);

			SystemUsage systemUsage = broker.getSystemUsage();
			systemUsage.getMemoryUsage().setLimit(MEMORY_STORE_USAGE_LIMIT);
			systemUsage.getTempUsage().setLimit(TEMP_STORE_USAGE_LIMIT);

			broker.start();
		} catch (Exception e) {
		}
	}

	@Test
	public void testHornetQToActiveMQRoute() throws Exception {
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;

		try {
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(inflowQueue);

			final TextMessage message = session.createTextMessage();
			logger.info("Sending XML to hornetQ inflow queue");
			message.setText(XML_PAYLOAD);
			producer.send(message);
		} finally {
			TestUtils.close(producer, null, session, connection);
		}

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
		try {
			connection = cf.createConnection(ActiveMQConnectionFactory.DEFAULT_USER, ActiveMQConnectionFactory.DEFAULT_PASSWORD);
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(session.createQueue(AMQ_OUTBOUND_QUEUE));

			logger.info("Verifying JSON message delivery from ActiveMQ outbound queue");
			final Message receivedMessage = consumer.receive(1000);
			assertNotNull(receivedMessage, "No message received from the queue: " + AMQ_OUTBOUND_QUEUE);
			assertNull(consumer.receive(1000), "Unexpected message received from queue: " + AMQ_OUTBOUND_QUEUE);

			if (receivedMessage instanceof TextMessage) {
				assertEquals(TestUtils.createJsonNode(((TextMessage) receivedMessage).getText()), JSON);
			} else {
				fail("Received message of wrong type");
			}
		} finally {
			TestUtils.close(null, consumer, session, connection);
		}

		logger.info("Verifying CSV file creation");
		File csv = new File(getProperty("file.out.directory") + CSV_FILE);
		System.out.println(csv.getAbsolutePath());
		assertTrue(csv.exists());
		BufferedReader br = new BufferedReader(new FileReader(csv));
		try {
			assertEquals(br.readLine(), "11,Tue Jan 10 12:12:02 CET 2012,\"Message Body\"");
		} finally {
			br.close();
		}
		csv.delete();
	}

	@Test
	public void testActiveMQToHornetQ() throws Exception {

		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
		try {
			connection = cf.createConnection(ActiveMQConnectionFactory.DEFAULT_USER, ActiveMQConnectionFactory.DEFAULT_PASSWORD);
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(session.createQueue(AMQ_INFLOW_QUEUE));

			final TextMessage message = session.createTextMessage();
			logger.info("Sending JSON to ActiveMQ inflow queue");
			message.setText(JSON_PAYLOAD);
			producer.send(message);
		} finally {
			TestUtils.close(producer, null, session, connection);
		}

		try {
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(outboundQueue);

			logger.info("Verifying XML message delivery from HornetQ outbound queue");
			final Message receivedMessage = consumer.receive(1000);
			assertNotNull(receivedMessage, "No message received from the queue: " + outboundQueue);
			assertNull(consumer.receive(1000), "Unexpected message received from queue: " + outboundQueue);

			if (receivedMessage instanceof TextMessage) {
				assertEquals(TestUtils.createXMLDocument(((TextMessage) receivedMessage).getText()), XML);
			} else {
				fail("Received message of wrong type");
			}
		} finally {
			TestUtils.close(null, consumer, session, connection);
		}
	}

	/**
	 * Reads property value from developer.properties.
	 *
	 * @param property to read
	 * @return property value
	 */
	public String getProperty(String property) {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream("/developer.properties"));
			return properties.getProperty(property);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
