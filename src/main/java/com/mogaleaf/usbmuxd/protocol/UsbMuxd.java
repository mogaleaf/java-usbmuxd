package com.mogaleaf.usbmuxd.protocol;


import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.mogaleaf.usbmuxd.api.model.DeviceAttachMessage;
import com.mogaleaf.usbmuxd.api.model.DeviceDetachMessage;
import com.mogaleaf.usbmuxd.api.model.Message;
import com.mogaleaf.usbmuxd.api.model.MessageType;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.function.Consumer;

public class UsbMuxd {

	public byte[] deviceConnectionMsg() {
		byte[] bytes = buildListenConnectionMsg();
		ByteBuffer buffer = buildByteMsg(bytes);
		return buffer.array();
	}

	private ByteBuffer buildByteMsg(byte[] bytes) {
		int len = (16 + bytes.length);
		int version = 1;
		int request = 8;
		int tag = 1;
		ByteBuffer buffer = ByteBuffer.allocate(len);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(0, len);
		buffer.putInt(4, version);
		buffer.putInt(8, request);
		buffer.putInt(12, tag);
		int i = 16;
		for (byte aByte : bytes) {
			buffer.put(i++,aByte);
		}
		return buffer;
	}

	private byte[] buildListenConnectionMsg() {
		NSDictionary root = new NSDictionary();
		root.put("MessageType", "Listen");
		root.put("ClientVersionString", "1.0");
		root.put("ProgName", "mogaleaf-usbmux-driver");
		String s = root.toXMLPropertyList();
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		return bytes;
	}

	public void readIncomingMessage(Socket socket,InputStream input, Consumer<Message> callback) throws IOException, ParserConfigurationException, ParseException, SAXException, PropertyListFormatException {
		while (socket.isConnected()) {
			int size = getSize(input);
			if (size > 0) {
				NSDictionary dico = getNsDictionary(input, size);
				NSString messageType = (NSString) dico.get("MessageType");
				MessageType messageTypeEnum = MessageType.valueOf(messageType.getContent());
				switch (messageTypeEnum) {
					case Attached:
						DeviceAttachMessage deviceAttachMessage = buildConnectionMsg(dico);
						callback.accept(deviceAttachMessage);
						break;
					case Detached:
						DeviceDetachMessage deviceDetachMessage = new DeviceDetachMessage();
						deviceDetachMessage.deviceId = dico.get("DeviceID").toString();
						callback.accept(deviceDetachMessage);
						break;
				}
			}
		}
	}

	private DeviceAttachMessage buildConnectionMsg(NSDictionary dico) {
		DeviceAttachMessage deviceAttachMessage = new DeviceAttachMessage();
		NSDictionary properties = (NSDictionary) dico.get("Properties");
		if (properties != null) {
			deviceAttachMessage.serialNumber = properties.get("SerialNumber").toString();
			deviceAttachMessage.connectionType = properties.get("ConnectionType").toString();
			deviceAttachMessage.deviceId = properties.get("DeviceID").toString();
			deviceAttachMessage.locationId = properties.get("LocationID").toString();
			deviceAttachMessage.productId = properties.get("ProductID").toString();
		}
		return deviceAttachMessage;
	}

	private NSDictionary getNsDictionary(InputStream input, int size) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {
		byte[] body = new byte[size];
		input.read(body);
		NSObject parse = PropertyListParser.parse(body);
		return (NSDictionary) parse;
	}

	private int getSize(InputStream input) throws IOException {
		byte[] header = new byte[16];
		input.read(header);
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(header);
		return buffer.getInt(0) - 16;
	}
}
