package com.mogaleaf.usbmuxd.protocol.win;

import com.mogaleaf.usbmuxd.protocol.UsbMuxdImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class UsbMuxdWindows extends UsbMuxdImpl {

	private static int DEFAULT_PORT_NUMBER = 27015;


	@Override
	public SocketAddress getAddress() {
		return  new InetSocketAddress("127.0.0.1", DEFAULT_PORT_NUMBER);
	}

	@Override
	protected Socket getSocketImpl() throws IOException {
		return new Socket();
	}


}
