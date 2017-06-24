package com.mogaleaf.usbmuxd.protocol.win;

import com.mogaleaf.usbmuxd.protocol.AbstractUsbMuxdDriver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by cecil on 6/23/2017.
 */
public class UsbMuxdDriverWindows extends AbstractUsbMuxdDriver {

	private static int DEFAULT_PORT_NUMBER = 27015;

	@Override
	public SocketAddress getConnectionSocketAddress() {
		return  new InetSocketAddress("127.0.0.1", DEFAULT_PORT_NUMBER);
	}
}
