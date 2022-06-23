> **Work-in-progress.** 

> This repository contains a work-in-progress version of PyLeap for Android.



## Introduction

Wirelessly transfer files to and from file transfer ready BluetoothA Bluetooth Low Energy File Transfer native iOS app written in Swift for Adafruit Industries. This app will make it accessible to send files via BLE (Bluetooth LE) to your Adafruit hardware on the go!


### Hardware
Currently testing with:

• [Adafruit Circuit Playground Bluefruit with nRF52840](https://www.adafruit.com/product/4333) 

• [Adafruit CLUE nRF52840 Express with nRF52840](https://www.adafruit.com/product/4500)



## Installing the debug APK

#### Android Permissions to sideload apps

Installing an APK from outside the Google PlayStore needs 2 permissions:

- Grant permission to the browser or files app to install apps (depending of the manufacturer you can unzip and install directly from the browser or you need to use the files app)
 
- Grant permisssion to install apps from unknown sources

Both permissions can be found on the device Settings but usually they will asked interactively when the app is downloaded (which is easier than to find them on the Settings menu that can be different for each manufacturer)

### Install steps

1- Download the "PyLeap debug APK" artifact from GitHub actions using the browser on your Android device

> **Note:** The browser may ask for permission to download the file. Grant it.

2- Click on the downloaded zip file (there should appear a notification with the direct link to the file when the download finishes) and extract the contents (an APK file)

> **Note:** The zip extraction works directly on devices by some manufacturers (like Samsung). If it complains that the file can not be opened, go to the Files apps and open it from there to extract the zip contents.

3- Click on the apk file and it should start the installation

> **Install permission:** A dialog may appear saying that an app from unknown sources can not be installed. Click on the button on that dialog that directs to Settings to grant that permission.  
 
> **Play Protect warning:** The installation can be blocked by "Play Protect". A dialog will be shown saying that Play Protect does not recognize the developer for this app. Click "Install" (Warning: there is an "Accept" button that will not install the app, because it will accept the block suggested by Play Protect)

4- PyLeap should be installed now

> **Note:** The first time running PyLeap it will ask for Bluetooth permission (or location permission on devices with Android less than 12). Please grant it.




