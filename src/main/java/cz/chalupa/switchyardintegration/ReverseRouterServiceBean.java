package cz.chalupa.switchyardintegration;

import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

@Service(ReverseRouterService.class)
public class ReverseRouterServiceBean implements ReverseRouterService {

	@Inject @Reference("HQOutboundReference")
	private RouterService hqOutBoundReference;
	
	@Override
	public void process(Message message) {
		// routing scenario
		hqOutBoundReference.process(message);
	}
}
