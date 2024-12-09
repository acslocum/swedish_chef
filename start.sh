#!/bin/bash

/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/bin/java -classpath .:./lib/libusb4java-1.3.0-darwin-x86-64.jar:./lib/usb4java-1.3.0.jar:./lib/commons-lang3-3.8.1.jar Scooby


#compile: javac -classpath ./lib/libusb4java-1.3.0-darwin-x86-64.jar:./lib/usb4java-1.3.0.jar:./lib/commons-lang3-3.8.1.jar Scooby.java