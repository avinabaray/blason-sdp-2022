package in.biggeeks.blason.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import in.biggeeks.blason.FeatureActivities.AlertsTabActivity;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.TensorflowLite.Classifier;
import in.biggeeks.blason.TensorflowLite.TensorflowClassifier;

import static in.biggeeks.blason.App.CHANNEL_ID;

public class BlasonLiveForegroundService extends Service implements SensorEventListener {

    private static final int LIVE_NOTIF = 124;
    private static final String TAG = "BlasonLiveFgService";
    private Service mService;
    private Notification notification;
    private LocationManager locationManager;
    private LocationListener locationListener;

    // ------------ Voice ---------------

    private SpeechRecognizer speechRecognizer;


    // ------------ ML START ------------

    private boolean keepPredictionThreadRunning = true;
    //    private float[][] result;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor;
    String[] label = {"Falling", "Walking", "Jogging", "Jumping", "up-stair", "down-stair", "stand2sit", "sit2stand"};

    private TensorflowClassifier tensorflowClassifier = new TensorflowClassifier();
    private int cntAccx = 0, cntAccy = 0, cntGyrx = 10, cntGyry = 0;
    private float[][][][] data = new float[1][20][20][3];

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final String MODEL_PATH = "model_v2_0.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int[] INPUT_SIZE = {1, 20, 20, 3};

    private float Accx, Accy, Accz, Gyrox, Gyroy, Gyroz;
    private Thread predictionThread;
    private NotificationManager notificationManager;

    private float[] lastAccData = new float[]{Integer.MAX_VALUE, 0.0f, 0.0f};
    private long lastAccTime;
    private float[] lastGyroData = new float[]{Integer.MAX_VALUE, 0.0f, 0.0f};
    private long lastGyroTime;
    private Intent speechIntent;
    private boolean allowTriggering = true;
    private PendingIntent pendingIntentToAlertsTab;

    // ------------ ML END ------------

    @Override
    public void onCreate() {
        super.onCreate();
        mService = this;
        Log.wtf(TAG, "onCreate: CREATED");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initializes the TF Lite model
        initAndLoadModel();

        if (checkSensorAvailability(Sensor.TYPE_ACCELEROMETER)) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            accelerometerSensor = null;
            Log.wtf("SENSOR_ERROR", "TYPE_ACCELEROMETER not found in device");
        }

        if (checkSensorAvailability(Sensor.TYPE_GYROSCOPE)) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        } else {
            gyroscopeSensor = null;
            Log.wtf("SENSOR_ERROR", "TYPE_GYROSCOPE not found in device");
        }

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        initVoiceRecognition();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf(TAG, "onStartCommand: STARTED");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mService,
                102,
                new Intent(mService, MainActivity.class),
                0
        );

        pendingIntentToAlertsTab = PendingIntent.getActivity(
                mService,
                105,
                new Intent(mService, AlertsTabActivity.class),
                0
        );

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blason Live")
                .setContentText("You are safe with us...")
                .setSmallIcon(R.drawable.ic_foreground_notif)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(mService, R.color.dark_green))
                .build();

        startForeground(LIVE_NOTIF, notification);

//        processFallingAlert();

//        new CountDownTimer(5000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                CommonMethods.triggerSOS(mService, null, true, null, null);
//            }
//        }.start();

        return START_NOT_STICKY;
    }

    public boolean checkSensorAvailability(int SensorType) {
        boolean isSensor = false;
        Log.d("Sensors Availability: ",
                "Check Sensor Availability: " + (sensorManager.getDefaultSensor(SensorType) != null));
        if (sensorManager.getDefaultSensor(SensorType) != null) {
            isSensor = true;
        }
        return isSensor;
    }

    private void initAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = tensorflowClassifier.create(getAssets(), MODEL_PATH, LABEL_PATH, INPUT_SIZE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.wtf(TAG, "onDestroy: DESTROYED");
        keepPredictionThreadRunning = false;
        sensorManager.unregisterListener(this);
        speechRecognizer.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                lastAccData[0] = modulateAccData(event.values[0]);
                lastAccData[1] = modulateAccData(event.values[1]);
                lastAccData[2] = modulateAccData(event.values[2]);

                lastAccTime = event.timestamp;
                newDataReceived();

                break;
            case Sensor.TYPE_GYROSCOPE:

                lastGyroData[0] = modulateGyroData(event.values[0]);
                lastGyroData[1] = modulateGyroData(event.values[1]);
                lastGyroData[2] = modulateGyroData(event.values[2]);

                lastGyroTime = event.timestamp;
                newDataReceived();

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void initVoiceRecognition() {
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "onReadyForSpeech: ");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech: ");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d(TAG, "onRmsChanged: ");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "onBufferReceived: ");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech: ");
            }

            @Override
            public void onError(int error) {
                Log.d(TAG, "onError: " + error);
                speechRecognizer.startListening(speechIntent);
            }

            @Override
            public void onResults(Bundle results) {
                Log.d(TAG, "onResults: ");
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    Log.wtf("RESULTS", Arrays.toString(matches.toArray()) + ": " + matches.size());
//                    Toast.makeText(mService, Arrays.toString(matches.toArray()), Toast.LENGTH_SHORT).show();
//                    txtSpeechInput.setText(Arrays.toString(matches.toArray()));

                    String speechStr = matches.get(0);

                    if (speechStr.contains("bachao") ||
                            speechStr.contains("help me")) {
                        processFallingAlert();
                        Notification notification = new NotificationCompat.Builder(mService, CHANNEL_ID)
//                    .setContentTitle("|| " + label[labelPos] + "  detected ||")
                                .setContentTitle("ALERT Keyword detected!!!")
//                    .setContentText("MaxProbability Value: " + maxProb)
                                .setContentText(getString(R.string.near_one) + "s Notified... Tap to View")
                                .setContentIntent(pendingIntentToAlertsTab)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_security)
                                .setColor(ContextCompat.getColor(mService, R.color.dark_red))
                                .build();
                        notificationManager.notify(((int) System.currentTimeMillis()), notification);
                    }

//                    StringTokenizer st = new StringTokenizer(speechStr);
//                    while (st.hasMoreTokens()) {
//                        String word = st.nextToken();
//                    }

                } else {
                    Log.wtf("RESULTS", "Empty");
                }
                speechRecognizer.startListening(speechIntent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults: ");
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                float[] scores = partialResults.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                if (matches != null) {
                    Log.wtf("RESULTS", Arrays.toString(matches.toArray()));
                } else {
                    Log.wtf("RESULTS", "Empty");
                }

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "onEvent: ");
            }
        });

        speechRecognizer.startListening(speechIntent);
    }

    private void processFallingAlert() {

        if (allowTriggering) {
            CommonMethods.triggerSOS(mService, null, true, null, null);
            allowTriggering = false;
            allowTriggeringAfterSomeTime();
        }
    }

    private void allowTriggeringAfterSomeTime() {
        new CountDownTimer(30000, 29000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            @Override
            public void onFinish() {
                allowTriggering = true;
            }
        }.start();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private float modulateAccData(float rawData) {

        float temp = (int) (255 * (rawData + 16)) / 32.0f;
        float ans = (float) (temp / 127.5) - 1;

        return ans;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private float modulateGyroData(float rawData) {

        float temp = (255 * (rawData + 34)) / 68.0f;
        float ans = (float) (temp / 127.5) - 1;

        return ans;
    }

    private int x = 0;
    private int y = 0;

    private void newDataReceived() {
//        Log.w("DATA", "A: " + lastAccTime + ", G: " + lastGyroTime + ", Diff: " + (lastAccTime - lastGyroTime));
        if (lastAccData[0] != Integer.MAX_VALUE && lastGyroData[0] != Integer.MAX_VALUE) {
            if (Math.abs(lastAccTime - lastGyroTime) < 60000) {
//                Log.wtf("DATA", "A: " + lastAccTime + ", G: " + lastGyroTime + ", Diff: " + (lastAccTime - lastGyroTime));

                int currRow = (x++) / 20;
                int currCol = (y++) % 20;

                if (currRow == 10) {
                    // Do prediction here
                    predictMotionAndToast();
                    Log.w("PREDICTION", "Data filled x:" + x + ", " + y);
                    x = 0;
                    y = 0;
                    return;
                }

                data[0][currRow][currCol][0] = lastAccData[0];
                data[0][currRow][currCol][1] = lastAccData[1];
                data[0][currRow][currCol][2] = lastAccData[2];

                data[0][currRow + 10][currCol][0] = lastGyroData[0];
                data[0][currRow + 10][currCol][1] = lastGyroData[1];
                data[0][currRow + 10][currCol][2] = lastGyroData[2];

                lastAccData[0] = Integer.MAX_VALUE;
                lastGyroData[0] = Integer.MAX_VALUE;
            }
        }
    }

    private void predictMotionAndToast() {
        int labelPos = 0;
        float maxProb = 0.0f;
        float[][] result = tensorflowClassifier.Prediction(data);

        for (int i = 0; i < result[0].length; i++) {
            if (maxProb < result[0][i]) {
                maxProb = result[0][i];
                labelPos = i;
            }
        }

        Toast.makeText(mService, label[labelPos], Toast.LENGTH_SHORT).show();

        if (label[labelPos].equals("Falling")) {

            processFallingAlert();



            Notification notification = new NotificationCompat.Builder(mService, CHANNEL_ID)
//                    .setContentTitle("|| " + label[labelPos] + "  detected ||")
                    .setContentTitle(label[labelPos] + " detected!!!")
//                    .setContentText("MaxProbability Value: " + maxProb)
                    .setContentText(getString(R.string.near_one) + "s Notified... Tap to View")
                    .setContentIntent(pendingIntentToAlertsTab)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_security)
                    .setColor(ContextCompat.getColor(mService, R.color.dark_red))
                    .build();
            notificationManager.notify(((int) System.currentTimeMillis()), notification);

            Log.wtf("LABEL pos", label[labelPos]);
            Log.wtf("Display Result:", "++++++++++++++++++++++++ " + maxProb + label[labelPos] + " +++++++++++++++++++++++++");

        } else {
            Log.wtf("LABEL pos", label[labelPos]);
            Log.d("Display Result:", "++++++++++++++++++++++++ " + maxProb + label[labelPos] + " +++++++++++++++++++++++++");
        }
    }
}
