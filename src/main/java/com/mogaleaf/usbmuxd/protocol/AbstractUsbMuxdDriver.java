package com.mogaleaf.usbmuxd.protocol;


import com.dd.plist.PropertyListFormatException;
import com.mogaleaf.usbmuxd.api.UsbMuxdDriver;
import com.mogaleaf.usbmuxd.api.model.Message;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class AbstractUsbMuxdDriver implements UsbMuxdDriver {

	private UsbMuxd usbMuxd = new UsbMuxd();
	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	public abstract SocketAddress getConnectionSocketAddress();

	@Override
	public void listenDeviceAttachEvent(Consumer<Message> callback) throws IOException {
		Socket connectionSocket = new Socket();
		connectionSocket.connect(getConnectionSocketAddress());
		byte[] connectByteMessage = usbMuxd.deviceConnectionMsg();
		InputStream inputStream = connectionSocket.getInputStream();
		executorService.execute(() -> handleDeviceConnectionMsg(connectionSocket,inputStream, callback));
		connectionSocket.getOutputStream().write(connectByteMessage);
	}

	@Override
	public void connectDevice(String deviceId) {

	}

	@Override
	public void disconnectDevice(String deviceId) {

	}

	private void handleDeviceConnectionMsg(Socket socket,InputStream input, Consumer<Message> callback) {
		try {
			usbMuxd.readIncomingMessage(socket,input,callback);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (PropertyListFormatException e) {
			e.printStackTrace();
		}
	}


}
