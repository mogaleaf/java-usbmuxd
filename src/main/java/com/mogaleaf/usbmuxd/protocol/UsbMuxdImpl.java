package com.mogaleaf.usbmuxd.protocol;

import com.mogaleaf.usbmuxd.api.IUsbMuxd;
import com.mogaleaf.usbmuxd.api.exception.UsbMuxdConnectException;
import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.Device;
import com.mogaleaf.usbmuxd.api.model.DeviceConnectionMessage;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class UsbMuxdImpl implements IUsbMuxd {

	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	protected DeviceListener deviceListener = new DeviceListener();
	protected DeviceConnecter deviceConnecter = new DeviceConnecter();
	private Map<Integer, Device> connectedDevices = Collections.synchronizedMap(new LinkedHashMap<>());
	protected boolean isStarted = false;
	private Socket connectionListeningSocket;
	private String registerUid;

	protected abstract SocketAddress getAddress() throws IOException;
	protected abstract Socket getSocketImpl() throws IOException;

	@Override
	public Collection<Device> connectedDevices() {
		ArrayList<Device> copy = new ArrayList<>();
		copy.addAll(connectedDevices.values());
		return copy;
	}

	@Override
	public UsbMuxdConnection connectToFirstDevice(int port, long time, TimeUnit timeUnit) throws UsbMuxdException {
		try {
			if (isStarted && !connectedDevices.isEmpty()) {
				return connectToDevice(port, connectedDevices.values().iterator().next());
			}
			CountDownLatch countDownLatch = new CountDownLatch(1);
			Device[] singletonDevice = new Device[1];
			String register = deviceListener.register(m -> {
				singletonDevice[0] = m.device;
				countDownLatch.countDown();
			});
			if (!isStarted) {
				startListening();
			}
			if (time > 0) {
				countDownLatch.await(time, timeUnit);
			} else {
				countDownLatch.await();
			}
			deviceListener.unregister(register);
			return connectToDevice(port, singletonDevice[0]);
		} catch (UsbMuxdConnectException e) {
			throw e;
		} catch (Exception e) {
			throw new UsbMuxdException(e);
		}
	}

	@Override
	public UsbMuxdConnection connectToFirstDevice(int port) throws UsbMuxdException {
		return connectToFirstDevice(port, -1, null);
	}

	@Override
	public UsbMuxdConnection connectToDevice(int port, Device device) throws UsbMuxdException {
		return connectToDevice(port, device, -1, null);
	}

	@Override
	public UsbMuxdConnection connectToDevice(int port, Device device, long time, TimeUnit timeUnit) throws UsbMuxdException {
		try {
			Socket connectionSocket = getSocketImpl();
			connectionSocket.connect(getAddress());
			byte[] connectByteMessage = PlistMessageService.buildConnectMsg(device.deviceId, port);
			InputStream inputStream = connectionSocket.getInputStream();
			Future<ConnectedMessage> submit = executorService.submit(() -> deviceConnecter.getConnectionResult(inputStream));
			connectionSocket.getOutputStream().write(connectByteMessage);
			ConnectedMessage connectedMessage;
			if (time > 0) {
				connectedMessage = submit.get(time, timeUnit);
			} else {
				connectedMessage = submit.get();
			}
			if (connectedMessage.result != 0) {
				connectedMessage.throwException();
			}
			UsbMuxdConnection usbMuxdConnection = new UsbMuxdConnection();
			usbMuxdConnection.inputStream = inputStream;
			usbMuxdConnection.outputStream = connectionSocket.getOutputStream();
			usbMuxdConnection.device = device;
			return usbMuxdConnection;
		} catch (UsbMuxdConnectException e) {
			throw e;
		} catch (Exception e) {
			throw new UsbMuxdException(e);
		}
	}

	@Override
	public void startListening() throws UsbMuxdException {
		try {
			connectionListeningSocket = getSocketImpl();
			connectionListeningSocket.connect(getAddress());
			byte[] connectByteMessage = PlistMessageService.buildListenConnectionMsg();
			InputStream inputStream = connectionListeningSocket.getInputStream();
			deviceListener.start(inputStream);
			isStarted = true;
			registerUid = deviceListener.register(m -> {
				switch (m.type) {
					case Add:
						connectedDevices.put(m.device.deviceId, m.device);
						break;
					case Remove:
						connectedDevices.remove(m.device.deviceId);
						break;
				}
			});
			connectionListeningSocket.getOutputStream().write(connectByteMessage);
			executorService.execute(deviceListener);
		} catch (Exception e) {
			throw new UsbMuxdException(e);
		}
	}

	@Override
	public void stopListening() throws UsbMuxdException {
		if (isStarted) {
			deviceListener.stop();
			deviceListener.unregister(registerUid);
			try {
				connectionListeningSocket.close();
			} catch (IOException e) {
				connectionListeningSocket = null;
			}
		}
		isStarted = false;
	}

	@Override
	public String registerDeviceConnectionListener(Consumer<DeviceConnectionMessage> consumer) {
		return deviceListener.register(consumer);
	}

	@Override
	public void unRegisterDeviceConnectionListener(String uid) {
		deviceListener.unregister(uid);
	}
}
