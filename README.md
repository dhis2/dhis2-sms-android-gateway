DHIS2-SMS-Android-Gateway
=========================
This repository contains the android gateway app for testing the SMS sync functionality of the tracker capture app.

**How to use it** 

Ideally you want to test it with 2 android phones that have SIM cards :
- Install the tracker capture app in one android device.
	- Configure the SMS settings (gateway and enable submission)
- Install, compile and run the android gateway app in another device
	- Make sure that you are accepting SMS permissions for this app
	- Configure the user, password and dhis2 server URL (2.33 V) for ex: http://android2.dhis2.org:8080/
- From the android capture app sync with SMS
- Check that an alert shows up in the gateway app telling you that the message was delivered to the dhis2 server.
- Go to dhis2 server mobile app > received SMS and verify the SMS is received with “PROCESSED” state. Then the information you modified in the app should be reflected in the server.
