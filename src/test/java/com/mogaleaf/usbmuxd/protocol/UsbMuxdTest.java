package com.mogaleaf.usbmuxd.protocol;


import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.mogaleaf.usbmuxd.api.model.DeviceAttachMessage;
import com.mogaleaf.usbmuxd.api.model.Message;
import com.sun.org.apache.bcel.internal.util.ByteSequence;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsbMuxdTest {
	UsbMuxd instance = new UsbMuxd();

	@Test
	public void testDeviceConnectionMsg() throws ParserConfigurationException, ParseException, SAXException, PropertyListFormatException, IOException {
		byte[] bytes = instance.deviceConnectionMsg();
		ByteBuffer header = ByteBuffer.allocate(16);
		header.order(ByteOrder.LITTLE_ENDIAN);
		header.put(bytes, 0, 16);
		int l = header.getInt(0);
		ByteBuffer msg = ByteBuffer.allocate(l - 16);
		msg.put(bytes, 16, l - 16);
		NSObject parse = PropertyListParser.parse(msg.array());

		assertThat(parse).isInstanceOf(NSDictionary.class);
		NSDictionary nsDictionary = (NSDictionary) parse;
		assertThat(nsDictionary).hasSize(3);
		assertThat(nsDictionary).containsKey("MessageType");

		NSObject nsObject = nsDictionary.get("MessageType");
		assertThat(nsObject).isInstanceOf(NSString.class);
		NSString typeString = (NSString) nsObject;
		assertThat(typeString.toString()).isEqualTo("Listen");
	}

	@Test
	public void testReadIncomingMessage() throws PropertyListFormatException, ParserConfigurationException, SAXException, ParseException, IOException {
		Consumer<Message> consumerMock = Mockito.mock(Consumer.class);

		NSDictionary root = new NSDictionary();
		root.put("MessageType", "Attached");
		NSDictionary properties = new NSDictionary();
		properties.put("DeviceID", new NSString("1"));
		properties.put("SerialNumber", new NSString("serial"));
		properties.put("ConnectionType", new NSString("usb"));
		properties.put("LocationID", new NSString("loc"));
		properties.put("ProductID", new NSString("po"));
		root.put("Properties", properties);
		String s = root.toXMLPropertyList();
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		ByteBuffer msgBytes = ByteBuffer.allocate(16 + bytes.length);
		msgBytes.order(ByteOrder.LITTLE_ENDIAN);
		msgBytes.putInt(0, 16 + bytes.length);
		int count = 16;
		for (byte aByte : bytes) {
			msgBytes.put(count++, aByte);
		}
		Socket socket = mock(Socket.class);
		when(socket.isConnected()).thenReturn(true,false);
		instance.readIncomingMessage(socket,new ByteSequence(msgBytes.array()), consumerMock);
		ArgumentCaptor<DeviceAttachMessage> mgc = ArgumentCaptor.forClass(DeviceAttachMessage.class);
		verify(consumerMock).accept(mgc.capture());

		DeviceAttachMessage value = mgc.getValue();

		assertThat(value.connectionType).isEqualTo("usb");
		assertThat(value.deviceId).isEqualTo("1");
		assertThat(value.locationId).isEqualTo("loc");
		assertThat(value.productId).isEqualTo("po");
		assertThat(value.serialNumber).isEqualTo("serial");

	}
}
