package cz.chalupa.switchyardintegration;

import org.switchyard.annotations.Transformer;

import javax.inject.Named;

@Named("Transformation")
public final class Transformation {

	private static final String DELIM = ",";

	@Transformer(to = "{urn:chalupa:switchyard-integration-xml:1.0}messageCSV")
	public String transformMessageToMessageCSV(Message message) {
		StringBuilder sb = new StringBuilder();
		sb.append(message.getId()).append(DELIM);
		if (message.getProcessDate() != null) {
			sb.append(message.getProcessDate());
		}
		sb.append(DELIM);
		if (message.getBody() != null) {
			sb.append("\"").append(message.getBody().replace("\"", "\"\"")).append("\"");
		}
		return sb.toString();
	}
}
