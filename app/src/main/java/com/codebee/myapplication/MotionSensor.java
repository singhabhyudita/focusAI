package com.codebee.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MotionSensor extends Thread implements SensorEventListener {

    private static final String TAG = "MotionSensor";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private final Context context;
    private volatile boolean stopThread = false;

    private boolean firstData = true;
    float[][] valuesLeft = new float[40][3];
    float[][] valuesRight = new float[40][3];

    private int counter = 0;
    private int sentCount = 0;
    long lastSaved = System.currentTimeMillis();
    private Interpreter interpreter;

    double pDistracted = 0.0;
    double pDrunk = 0.0;
    double pNormal = 0.0;

    public MotionSensor(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.d(TAG, "Initializing sensor services");
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSensor() {
        counter = 0;
        sentCount = 0;
        firstData = true;
        sensorManager.registerListener(MotionSensor.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d(TAG, "Registered accelerometer listener");
    }

    public void stopSensor() {
        sensorManager.unregisterListener(MotionSensor.this, accelerometer);
        Log.d(TAG, "Unregistered accelerometer listener");
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("takia.tflite");
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffsets = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffsets, declaredLength);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int ACCELEROMETER_TIME_PERIOD = 50; // 20Hz frequency

        if ((System.currentTimeMillis() - lastSaved) > ACCELEROMETER_TIME_PERIOD) {
            lastSaved = System.currentTimeMillis();

            if(counter == 40){
                counter = 0;
                if(!firstData) {

                    float[][][][] input = new float[1][80][3][1];
                    int k = 0;
                    for(int i = 0 ; i < 40; i++) {
                        for (int j = 0; j < 3; j++)
                            input[0][k][j][0] = valuesLeft[i][j];
                        k++;
                    }
                    for(int i = 0 ; i < 40; i++) {
                        for (int j = 0; j < 3; j++)
                            input[0][k][j][0] = valuesRight[i][j];
                        k++;
                    }
                    float[][] output = new float[1][3];
                    interpreter.run(input, output);

                    if(output[0][0] > output[0][1] && output[0][0] > output[0][2]) {
                        System.out.println("Distracted");
                        pDistracted++;
                    }
                    else if(output[0][1] > output[0][0] && output[0][1] > output[0][2]) {
                        System.out.println("Drunk");
                        pDrunk++;
                    }
                    else {
                        System.out.println("Normal");
                        pNormal++;
                    }

                    for(int i = 0; i < 40; i++)
                    {
                        valuesLeft[i][0] = valuesRight[i][0];
                        valuesLeft[i][1] = valuesRight[i][1];
                        valuesLeft[i][2] = valuesRight[i][2];
                    }

                    // TODO: Get class from model
                    sentCount++;
                    if(sentCount == 10){

                        pDrunk /= 10;
                        pDistracted /= 10;
                        pNormal /= 10;

                        if(pDrunk > 0.7 || pNormal < 0.4) {
                            final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                            final VibrationEffect vibrationEffect1;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrationEffect1 = VibrationEffect.createOneShot(5000, VibrationEffect.EFFECT_HEAVY_CLICK);
                                vibrator.cancel();
                                vibrator.vibrate(vibrationEffect1);
                            }

                            String channelId = "motion_sensor_notification_channel";
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Intent resultIntent = new Intent();
                            PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), channelId);
                            builder.setSmallIcon(R.mipmap.ic_launcher);
                            builder.setContentTitle("FocusAI");
                            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
                            builder.setContentText("Safety Alert!");
                            builder.setAutoCancel(false);
                            builder.setPriority(NotificationCompat.PRIORITY_MAX);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                                    NotificationChannel notificationChannel = new NotificationChannel(
                                            channelId,
                                            "Motion Sensor Service",
                                            NotificationManager.IMPORTANCE_HIGH
                                    );
                                    notificationChannel.setDescription("This channel is used by motion sensor service.");
                                    notificationManager.createNotificationChannel(notificationChannel);
                                }
                            }

                            notificationManager.notify(1, builder.build());

                            pDistracted = pDrunk = pNormal = 0.0;
                        }

                        sentCount = 0;
                    }
                }
                else
                    firstData = false;
            }

            valuesRight[counter][0] = event.values[0];
            valuesRight[counter][1] = event.values[1];
            valuesRight[counter][2] = event.values[2];
            counter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
