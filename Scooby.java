import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsbException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Scooby extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	static boolean playingRandom = false;
	static MediaPlayer baselinePlayer;
	static MediaPlayer randomPlayer;
	List<File> videos;
	Stack<File> currentVideos;
	final int EASTER_EGG_PERCENT = 1;
	int lastPlayed = 0;

	
	
	@Override
	public void init() throws Exception {
		videos = initFileList("./videos/");
	}
	
	public List<File> initFileList(String directory) {
		File file = new File(directory);
		File[] allFiles = file.listFiles();
		return villainVideoList(allFiles,".mp4");
		
	}

	@Override
	public void start(Stage primaryStage) {
		System.getProperties().setProperty("javafx.animation.framerate", "30");
		StackPane root = new StackPane();
		MediaView mediaView = new MediaView(getBaselineVideo());
		root.getChildren().add(mediaView);
		Scene scene = new Scene(root, 1024, 768, Color.BLACK);
		mediaView.fitWidthProperty().bind(scene.widthProperty());
		mediaView.fitHeightProperty().bind(scene.heightProperty());

		scene.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (!Scooby.playingRandom) {
				Scooby.playingRandom = true;
				getBaselineVideo().stop();
				Scooby.randomPlayer = getRandomVideo(mediaView);
				mediaView.setMediaPlayer(Scooby.randomPlayer);
				Scooby.randomPlayer.play();
				fireSignalToChef();
			}
		});

		primaryStage.setScene(scene);
		primaryStage.show();
		getBaselineVideo().play();
	}
	
	private void fireSignalToChef() {
		Thread thread = new Thread() {

			@Override
			public void run() {
				for(int i=0; i<14; i++) {
					writeToUSB();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	protected void writeToUSB() {
		Context context = new Context();
		LibUsb.init(context);
		DeviceList deviceList = new DeviceList();
		DeviceHandle handle = new DeviceHandle();
		LibUsb.getDeviceList(context, deviceList);
		System.out.println("Device count: "+ deviceList.getSize());
		for(Device device: deviceList)
		{
			DeviceDescriptor descriptor = new DeviceDescriptor();
			int result = LibUsb.getDeviceDescriptor(device, descriptor);
			if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
			System.out.println("Vendor: " + descriptor.idVendor() + " Product: " + descriptor.idProduct() + " string: " + device.toString());
		}
		Device device = deviceList.get(0);
		int result = LibUsb.open(device, handle);
		int timeout = 5000;
		if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result);
		try
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(8);
			buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
			int transfered = LibUsb.controlTransfer(handle, 
			    (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
			    (byte) 0x09, (short) 2, (short) 1, buffer, timeout);
			if (transfered < 0) throw new LibUsbException("Control transfer failed", transfered);
			System.out.println(transfered + " bytes sent");
		}
		finally
		{
		    LibUsb.close(handle);
		}
		
	}

	public void resetVideos() {
		currentVideos = new Stack<File>();
		List<File> videoList = new ArrayList<File>(videos);
		Collections.shuffle(videoList);
		currentVideos.addAll(videoList);
	}
	
	public File nextVideo() {
		if(currentVideos==null || currentVideos.isEmpty()) {
			resetVideos();
		}
		File videoToPlay = null;
		videoToPlay = currentVideos.pop();
		return videoToPlay;
	}

	private static MediaPlayer getBaselineVideo() {
		if (Scooby.baselinePlayer == null) {
			Scooby.baselinePlayer = new MediaPlayer(
					new Media(Scooby.class.getResource("baseline.mp4").toExternalForm()));
			Scooby.baselinePlayer.setCycleCount(MediaPlayer.INDEFINITE);
		}
		return Scooby.baselinePlayer;

	}

	private MediaPlayer getRandomVideo(MediaView mediaView) {
		Scooby.randomPlayer = new MediaPlayer(
				new Media(Scooby.class.getResource(nextVideo().getPath()).toExternalForm()));
		randomPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				Scooby.randomPlayer.dispose();
				Scooby.playingRandom = false;
				mediaView.setMediaPlayer(getBaselineVideo());
				getBaselineVideo().play();

			}
		});
		return randomPlayer;
	}

	private List<File> villainVideoList(File[] allFiles, String extension) {
		List<File> videos = new ArrayList<File>();
		for (int i = 0; i < allFiles.length; i++)
			if (allFiles[i].getName().contains(extension))
				videos.add(allFiles[i]);
		return videos;
	}

}