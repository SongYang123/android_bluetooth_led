package com.ysong.bluetooth_led;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BluetoothSerial {

	private static final int BUFFER_SIZE = 1024;
	private static final int DELIMITER = 10;

	private Activity activity = null;
	private BluetoothAdapter bluetoothAdapter = null;
	private BluetoothDevice bluetoothDevice = null;
	private BluetoothSocket bluetoothSocket = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private BlockingQueue<byte[]> readQ = new LinkedBlockingQueue<>(8);
	private boolean threadEnabled = false;
	private boolean socketLocked = true;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private int frameSize;
	private int count;

	private class ReadDelimiterThread implements Runnable {
		@Override
		public void run() {
			int count = 0;
			while (threadEnabled) {
				try {
					int b = inputStream.read();
					if (b != DELIMITER) {
						buffer[count] = (byte) b;
						count++;
					} else if (b == DELIMITER && count > 0) {
						byte[] data = new byte[count];
						System.arraycopy(buffer, 0, data, 0, count);
						readQ.offer(data);
						count = 0;
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private class ReadFrameThread implements Runnable {
		@Override
		public void run() {
			count = 0;
			while (threadEnabled) {
				try {
					int b = inputStream.read();
					buffer[count] = (byte) b;
					if (count < frameSize - 1) {
						count++;
					} else {
						byte[] data = new byte[frameSize];
						System.arraycopy(buffer, 0, data, 0, frameSize);
						readQ.offer(data);
						count = 0;
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public BluetoothSerial(Activity activity) {
		this.activity = activity;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void connect(int frameSize) throws Exception {
		if (bluetoothAdapter == null) {
			throw new Exception("Bluetooth not supported on this device");
		} else if (!bluetoothAdapter.isEnabled()) {
			activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
		} else if (bluetoothDevice != null) {
			throw new Exception("Connect already");
		} else {
			for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
				if (device.getName().equals("skin_motion_capture")) {
					bluetoothDevice = device;
					break;
				}
			}
			if (bluetoothDevice == null) {
				throw new Exception("Cannot find skin_motion_capture");
			} else {
				try {
					UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
					bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
					bluetoothSocket.connect();
					inputStream = bluetoothSocket.getInputStream();
					outputStream = bluetoothSocket.getOutputStream();
					threadEnabled = true;
					if (frameSize == 0) {
						new Thread(new ReadDelimiterThread()).start();
					} else {
						this.frameSize = frameSize;
						new Thread(new ReadFrameThread()).start();
					}
					socketLocked = false;
				} catch (Exception e) {
					throw e;
				}
			}
		}
	}

	public void disconnect() throws Exception {
		socketLocked = true;
		if (bluetoothDevice == null) {
			throw new Exception("Disconnect already");
		} else {
			String err = "";
			threadEnabled = false;
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					err += e.toString() + "\n";
				} finally {
					outputStream = null;
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					err += e.toString() + "\n";
				} finally {
					inputStream = null;
				}
			}
			if (bluetoothSocket != null) {
				try {
					bluetoothSocket.close();
				} catch (Exception e) {
					err += e.toString() + "\n";
				} finally {
					bluetoothSocket = null;
				}
			}
			bluetoothDevice = null;
			if (!err.equals("")) {
				throw new Exception(err);
			}
		}
	}

	public boolean getSocketLocked() {
		return socketLocked;
	}

	public void setSockedLocked(boolean locked) {
		socketLocked = locked;
	}

	public byte[] read() throws Exception {
		return readQ.take();
	}

	public byte[] read(long timeout) throws Exception {
		return readQ.poll(timeout, TimeUnit.MILLISECONDS);
	}

	public void write(byte[] buffer) throws Exception {
		outputStream.write(buffer);
	}

	public void clear() {
		count = 0;
	}

	public void flush() throws Exception {
		while (!readQ.isEmpty()) {
			readQ.take();
		}
	}

	public void poisonPill() {
		readQ.offer(new byte[0]);
	}
}