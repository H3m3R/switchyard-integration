package cz.chalupa.switchyardintegration;

import org.apache.camel.builder.RouteBuilder;

public class ReverseRouterServiceRoute extends RouteBuilder {

	public void configure() {
		from("switchyard://ReverseRouterService")
				.log("Received message for 'ReverseRouterService' : ${body}")
				.to("switchyard://HQOutboundReference");
	}
}
