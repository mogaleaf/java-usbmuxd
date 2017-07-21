package com.mogaleaf.usbmuxd.protocol;

import com.mogaleaf.usbmuxd.api.exception.UsbMuxdConnectException;

public class ConnectedMessage {
	public int result;

	public void throwException() throws UsbMuxdConnectException {
		switch(result){
			case 2:
				throw new UsbMuxdConnectException("Device not connected");
			case 3:
				throw new UsbMuxdConnectException("Connection refused");
			case 0:
				return;
			default:
				throw new UsbMuxdConnectException("Not connected for unknown reason");
		}
	}
}
