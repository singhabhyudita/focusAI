package com.codebee.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.codebee.myapplication.ml.MobilenetV110224Quantized1Default1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MotionSensor extends Thread implements SensorEventListener {

    private static final String TAG = "MotionSensor";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private final Context context;
    private volatile boolean stopThread = false;

    float[] xValuesLeft = new float[40];
    float[] xValuesRight = new float[40];
    float[] yValuesLeft = new float[40];
    float[] yValuesRight = new float[40];
    float[] zValuesLeft = new float[40];
    float[] zValuesRight = new float[40];

    private int counter = 0;
    long lastSaved = System.currentTimeMillis();

    public MotionSensor(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.d(TAG, "Initializing sensor services");
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void startSensor() {
        counter = 0;
        sensorManager.registerListener(MotionSensor.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "Registered accelerometer listener");
    }

    public void stopSensor() {
        sensorManager.unregisterListener(MotionSensor.this, accelerometer);
        Log.d(TAG, "Unregistered accelerometer listener");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int ACCELEROMETER_TIME_PERIOD = 50; // 20Hz frequency

        if ((System.currentTimeMillis() - lastSaved) > ACCELEROMETER_TIME_PERIOD) {
            lastSaved = System.currentTimeMillis();

            System.out.println("Reading...");

            if(counter == 40){
                counter = 0;
                System.arraycopy(xValuesRight, 0, xValuesLeft, 0, 40);
                System.arraycopy(yValuesRight, 0, yValuesLeft, 0, 40);
                System.arraycopy(zValuesRight, 0, zValuesLeft, 0, 40);
                System.out.println("Send data...");
            }

//            try {
//                MobilenetV110224Quantized1Default1 model = MobilenetV110224Quantized1Default1.newInstance(context);
//
//                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*3*80);
//
//                // Creates inputs for reference.
//                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
//                inputFeature0.loadBuffer(byteBuffer);
//
//                // Runs model inference and gets result.
//                MobilenetV110224Quantized1Default1.Outputs outputs = model.process(inputFeature0);
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//                // Releases model resources if no longer used.
//                model.close();
//            } catch (IOException e) {
//                // TODO Handle the exception
//            }


            xValuesRight[counter] = event.values[0];
            yValuesRight[counter] = event.values[1];
            zValuesRight[counter] = event.values[2];
            counter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
