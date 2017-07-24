package com.mogaleaf;

import com.mogaleaf.usbmuxd.api.IUsbMuxd;
import com.mogaleaf.usbmuxd.api.UsbMuxdFactory;
import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;

import java.io.IOException;


public class Main {
	static IUsbMuxd usbMuxdDriver = UsbMuxdFactory.getInstance();

	public static void main(String args[]) throws IOException, InterruptedException {
		try {

			usbMuxdDriver.registerDeviceConnectionListener(m->{
				switch (m.type){
					case Add:
						System.out.println("add " + m.device.deviceId);
						break;
					case Remove:
						System.out.println("remove " + m.device.deviceId);
						break;
				}
			});
			usbMuxdDriver.startListening();

			UsbMuxdConnection usbMuxdConnection = usbMuxdDriver.connectToFirstDevice(62078);
			usbMuxdConnection.outputStream.write(/*anyByte*/0);
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
