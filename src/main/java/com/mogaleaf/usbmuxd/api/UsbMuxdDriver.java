package com.mogaleaf.usbmuxd.api;


import com.mogaleaf.usbmuxd.api.model.DeviceAttachMessage;
import com.mogaleaf.usbmuxd.api.model.Message;

import java.io.IOException;
import java.util.function.Consumer;

public interface UsbMuxdDriver {

	void listenDeviceAttachEvent(Consumer<Message> callback) throws IOException;

	void connectDevice(String deviceId);

	void disconnectDevice(String deviceId);
}
