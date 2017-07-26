package com.mogaleaf.usbmuxd.protocol;

import com.dd.plist.NSDictionary;
import com.mogaleaf.usbmuxd.api.model.Device;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

		when(inputStream.read(any(byte[].class))).then(arg -> {
			byte[] argumentAt = arg.getArgumentAt(0, byte[].class);
			byte[] cpy = null;
			if(argumentAt.length == first.length){
				cpy = first;
			} else if(argumentAt.length == seconde.length){
				cpy = seconde;
			}
			if(cpy != null){
				int i = 0;
				for (byte b : cpy) {
					argumentAt[i++] = b;
				}
			}
			return 1;
		});
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


}
