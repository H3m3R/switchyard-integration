package cz.chalupa.switchyardintegration;

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit")
public class TransformationTest {

	@DataProvider
	public static Object[][] transformationDataProvider() {
		Date now = new Date();
		return new Object[][] {
				{ new Message(1, now, "test"), "1," + now + ",\"test\"" },
				{ new Message(1, now, "test, test"), "1," + now + ",\"test, test\"" },
				{ new Message(1, now, "test, \"test\""), "1," + now + ",\"test, \"\"test\"\"\"" },
				{ new Message(1, null, null), "1,," }
		};
	}

	@Test(dataProvider = "transformationDataProvider")
	public void testCsvTransformation(Message message, String result) {
		Transformation t = new Transformation();
		assertEquals(t.transformMessageToMessageCSV(message), result);
	}
}
