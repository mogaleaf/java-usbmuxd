package com.mogaleaf.usbmuxd.api.model;

/*
* Connection or Disconnection message for a Device.
 */
public class DeviceConnectionMessage {
	public Device device;
	public Type type;

	public enum Type {
		Add, Remove
	}
}
