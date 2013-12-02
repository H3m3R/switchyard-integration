package cz.chalupa.switchyardintegration;

import java.io.FileInputStream;
import java.util.Properties;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Test helper.
 */
public class TestUtils {

	/**
	 * Creates {@link org.w3c.dom.Document} from the specified string.
	 *
	 * @param xml to parse
	 * @return new xml document
	 * @throws Exception
	 */
	public static Document createXMLDocument(String xml) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xml);
			doc.normalizeDocument();
			return doc;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Creates {@link org.codehaus.jackson.JsonNode} from the specified string.
	 *
	 * @param json to parse
	 * @return new json node
	 * @throws Exception
	 */
	public static JsonNode createJsonNode(String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readTree(json);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Clean up JMS objects.
	 *
	 * @param producer the producer
	 * @param consumer the consumer
	 * @param session the session
	 * @param conn the connection
	 */
	public static void close(MessageProducer producer, MessageConsumer consumer, Session session, Connection conn) {
		if (producer != null) {
			try {
				producer.close();
			} catch (Exception e) {
			}
		}
		if (consumer != null) {
			try {
				consumer.close();
			} catch (Exception e) {
			}
		}
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
}
