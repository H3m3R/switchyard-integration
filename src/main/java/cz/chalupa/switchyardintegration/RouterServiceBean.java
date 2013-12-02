package cz.chalupa.switchyardintegration;

import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

@Service(RouterService.class)
public class RouterServiceBean implements RouterService {

	@Inject @Reference("AMQOutboundReference")
	private RouterService amqOutBoundReference;
	
	@Inject @Reference("FileReference")
	private RouterService fileReference;

	@Override
	public void process(Message message) {
		// routing scenario
		amqOutBoundReference.process(message);
		fileReference.process(message);
	}
}