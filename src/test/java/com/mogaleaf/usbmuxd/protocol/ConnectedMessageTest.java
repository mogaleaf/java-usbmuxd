package com.mogaleaf.usbmuxd.protocol;

import com.mogaleaf.usbmuxd.api.exception.UsbMuxdConnectException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnectedMessageTest {

	ConnectedMessage instance = new ConnectedMessage();

	@Test
	public void testRes0(){
		instance.result = 0;
		try {
			instance.throwException();
		} catch (UsbMuxdConnectException e) {
			fail();
		}
	}

	@Test
	public void testRes1(){
		instance.result = 1;
		try {
			instance.throwException();
		} catch (UsbMuxdConnectException e) {
			assertThat(e.getMessage()).isEqualTo("Not connected for unknown reason");
		}
	}

	@Test
	public void testRes2(){
		instance.result = 2;
		try {
			instance.throwException();
		} catch (UsbMuxdConnectException e) {
			assertThat(e.getMessage()).isEqualTo("Device not connected");
		}
	}

	@Test
	public void testRes3(){
		instance.result = 3;
		try {
			instance.throwException();
		} catch (UsbMuxdConnectException e) {
			assertThat(e.getMessage()).isEqualTo("Connection refused");
		}
	}
}
