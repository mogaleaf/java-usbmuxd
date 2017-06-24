package com.mogaleaf.usbmuxd.api.model;

public class DeviceDetachMessage extends Message  {
	public String deviceId;

	public DeviceDetachMessage(){
		messageType = MessageType.Detached;
	}
}
