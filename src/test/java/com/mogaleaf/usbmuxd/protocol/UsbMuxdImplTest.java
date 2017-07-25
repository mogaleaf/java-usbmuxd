package com.mogaleaf.usbmuxd.protocol;

import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.Device;
import com.mogaleaf.usbmuxd.api.model.DeviceConnectionMessage;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsbMuxdImplTest {

	UsbMuxdImpl instance;

	@Mock
	SocketAddress mockSocketAddress;

	@Mock
	Socket mockSocket;

	@Mock
	DeviceListener deviceListener;

	@Mock
	DeviceConnecter deviceConnecter;

	@Mock
	OutputStream outputStream;

	@Mock
	InputStream inputStream;


	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(mockSocket.getOutputStream()).thenReturn(outputStream);
		when(mockSocket.getInputStream()).thenReturn(inputStream);
		instance = new UsbMuxdImpl() {
			@Override
			protected SocketAddress getAddress() throws IOException {
				return mockSocketAddress;
			}

			@Override
			protected Socket getSocketImpl() throws IOException {
				return mockSocket;
			}
		};
		instance.deviceListener = deviceListener;
		instance.deviceConnecter = deviceConnecter;
	}

	@Test
	public void testconnectToFirstDevice() throws UsbMuxdException, InterruptedException {
		ConnectedMessage connectedMessage = new ConnectedMessage();
		connectedMessage.result = 0;
		ArgumentCaptor<Consumer> argumentCaptor = ArgumentCaptor.forClass(Consumer.class);
		when(deviceListener.register(argumentCaptor.capture())).thenReturn("test");
		when(deviceConnecter.getConnectionResult(anyObject())).thenReturn(connectedMessage);
		instance.isStarted = true;
		DeviceConnectionMessage fakeMessage = new DeviceConnectionMessage();
		fakeMessage.type = DeviceConnectionMessage.Type.Add;
		fakeMessage.device = new Device();
		fakeMessage.device.deviceId = 2;

		Thread producerThread = new Thread(() -> {
			int size = argumentCaptor.getAllValues().size();
			while (size == 0) {
				size = argumentCaptor.getAllValues().size();
			}
			argumentCaptor.getValue().accept(fakeMessage);
		});
		producerThread.start();

		Thread consumerThread = new Thread(() -> {
			try {
				UsbMuxdConnection usbMuxdConnection = instance.connectToFirstDevice(2044);
				verify(deviceListener).unregister("test");
				assertThat(usbMuxdConnection.device).isSameAs(fakeMessage.device);

			} catch (UsbMuxdException e) {
				fail(e.getMessage());
			}
		});
		consumerThread.start();

		producerThread.join(1000);
		consumerThread.join(1000);


	}

	@Test
	public void testStartListening() {
		try {
			ArgumentCaptor<Consumer> argumentCaptor = ArgumentCaptor.forClass(Consumer.class);
			instance.startListening();
			verify(deviceListener).register(argumentCaptor.capture());
			verify(deviceListener).start(inputStream);
			verify(outputStream).write(any());
			assertThat(instance.isStarted).isTrue();

		} catch (UsbMuxdException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void stopListening() {
		try {
			when(deviceListener.register(any())).thenReturn("test");
			instance.startListening();
			instance.stopListening();
			verify(deviceListener).unregister("test");
			verify(deviceListener).stop();
			assertThat(instance.isStarted).isFalse();
		} catch (UsbMuxdException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testConnectedDevices(){
		Collection<Device> devices = instance.connectedDevices();
		assertThat(devices).isNotSameAs(instance.connectedDevices());
	}

}
