package com.mogaleaf.usbmuxd.api;

import com.mogaleaf.usbmuxd.protocol.linux.UsbMuxdLinux;
import com.mogaleaf.usbmuxd.protocol.win.UsbMuxdWindows;

public class UsbMuxdFactory {

	public static IUsbMuxd getInstance() {
		String property = System.getProperty("os.name");
		if (property.startsWith("Window")) {
			return new UsbMuxdWindows();
		} else {
			return new UsbMuxdLinux();
		}
	}
}
