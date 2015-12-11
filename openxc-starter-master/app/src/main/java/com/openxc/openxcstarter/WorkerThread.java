package com.openxc.openxcstarter;

import android.util.Log;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.openxc.measurements.Measurement;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WorkerThread implements Runnable {

	private final Measurement Speed;
	private final Measurement fuelConsumed;
	private final Measurement odometer;
	private final Measurement acceleration;
	private final Measurement gear;
	private final Measurement longitude;
	private final Measurement latitude;
	private final String tripId;
	private String identificator;
	
	 
	
	    public WorkerThread(com.openxc.measurements.Measurement Speed, com.openxc.measurements.Measurement fuelConsumed, com.openxc.measurements.Measurement odometer, com.openxc.measurements.Measurement acceleration, com.openxc.measurements.Measurement gear, com.openxc.measurements.Measurement longitude, com.openxc.measurements.Measurement latitude, String tripId, String identificator){
	
	        this.Speed = Speed;
			this.fuelConsumed = fuelConsumed;
			this.odometer = odometer;
			this.acceleration = acceleration;
			this.gear = gear;
			this.longitude = longitude;
			this.latitude = latitude;
			this.tripId = tripId;
			this.identificator = identificator;

	    }
	
	 
	
	    @Override
		/**
		 * Mandatory implementation of method "run" from runnable interface
		 */
	    public void run() {
			long startTime = System.currentTimeMillis();

	        System.out.println(Thread.currentThread().getName()+ " Start. Command = "+identificator);
	
	        processCommand();

			long duration = System.currentTimeMillis() - startTime;

	        System.out.println(Thread.currentThread().getName() + " command: " + identificator + " End. Duration: " + duration);
	
	    }


		/**
		 * method to make the actual post requests to the iotService servlet
		 */
	    private void processCommand() {
			Float speedFloat;
			Float fuelConsumedFloat;
			Float accelerationFloat;
			Float odometerFloat;
			Integer gearInt;
			String gpsPosition;


			final HttpClient Apacheclient = new DefaultHttpClient();
			Apacheclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);


			//different settings to adjust apache http client to your needs

			//Apacheclient.getParams().setParameter("http.socket.sendbuffer",  <int>);
			//Apacheclient.getParams().setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, <int>);
			//Apacheclient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, true);
			//Apacheclient.getParams().setParameter("http.connection.timeout", <int>);
			//Apacheclient.getParams().setParameter("http.socket.timeout", <int>);

			//URL to iot Service

			//replace url to IoT services servlet - replace <yourAccountID> and [deviceID]
			String url = "https://iotmms<yourAccountID>trial.hanatrial.ondemand.com/com.sap.iotservices.mms/v1/api/http/data/[deviceID]";

			//Set Header data, that iot Service servlet accept post requests
			HttpPost httpPost = new HttpPost(url);

			//replace <oAuth 2.0 token>
			httpPost.addHeader("authorization", "Bearer <oAuth 2.0 token>");
			httpPost.addHeader("content-type", "application/json;charset=utf-8");
			httpPost.addHeader("cache-control", "no-cache");


			//different cars expose different data through ODB , set default value if data is not available

			if (Speed != null){
				 speedFloat = getFloatFromString(Speed);
			} else {
				speedFloat  = Float.parseFloat("0");
			};

			if (fuelConsumed != null){
				fuelConsumedFloat = getFloatFromString(fuelConsumed);
			}
			else {
				fuelConsumedFloat = Float.parseFloat("0");
			};

			if (acceleration != null){
				accelerationFloat = getFloatFromString(acceleration);
			} else {
				accelerationFloat = Float.parseFloat("0");
			};
			if (odometer != null){
				odometerFloat = getFloatFromString(odometer);
			} else {
				odometerFloat = Float.parseFloat("0");
			};
			if (gear != null) {
				gearInt = transformGeartoInt(gear);
			} else {
				gearInt = 0;
			};
			if (latitude != null && longitude != null) {

				gpsPosition = longitude.getValue() + ";" + latitude.getValue() ;
				gpsPosition = gpsPosition.replace("°","");
				gpsPosition = gpsPosition.replace(" ","");
			}
			else {
				gpsPosition = "-74.00594130000002;40.7127837";
			}



			//replace <messageType>
			String requestBody = "{\"mode\":\"sync\", \"messageType\":\"<messageType>\", \"messages\": [ {\"Speed\":" + speedFloat + ", \"FuelConsumed\":" + fuelConsumedFloat + ", \"Odometer\":" + odometerFloat + ", \"Acceleration\":" + accelerationFloat + ", \"Gear\":" + gearInt + ",\"GPSPosition\":\""+ gpsPosition +"\",\"TripId\":\"" + tripId + "\"}]}";

			try {
				StringEntity stringEntity = new StringEntity(requestBody);


				httpPost.setEntity(stringEntity);
			} catch (Exception e) {

				Log.i("Info", "Could not set entity to http Post");
			}


			try {

				org.apache.http.HttpResponse response = Apacheclient.execute(httpPost);
				InputStream body = response.getEntity().getContent();
				String content = CharStreams.toString(new InputStreamReader(body, Charsets.UTF_8));
				body.close();
				Closeables.closeQuietly(body);

				Log.i("Info", "Response from HTTP Post:" + response.getStatusLine().getReasonPhrase() + " Code :" + Integer.toString(response.getStatusLine().getStatusCode()) + " Message: " + content);

				if(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 202) {
					SimpleThreathPool.setSuccessCounter(SimpleThreathPool.getSuccessCounter() + 1);
				}
				else {
					SimpleThreathPool.setFailureCounter(SimpleThreathPool.getFailureCounter() + 1);
				}

			} catch (IOException e) {
				e.printStackTrace();

				SimpleThreathPool.setFailureCounter(SimpleThreathPool.getFailureCounter() + 1);


			}

			//Run on UI Threath in order to update UI from background thread
			StarterActivity.getStarterActivcity().runOnUiThread(new Runnable() {
				public void run() {

					StarterActivity.getStarterActivcity().getmSuccessRequests().setText("Successfull Requests: "
							+ SimpleThreathPool.getSuccessCounter());

					StarterActivity.getStarterActivcity().getmFailedRequests().setText("Failed Requests: "
							+ SimpleThreathPool.getFailureCounter());
				}
			});


		}


	/**
	 * Helper method
	 * @param raw
	 * @return
	 */
	public Float getFloatFromString(Measurement raw){
		String rawString = raw.getValue().toString();
		int index = rawString.indexOf(" ");
		String rawSpeed = rawString.substring(0, index - 1);
		return Float.parseFloat(rawSpeed);
	}

	/**
	 * Transforms a measurement object of type "gear" into a integer value
	 * @param raw
	 * @return
	 */
	public int transformGeartoInt(Measurement raw){

		switch (raw.getValue().toString()) {
			case "FIRST":

				return 1;
			case "SECOND":

				return 2;
			case "THIRD":

				return 3;
			case "FOURTH":

				return 4;
			case "FIFTH":

				return 5;
			case "SIXTH":

				return 6;
			default:

				return Integer.parseInt(String.valueOf(raw.getValue()));
		}

	}

}
	



