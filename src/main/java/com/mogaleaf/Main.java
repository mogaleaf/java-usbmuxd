package com.mogaleaf;

import com.mogaleaf.usbmuxd.api.UsbMuxdDriver;
import com.mogaleaf.usbmuxd.api.model.DeviceAttachMessage;
import com.mogaleaf.usbmuxd.api.model.DeviceDetachMessage;
import com.mogaleaf.usbmuxd.api.model.Message;
import com.mogaleaf.usbmuxd.protocol.win.UsbMuxdDriverWindows;

import java.io.IOException;


public class Main {
	public static void main(String args[]) throws IOException {
		UsbMuxdDriver usbMuxdDriver = new UsbMuxdDriverWindows();
		usbMuxdDriver.listenDeviceAttachEvent(Main::handle);
	}

	private static void handle(Message message) {
		switch (message.messageType) {
			case Attached:
				DeviceAttachMessage deviceAttachMessage = (DeviceAttachMessage) message;
				System.out.println("device id/serial : " + deviceAttachMessage.deviceId + "/" + deviceAttachMessage.serialNumber + " is attach.");
				break;
			case Detached:
				DeviceDetachMessage deviceDetachMessage = (DeviceDetachMessage) message;
				System.out.println("device id " + deviceDetachMessage.deviceId + " is detach.");
		}
	}
}
