package com.mogaleaf.usbmuxd.api;

import com.mogaleaf.usbmuxd.api.exception.UsbMuxdException;
import com.mogaleaf.usbmuxd.api.model.Device;
import com.mogaleaf.usbmuxd.api.model.DeviceConnectionMessage;
import com.mogaleaf.usbmuxd.api.model.UsbMuxdConnection;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/*
*
* Communication on Usbmuxd.
*
*/
public interface IUsbMuxd {
	/**
	 * Get a Copy of All connected Devices.
	 *
	 * @return A collection of devices connected via USB.
	 */
	Collection<Device> connectedDevices();

	/**
	 * Connect to the first find device, stop if too long.
	 *
	 * @return I/O and Device .
	 */
	UsbMuxdConnection connectToFirstDevice(int port, long time, TimeUnit timeUnit) throws UsbMuxdException;

	/**
	 * Connect to the first find device.
	 *
	 * @return I/O and Device .
	 */
	UsbMuxdConnection connectToFirstDevice(int port) throws UsbMuxdException;

	/**
	 * Connect to a device.
	 *
	 * @return I/O and Device .
	 */
	UsbMuxdConnection connectToDevice(int port, Device device) throws UsbMuxdException;

	/**
	 * Try to Connect to a device before the timeout
	 *
	 * @return I/O and Device .
	 */
	UsbMuxdConnection connectToDevice(int port, Device device, long time, TimeUnit timeUnit) throws UsbMuxdException;

	/**
	 * start listening on incoming device connection/disconnection.
	 */
	void startListening() throws UsbMuxdException;

	/**
	 * stop listening on incoming device connection/disconnection
	 */
	void stopListening() throws UsbMuxdException;

	/**
	 * Register a new DeviceConnection consumer
	 *
	 * @return unique id on the listner
	 */
	String registerDeviceConnectionListener(Consumer<DeviceConnectionMessage> consumer);

	/**
	 * Unegister a new DeviceConnection consumer
	 *
	 */
	void unRegisterDeviceConnectionListener(String uid);
}
