package com.mogaleaf.usbmuxd.protocol;

import com.dd.plist.NSDictionary;
import com.mogaleaf.usbmuxd.api.model.Device;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceListenerTest {

	DeviceListener instance;

	@Mock
	InputStream inputStream;


	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		instance = new DeviceListener();
	}


	@Test
	public void testAttached() throws IOException, InterruptedException {

		NSDictionary root = new NSDictionary();
		root.put("MessageType", "Attached");
		NSDictionary props = new NSDictionary();
		root.put("Properties", props);
		props.put("SerialNumber", "123");
		props.put("ConnectionType", "usb");
		props.put("DeviceID", "1");
		props.put("LocationID", "lo");
		props.put("ProductID", "po");
		String s = root.toXMLPropertyList();
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		ByteBuffer msg = PlistMessageService.buildByteMsg(bytes);
		byte[] first = new byte[16];
		byte[] seconde = new byte[bytes.length];
		msg.get(first);
		msg.get(seconde);
		when(inputStream.read(Matchers.argThat(new InputStreamByteMock(first,seconde)))).thenReturn(1);
		instance.start(inputStream);
		instance.register(d -> {
			Device device = d.device;
			assertThat(device.deviceId).isEqualTo(1);
			assertThat(device.connectionType).isEqualTo("usb");
			assertThat(device.serialNumber).isEqualTo("123");
			assertThat(device.locationId).isEqualTo("lo");
			assertThat(device.productId).isEqualTo("po");
			instance.stop();
		});
		instance.run();
	}

	class InputStreamByteMock extends BaseMatcher<byte[]> {

		Map<Integer, byte[]> bySize = new HashMap<>();

		InputStreamByteMock(byte[]... bytesToMatch) {
			for (byte[] toMatch : bytesToMatch) {
				bySize.put(toMatch.length, toMatch);
			}
		}

		@Override
		public boolean matches(Object o) {
			if (o instanceof byte[]) {
				byte[] copy = (byte[]) o;
				byte[] bytes = bySize.get(copy.length);
				int i = 0;
				for (byte b : bytes) {
					copy[i++] = b;
				}
				return true;
			}
			return false;
		}

		@Override
		public void describeMismatch(Object o, Description description) {

		}

		@Override
		public void describeTo(Description description) {

		}
	}
}
