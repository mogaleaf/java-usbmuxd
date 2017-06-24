package com.mogaleaf.usbmuxd.api.model;

public class DeviceAttachMessage extends Message  {
	public String serialNumber;
	public String productId;
	public String locationId;
	public String deviceId;
	public String connectionType;

	public DeviceAttachMessage(){
		messageType = MessageType.Attached;
	}
}
