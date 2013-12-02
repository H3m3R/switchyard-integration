package cz.chalupa.switchyardintegration;

import org.apache.camel.builder.RouteBuilder;

public class RouterServiceRoute extends RouteBuilder {

	public void configure() {
		from("switchyard://RouterService")
				.log("Received message for 'RouterService' : ${body}")
				.to("switchyard://AMQOutboundReference")
				.to("switchyard://FileReference");
	}
}
