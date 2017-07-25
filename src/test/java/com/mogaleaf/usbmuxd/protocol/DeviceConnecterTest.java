package com.mogaleaf.usbmuxd.protocol;

import com.dd.plist.NSDictionary;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceConnecterTest {

	DeviceConnecter instance;

	@Mock
	InputStream inputStream;

	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		instance = new DeviceConnecter();
	}

	@Test
	public void testgetConnectionResult() throws IOException {
		NSDictionary root = new NSDictionary();
		root.put("MessageType", "Result");
		root.put("Number", 0);
		String s = root.toXMLPropertyList();
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		ByteBuffer msg = PlistMessageService.buildByteMsg(bytes);
		byte[] first = new byte[16];
		byte[] seconde = new byte[bytes.length];
		msg.get(first);
		msg.get(seconde);
		when(inputStream.read(Matchers.argThat(new InputStreamMockHelper(first,seconde)))).thenReturn(1);
		ConnectedMessage connectionResult = instance.getConnectionResult(inputStream);
		assertThat(connectionResult.result).isEqualTo(0);

	}

}
