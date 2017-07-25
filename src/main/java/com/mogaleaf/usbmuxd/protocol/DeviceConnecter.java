package com.mogaleaf.usbmuxd.protocol;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;

import java.io.InputStream;

public class DeviceConnecter {


	public ConnectedMessage getConnectionResult(InputStream inputStream) {
		try {
			int size = PlistMessageService.getSize(inputStream);
			if (size > 0) {
				NSDictionary dico = PlistMessageService.getNsDictionary(inputStream, size);
				PlistMessageService.ResultType messageTypeEnum = PlistMessageService.retrieveMsgType(dico);
				switch (messageTypeEnum) {
					case Result:
						NSNumber statusS = (NSNumber) dico.get("Number");
						int status = statusS.intValue();
						ConnectedMessage connectedMessage = new ConnectedMessage();
						connectedMessage.result = status;
						return connectedMessage;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getConnectionResult(inputStream);
	}

}
