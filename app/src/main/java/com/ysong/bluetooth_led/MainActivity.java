package com.ysong.bluetooth_led;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private static final int TIME_RESOLUTION = 20;

	private Toast toast = null;
	private RadioButton radioOn = null;
	private RadioButton radioBlink = null;
	private RadioButton radioFade = null;
	private TextView textRise = null;
	private TextView textFall = null;
	private TextView textRed = null;
	private TextView textGreen = null;
	private TextView textBlue = null;
	private TextView textHue = null;
	private SeekBar seekRise = null;
	private SeekBar seekFall = null;
	private SeekBar seekRed = null;
	private SeekBar seekGreen = null;
	private SeekBar seekBlue = null;
	private SeekBar seekHue = null;
	private BluetoothSerial bluetoothSerial = null;
	private boolean asyncTaskEnabled = false;
	private boolean update = false;
	private byte[] data = new byte[12];

	private class UpdateAsyncTask extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... v) {
			while (asyncTaskEnabled) {
				try {
					if (update) {
						bluetoothSerial.write(data);
						update = false;
					}
					Thread.sleep(25);
				} catch (Exception e) {
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... msg) {
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		radioOn = (RadioButton)findViewById(R.id.rdo_on);
		radioBlink = (RadioButton)findViewById(R.id.rdo_blink);
		radioFade = (RadioButton)findViewById(R.id.rdo_fade);
		textRise = (TextView)findViewById(R.id.text_rise);
		textRise.setText("Rise time: " + TIME_RESOLUTION + " ms");
		textFall = (TextView)findViewById(R.id.text_fall);
		textFall.setText("Fall time: " + TIME_RESOLUTION + " ms");
		textRed = (TextView)findViewById(R.id.text_red);
		textRed.setText("Red: 0");
		textGreen = (TextView)findViewById(R.id.text_green);
		textGreen.setText("Green: 0");
		textBlue = (TextView)findViewById(R.id.text_blue);
		textBlue.setText("Blue: 0");
		textHue = (TextView)findViewById(R.id.text_hue);
		textHue.setText("Hue: 0");
		seekRise = (SeekBar)findViewById(R.id.seek_rise);
		seekFall = (SeekBar)findViewById(R.id.seek_fall);
		seekRed = (SeekBar)findViewById(R.id.seek_red);
		seekGreen = (SeekBar)findViewById(R.id.seek_green);
		seekBlue = (SeekBar)findViewById(R.id.seek_blue);
		seekHue = (SeekBar)findViewById(R.id.seek_hue);
		setAllEnabled(false);
		bluetoothSerial = new BluetoothSerial(this);
		dataInit();
		seekRise.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				progressValue++;
				System.arraycopy(toHex8(progressValue), 0, data, 1, 2);
				update = true;
				textRise.setText("Rise time: " + progressValue * TIME_RESOLUTION + " ms");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekRise.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
		seekFall.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				progressValue++;
				System.arraycopy(toHex8(progressValue), 0, data, 3, 2);
				update = true;
				textFall.setText("Fall time: " + progressValue * TIME_RESOLUTION + " ms");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekFall.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
		seekRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				System.arraycopy(toHex8(progressValue), 0, data, 5, 2);
				update = true;
				textRed.setText("Red: " + progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekRed.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
		seekGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				System.arraycopy(toHex8(progressValue), 0, data, 7, 2);
				update = true;
				textGreen.setText("Green: " + progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekGreen.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
		seekBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				System.arraycopy(toHex8(progressValue), 0, data, 9, 2);
				update = true;
				textBlue.setText("Blue: " + progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekBlue.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
		seekHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				textHue.setText("Hue: " + progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setAllEnabled(false);
				seekHue.setEnabled(true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setAllEnabled(true);
			}
		});
	}

	@Override
	protected void onDestroy() {
		setAllEnabled(false);
		asyncTaskEnabled = false;
		bluetoothSerial.poisonPill();
		try {
			bluetoothSerial.disconnect();
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	public void onCxnHandler(View view) {
		try {
			bluetoothSerial.connect(0);
			toastShow("Connect success");
			asyncTaskEnabled = true;
			new UpdateAsyncTask().execute();
			setAllEnabled(true);
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	public void onDxnHandler(View view) {
		setAllEnabled(false);
		asyncTaskEnabled = false;
		bluetoothSerial.poisonPill();
		try {
			bluetoothSerial.disconnect();
			toastShow("Disconnect success");
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	public void onRdoHandler(View view) {
		switch (view.getId()) {
			case R.id.rdo_on:
				data[0] = 1;
				break;
			case R.id.rdo_blink:
				data[0] = 2;
				break;
			case R.id.rdo_fade:
				data[0] = 3;
				break;
			default:
				break;
		}
		update = true;
	}

	private void setAllEnabled(boolean enabled) {
		radioOn.setEnabled(enabled);
		radioBlink.setEnabled(enabled);
		radioFade.setEnabled(enabled);
		seekRise.setEnabled(enabled);
		seekFall.setEnabled(enabled);
		seekRed.setEnabled(enabled);
		seekGreen.setEnabled(enabled);
		seekBlue.setEnabled(enabled);
		seekHue.setEnabled(enabled);
	}

	private void dataInit() {
		data[0] = 0;
		data[1] = 49;
		data[2] = 48;
		data[3] = 49;
		data[4] = 48;
		data[11] = 10;
		for (int i = 5; i < 11; i++) {
			data[i] = 48;
		}
	}

	private byte[] toHex8(int x) {
		byte[] hex = new byte[2];
		hex[0] = (byte) ((x & 0x0F) + 48);
		hex[1] = (byte) (((x >> 4) & 0x0F) + 48);
		return hex;
	}

	private void toastShow(String str) {
		toast.setText(str);
		toast.show();
	}
}
