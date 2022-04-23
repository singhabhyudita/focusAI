package com.codebee.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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
        sentCount = 0;
        firstData = true;
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
                for(int i = 0; i < 40; i++)
                {
                    valuesLeft[i][0] = valuesRight[i][0];
                    valuesLeft[i][1] = valuesRight[i][1];
                    valuesLeft[i][2] = valuesRight[i][2];
                }

                if(!firstData) {
                    System.out.println("Send data...");

                    // TODO: Get class from model
                    sentCount++;
                    if(sentCount == 3){
                        stopSensor();

                        double pSafe = Math.random();
                        double pDrunk = Math.random();
                        double pUnsafe = 1 - pSafe;
                        if(pDrunk > 0.7 || pUnsafe > 0.6) {
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
                        }

                        long t = (long) Math.ceil(1000 * 60 * 2 * pSafe);
                        try {
                            System.out.println("Sleeping for " + t + " ms");
                            Thread.sleep(t);
                            startSensor();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                else
                    firstData = false;
            }

            valuesRight[counter][0] = event.values[0];
            valuesRight[counter][1] = event.values[1];
            valuesRight[counter][1] = event.values[2];
            counter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
