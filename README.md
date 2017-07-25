# Detect and communicate with your iphone/ipad.

- API to use usbmuxd in java.
- If using windows, itunes must have been installed in order to have usbmuxd drivers.

### Easy to use :

**Connect to the first device :**

``` IUsbMuxd usbMuxdDriver = UsbMuxdFactory.getInstance();
UsbMuxdConnection usbMuxdConnection = usbMuxdDriver.connectToFirstDevice(62078);
usbMuxdConnection.outputStream.write(/*anyByte*/0);
```

**Listen to devices connection :**

```
usbMuxdDriver.registerDeviceConnectionListener(m->{
 				switch (m.type){
 					case Add:
 						System.out.println("add " + m.device.deviceId);
 						break;
 					case Remove:
 						System.out.println("remove " + m.device.deviceId);
 						break;
 				}
 			});
usbMuxdDriver.startListening();
 ```
