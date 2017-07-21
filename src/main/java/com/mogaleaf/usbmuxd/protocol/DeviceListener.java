package com.mogaleaf.usbmuxd.protocol;

import com.dd.plist.NSDictionary;
import com.mogaleaf.usbmuxd.api.model.Device;
import com.mogaleaf.usbmuxd.api.model.DeviceConnectionMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DeviceListener implements Runnable {


	private InputStream inputStream;

	private boolean running = false;

	private final Map<String, Consumer<DeviceConnectionMessage>> consumers = Collections.synchronizedMap(new HashMap<>());


	public String register(Consumer<DeviceConnectionMessage> deviceConnectionListener) {
		String uid = UUID.randomUUID().toString();
		consumers.put(uid, deviceConnectionListener);
		return uid;
	}

	public void unregister(String uid) {
		consumers.remove(uid);
	}

	public void start(InputStream inputStream) {
		this.inputStream = inputStream;
		running = true;
	}


	@Override
	public void run() {
		while (running) {
			try {
				int size = PlistMessageService.getSize(inputStream);
				if (size > 0) {
					NSDictionary dico = PlistMessageService.getNsDictionary(inputStream, size);
					PlistMessageService.ResultType messageTypeEnum = PlistMessageService.retrieveMsgType(dico);
					DeviceConnectionMessage deviceConnectionMessage = new DeviceConnectionMessage();
					switch (messageTypeEnum) {
						case Attached:
							Device deviceAttachMessage = buildDevice(dico);
							deviceConnectionMessage.device = deviceAttachMessage;
							deviceConnectionMessage.type = DeviceConnectionMessage.Type.Add;
							notify(deviceConnectionMessage);
							break;
						case Detached:
							Device deviceDetachMessage = new Device();
							deviceDetachMessage.deviceId = Integer.valueOf(dico.get("DeviceID").toString());
							deviceConnectionMessage.device = deviceDetachMessage;
							deviceConnectionMessage.type = DeviceConnectionMessage.Type.Remove;
							notify(deviceConnectionMessage);
					}
				}
			} catch (Exception e) {
				stop();
				e.printStackTrace();
			}
		}
	}

	private void notify(DeviceConnectionMessage deviceMsg) {
		synchronized (consumers) {
			consumers.values().forEach(c -> c.accept(deviceMsg));
		}
	}


	private Device buildDevice(NSDictionary dico) {
		Device deviceAttachMessage = new Device();
		NSDictionary properties = (NSDictionary) dico.get("Properties");
		if (properties != null) {
			deviceAttachMessage.serialNumber = properties.get("SerialNumber").toString();
			deviceAttachMessage.connectionType = properties.get("ConnectionType").toString();
			deviceAttachMessage.deviceId = Integer.valueOf(properties.get("DeviceID").toString());
			deviceAttachMessage.locationId = properties.get("LocationID").toString();
			deviceAttachMessage.productId = properties.get("ProductID").toString();
		}
		return deviceAttachMessage;
	}


	public void stop() {
		if (running) {
			try {
				inputStream.close();
			} catch (IOException e) {
				inputStream = null;
			}
		}
		running = false;
	}
}
