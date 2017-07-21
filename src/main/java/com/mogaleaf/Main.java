package com.mogaleaf;

import com.mogaleaf.usbmuxd.api.IUsbMuxd;
import com.mogaleaf.usbmuxd.api.UsbMuxdFactory;
import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.Device;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;
import com.mogaleaf.usbmuxd.protocol.win.UsbMuxdWindows;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;


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
			//Thread.sleep(10000);
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
