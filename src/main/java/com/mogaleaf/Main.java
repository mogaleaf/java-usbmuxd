package com.mogaleaf;

import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.mogaleaf.usbmuxd.api.IUsbMuxd;
import com.mogaleaf.usbmuxd.api.UsbMuxdFactory;
import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;
import com.mogaleaf.usbmuxd.protocol.PlistMessageService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Main {
	static IUsbMuxd usbMuxdDriver = UsbMuxdFactory.getInstance();

	public static void main(String args[]) throws IOException, InterruptedException {
		try {

			usbMuxdDriver.registerDeviceConnectionListener(m -> {
				switch (m.type) {
					case Add:
						System.out.println("add " + m.device.deviceId);
						break;
					case Remove:
						System.out.println("remove " + m.device.deviceId);
						break;
				}
			});

			UsbMuxdConnection usbMuxdConnection = usbMuxdDriver.connectToFirstDevice(62078);

			new Thread(() -> {
				byte[] res = new byte[4];
				try {

					usbMuxdConnection.inputStream.read(res);
					ByteBuffer readB = ByteBuffer.allocate(res.length);
					readB.order(ByteOrder.BIG_ENDIAN);
					readB.put(res);
					int aInt = readB.getInt(0);
					byte[] body = new byte[aInt];
					usbMuxdConnection.inputStream.read(body);
					NSObject parse = PropertyListParser.parse(body);
					System.out.println(parse.toXMLPropertyList());
				} catch (Exception e) {
					e.printStackTrace();
				}


			}).start();

			byte[] bytes = PlistMessageService.tryLockDown();

			ByteBuffer buffer = ByteBuffer.allocate(
					4);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putInt(bytes.length);
			usbMuxdConnection.outputStream.write(buffer.array());


			buffer = ByteBuffer.allocate(
					bytes.length);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.put(bytes);
			usbMuxdConnection.outputStream.write(bytes);


			//Thread.sleep(10000);wri
			//usbMuxdDriver.stopListening();
			/*
			UsbMuxdConnection usbMuxdConnection = usbMuxdDriver.connectToFirstDevice(62078);
			System.out.println("connected to Device [" + usbMuxdConnection.device.deviceId + "/" + usbMuxdConnection.device.serialNumber + "]");
			Collection<Device> devices = usbMuxdDriver.connectedDevices();
			devices.stream().forEach(d-> System.out.println(d.serialNumber));*/
		} catch (UsbMuxdException e) {
			e.printStackTrace();
		}
	}

}
