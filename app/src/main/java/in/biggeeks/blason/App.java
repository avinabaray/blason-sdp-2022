package in.biggeeks.blason;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_ID = "blasonLiveChannel";
    public static final String ALERT_NOTIF_CH_ID = "alertNotifChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifManager.createNotificationChannel(serviceChannel);

            Uri sirenSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notif_siren_sound);
            NotificationChannel alertChannel = new NotificationChannel(
                    ALERT_NOTIF_CH_ID,
                    "FCM Alert Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            alertChannel.enableVibration(true);
            alertChannel.setVibrationPattern(new long[]{0, 4000, 200, 4000, 200, 4000});
            alertChannel.setSound(sirenSoundUri, att);
            notifManager.createNotificationChannel(alertChannel);


        }
    }
}
