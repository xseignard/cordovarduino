package org.stereolux.cordova.serial;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.IntentFilter;
import android.util.Log;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

/**
 * Cordova plugin to communicate with the android serial port
 * @author Xavier Seignard <xavier.seignard@gmail.com>
 */
public class Serial extends CordovaPlugin {
	
    // logging tag
    private final String TAG = Serial.class.getSimpleName();
    // actions definitions
    private static final String ACTION_OPEN = "openSerial";
    private static final String ACTION_READ = "readSerial";
    private static final String ACTION_WRITE = "writeSerial";
    private static final String ACTION_CLOSE = "closeSerial";
	private static final Object ACTION_SUSCRIBE = "suscribe";
    // physicaloid object that will handle the serial connection
    private Physicaloid mPhysicaloid;
    // callback context used for subscription
    private CallbackContext suscribeCallback;
    // 
    private String suscribeData;
    
    /**
     * Overridden execute method
     * @param action the string representation of the action to execute
     * @param args
     * @param callbackContext the cordova {@link CallbackContext}
     * @return true if the action exists, false otherwise
     * @throws JSONException if the args parsing fails
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	Log.d(TAG, "Action: " + action);
        JSONObject arg_object = args.optJSONObject(0);
        // open serial port
        if (ACTION_OPEN.equals(action)) {
            JSONObject opts = arg_object.has("opts")? arg_object.getJSONObject("opts") : new JSONObject();
            openSerial(opts, callbackContext);
            return true;
        }
        // write to the serial port
        else if (ACTION_WRITE.equals(action)) {
            String data = arg_object.getString("data");
            writeSerial(data, callbackContext);
            return true;
        }
        // read on the serial port
        else if (ACTION_READ.equals(action)) {
            readSerial(callbackContext);
            return true;
        }
        // suscribe 
        else if (ACTION_SUSCRIBE.equals(action)) {
        	suscribe(callbackContext);
            return true;
        }
        // close the serial port
        else if (ACTION_CLOSE.equals(action)) {
            closeSerial(callbackContext);
            return true;
        }
        // the action doesn't exist
        return false;
    }

    /**
     * Open the serial port. It will request the user permission to do it if needed.
     * @param opts a {@link JSONObject} containing the connection paramters
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void openSerial(final JSONObject opts, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	try {
            		// create filter on the permission we ask
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(UsbBroadcastReceiver.USB_PERMISSION);
                    // this broadcast receiver will handle the permission results
                    UsbBroadcastReceiver usbReceiver = new UsbBroadcastReceiver(callbackContext);
                    cordova.getActivity().registerReceiver(usbReceiver, filter);
                    // create the physicaloid object
                    mPhysicaloid = new Physicaloid(cordova.getActivity());
                    // configure the connection with provided params and/or defaults
            		UartConfig uartConfig = new UartConfig();
	            	uartConfig.baudrate = opts.has("baudRate") ? opts.getInt("baudRate") : 9600;
	            	uartConfig.dataBits = opts.has("dataBits") ? opts.getInt("dataBits") : UartConfig.DATA_BITS8;
	            	uartConfig.stopBits = opts.has("stopBits") ? opts.getInt("stopBits") : UartConfig.STOP_BITS1;
	            	uartConfig.parity = opts.has("parity") ? opts.getInt("parity") : UartConfig.PARITY_NONE;
	
	            	if(mPhysicaloid.open(uartConfig)) {
	            		mPhysicaloid.addReadListener(new ReadLisener() {
	                        /* (non-Javadoc)
	                         * @see com.physicaloid.lib.usb.driver.uart.ReadLisener#onRead(int)
	                         */
	                        @Override
	                        public void onRead(int size) {
	                        	byte[] buf = new byte[size];
	                            mPhysicaloid.read(buf, size);
	                            Log.d(TAG, "Buf:" + buf);
	                            suscribeData = new String(buf);
	                            Log.d(TAG, "Read:" + suscribeData);
	                            if (suscribeCallback != null) {
	                            	sendDataToSubscriber();
	                            }
	                        }
	            		});
	            		Log.d(TAG, "Serial port opened!");
	                    callbackContext.success("Serial port opened!");
					}
	            	else {
	                    Log.d(TAG, "Cannot connect to the device!");
	                    callbackContext.error("Cannot connect to the device!");
	            	}
	            	// unregister the broadcast receiver since it's no longer needed
	            	cordova.getActivity().unregisterReceiver(usbReceiver);
            	}
            	catch (JSONException e) {
            		// deal with error
                    Log.d(TAG, e.getMessage());
                    callbackContext.error(e.getMessage());
            	}
            }
        });
    }

    /**
     * Write on the serial port
     * @param data the {@link String} representation of the data to be written on the port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void writeSerial(final String data, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (mPhysicaloid.isOpened()) {
                	byte[] buffer = data.getBytes();
                    mPhysicaloid.write(buffer, buffer.length);
                    callbackContext.success();
                }
                else {
                    Log.d(TAG, "Not connected to the device!");
                    callbackContext.error("Not connected to the device!");
                }
            }
        });
    }

    /**
     * Read on the serial port
     * @param callbackContext the {@link CallbackContext}
     */
    private void readSerial(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	if (mPhysicaloid.isOpened()) {
	            	byte[] buf = new byte[256];
	                mPhysicaloid.read(buf, buf.length);
	                String str = new String(buf);
	                callbackContext.success(str);
            	}
            	else {
            		Log.d(TAG, "Not connected to the device!");
                    callbackContext.error("Not connected to the device!");
            	}
            }
        });
    }
    
    private void suscribe(final CallbackContext callbackContext) {
    	cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	suscribeCallback = callbackContext;
        		
        		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
    	});
    }
    
    private void sendDataToSubscriber() {
    	if (suscribeData != null && suscribeData.length() > 0) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, suscribeData);
            result.setKeepCallback(true);
            suscribeCallback.sendPluginResult(result);
            sendDataToSubscriber();
        }
	}

    /**
     * Close the serial port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void closeSerial(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	if(mPhysicaloid.close()) {
            		callbackContext.success();
                }
            	else {
            		Log.d(TAG, "Cannot close the serial port!");
                    callbackContext.error("Cannot close the serial port!");
            	}
            }
        });
    }
    
    /**
     * Close the serial port when the user closes the app
     */
    @Override
    public void onDestroy() {
    	if(mPhysicaloid.close()) {
    		Log.d(TAG, "Closing the serial port");
        }
    	else {
    		Log.d(TAG, "Cannot close the serial port!");
    	}
    }
}