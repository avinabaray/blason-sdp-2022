package in.biggeeks.blason.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;

import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;

import static in.biggeeks.blason.App.ALERT_NOTIF_CH_ID;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FBMessagingService";

    public FirebaseMessagingService() {

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();
            Log.d(TAG, "onMessageReceived: " + "TITLE: " + title + ", MSG: " + message);

            sendNotification(title, message);
        }

        if (remoteMessage.getData().size() > 0) {
            // Process the data here
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void sendNotification(String title, String msgBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri sirenSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notif_siren_sound);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ALERT_NOTIF_CH_ID)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setColor(getResources().getColor(R.color.reject_red))
                .setContentTitle(title)
                .setContentText(msgBody)
                .setAutoCancel(true)
                .setSound(sirenSoundUri)
                .setVibrate(new long[]{0, 4000, 200, 4000, 200, 4000})
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
//        super.onNewToken(token);
        Log.wtf("TOKEN", "Refreshed token in Service: " + token);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            MainActivity.CURR_USER_MODEL.setFcmToken(token);
            CommonMethods.updateFcmTokenInDB(token);
        }
    }
}
