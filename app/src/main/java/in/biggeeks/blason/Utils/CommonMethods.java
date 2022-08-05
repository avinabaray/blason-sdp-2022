package in.biggeeks.blason.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.biggeeks.blason.BroadcastReceivers.SmsDeliveredReceiver;
import in.biggeeks.blason.BroadcastReceivers.SmsSentReceiver;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.AlertModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;

import static android.content.Context.LOCATION_SERVICE;

public class CommonMethods {

    private ProgressDialog pd;

    /**
     * Capitalizes first letter of each word and rest small letters
     */
    public static String normalizeNameString(String str) {
        try {
            char[] ch = str.toCharArray();
            for (int i = 0; i < str.length(); i++) {

                if (i == 0 && ch[i] != ' ' || ch[i] != ' ' && ch[i - 1] == ' ') {
                    if (ch[i] >= 'a' && ch[i] <= 'z') {
                        ch[i] = (char) (ch[i] - 'a' + 'A');
                    }
                } else if (ch[i] >= 'A' && ch[i] <= 'Z')
                    ch[i] = (char) (ch[i] + 'a' - 'A');
            }
            return new String(ch);
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            return str;
        }
    }

    public static void openUrlInBrowser(Activity mActivity, String url) throws Exception {
        Intent browserIntent = null;
        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mActivity.startActivity(browserIntent);
//        Toast.makeText(mActivity, "Opening Link...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens Google Maps app with a marker
     *
     * @param rootLayout if {@code null} is passed, error will be shown in {@link Toast}, else in {@link Snackbar}
     * @param label      to show Label against the Marker in Google Maps app
     */
    public static void openMap(Activity mActivity, View rootLayout, double latitude, double longitude, String label) {
        Uri gmmIntentUri = Uri.parse("geo:" + "0,0" + "?" +
                // Query coordinates to put Marker
                "q=" + latitude + "," + longitude +
                // Label text to show on Google Maps
                "(" + label + ")"
        );
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            mActivity.startActivity(mapIntent);
        } else {
            String mapWebUrl = "https://www.google.com/maps/place/" + latitude + "," + longitude;
            try {
                openUrlInBrowser(mActivity, mapWebUrl);
                Toast.makeText(mActivity, "Install Google Maps for a better experience", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
                if (rootLayout != null)
                    new CommonMethods().makeSnack(rootLayout, "Install Google Maps");
                else
                    Toast.makeText(mActivity, "Install Google Maps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void updateFcmTokenInDB(String fcmToken) {
        MainActivity.CURR_USER_MODEL.setFcmToken(fcmToken);

        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .document(MainActivity.CURR_USER_MODEL.getId())
                .set(MainActivity.CURR_USER_MODEL)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Nothing to do
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    public void createAlert(AlertDialog.Builder alertDialogBuilder, String msg) {
        alertDialogBuilder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void makeSnack(View rootLayout, String msg) {
        final Snackbar snackbar = Snackbar
                .make(rootLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        }).setActionTextColor(Color.parseColor("#FFFFFF"));
    }

    public void loadingDialogStart(Context context) {
        pd = new ProgressDialog(context);
        pd.setMessage("Please wait...");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
    }

    /**
     * This is the docs for the user defined method by Avinaba Ray
     *
     * @param context pass the Context or Activity reference
     * @param message pass the message to be displayed in the dialog
     */
    public void loadingDialogStart(Context context, String message) {
        pd = new ProgressDialog(context);
        pd.setMessage(message);
        pd.show();
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
    }


    public void loadingDialogStop() {
        pd.dismiss();
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * Utility method to convert dp -> pixel
     *
     * @param dp pass dp to be converted
     * @return returns passes <code>dp</code> into <code>pixels</code>
     */
    public static int convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static String formatPhone(String phoneNo) {
        return phoneNo.substring(0, 3) + " " +
                phoneNo.substring(3, 8) + " " +
                phoneNo.substring(8, phoneNo.length());
    }

    public static void call(Activity mActivity, String phoneNo) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNo.trim()));
        mActivity.startActivity(callIntent);
    }

    /**
     * Triggers an Alert in the Server and Notifies all the NearOnes
     *
     * @param rootLayout   pass {@code null} in triggered from background
     * @param onBackground true if called from background like from a {@link Service}
     * @param triggerText  Specify a message or {@code null} to set the default "I'm in Danger"
     * @param alertLevel   pass the {@link AlertModel.AlertLevel} or {@code null} to set {@code AlertLevel.EXTREME}
     */
    public static void triggerSOS(final Context context,
                                  @Nullable final View rootLayout,
                                  final boolean onBackground,
                                  @Nullable String triggerText,
                                  @Nullable AlertModel.AlertLevel alertLevel) {

        final AlertModel alertModel = new AlertModel();
        final DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(Constants.FB_ALERTS_COL)
                .document();
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria locCriteria = new Criteria();

        triggerText = triggerText == null ? "I'm in Danger" : triggerText;
        alertLevel = alertLevel == null ? AlertModel.AlertLevel.EXTREME : alertLevel;

        // Defining Location Criteria that needs to be fetched from the System
        locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        locCriteria.setPowerRequirement(Criteria.POWER_HIGH);
        locCriteria.setAltitudeRequired(false);
        locCriteria.setBearingRequired(false);
        locCriteria.setSpeedRequired(false);
        locCriteria.setCostAllowed(true);
        locCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        locCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        alertModel.setId(docRef.getId());
        alertModel.setAlertStatus(AlertModel.AlertStatus.ACTIVE);
        alertModel.setAlertLevel(AlertModel.AlertLevel.EXTREME);
        alertModel.setTriggerText("I'm in Danger");
        alertModel.setTriggerTime(Timestamp.now());
        alertModel.setVictimID(MainActivity.CURR_USER_MODEL.getId());


        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.w("LOCATION", location.toString());
                Log.wtf("LOCATION", "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());

                // setting current Location of the victim in the AlertModel
                alertModel.setVictimLoc(new GeoPoint(location.getLatitude(), location.getLongitude()));
                insertAlertInFirebase(context, docRef, alertModel, rootLayout, onBackground);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestSingleUpdate(locCriteria, locationListener, null);
        } else {
            Log.wtf("LOC_PERMISSION", "Denied");
            Toast.makeText(context, "LOC_PERMISSION Denied", Toast.LENGTH_SHORT).show();
        }

    }

    private static void insertAlertInFirebase(final Context context,
                                              final DocumentReference docRef, final AlertModel alertModel,
                                              @Nullable final View rootLayout,
                                              final boolean onBackground) {
        final ArrayList<String> receiverIDs = new ArrayList<>();
        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .document(MainActivity.CURR_USER_MODEL.getId())
                .collection(Constants.FB_NEAR_ONES_COL)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot docSnap : queryDocumentSnapshots) {
                            receiverIDs.add(docSnap.getId());
                        }
                        alertModel.setReceiverIDs(receiverIDs);
                        Log.wtf("INTERNET", String.valueOf(isNetworkAvailable(context)));
//                        if (!isNetworkAvailable(context)) {
//                            sendOfflineAlert(context, alertModel);
//                        }

                        // Delaying teh SMS alert for 10 secs so that Push Notif may complete
                        new CountDownTimer(10000, 9000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                Log.wtf("SMS Alert", "Initiated");
                                sendOfflineAlert(context, alertModel);
                            }
                        }.start();

                        docRef.set(alertModel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (!onBackground && rootLayout != null) {
                                            new CommonMethods().makeSnack(rootLayout, "All Near-Ones notified!");
                                        }
                                        // Alert all near ones through notification here
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    /**
     * Formats the Firebase Timestamp object in a readable format
     *
     * @param timestamp Firebase Timestamp Object
     * @return String in DD-MM-YYYY • HH:MM(AM/PM) format
     */
    public static String getFormattedDateTime(Timestamp timestamp) {
        if (timestamp != null) {
            String dateTimeString = "";
            String timeSuffix;
            if (timestamp.toDate().getDate() < 10) {
                dateTimeString += "0" + timestamp.toDate().getDate() + "-";
            } else {
                dateTimeString += timestamp.toDate().getDate() + "-";
            }
            if (timestamp.toDate().getMonth() < 10) {
                dateTimeString += "0" + (timestamp.toDate().getMonth() + 1) + "-";
            } else {
                dateTimeString += (timestamp.toDate().getMonth() + 1) + "-";
            }
            dateTimeString += String.valueOf(timestamp.toDate().getYear() + 1900 - 2000) + " • ";
            if (timestamp.toDate().getHours() > 12) {
                dateTimeString += timestamp.toDate().getHours() - 12 + ":";
                timeSuffix = "PM";
            } else {
                dateTimeString += timestamp.toDate().getHours() + ":";
                timeSuffix = "AM";
            }
            if (timestamp.toDate().getMinutes() < 10) {
                dateTimeString += "0" + timestamp.toDate().getMinutes();
            } else {
                dateTimeString += timestamp.toDate().getMinutes();
            }
            dateTimeString += timeSuffix;

            return dateTimeString;
        } else {
            return "null";
        }
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }

    public static void gpsStatusCheck(final Activity mActivity) {
        LocationManager manager = (LocationManager) mActivity.getSystemService(LOCATION_SERVICE);
        boolean gpsOn = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsOn) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
            alertDialogBuilder.setTitle("Enable GPS")
                    .setMessage("Please enable Location Services for the app to work as intended. " +
                            "Otherwise, some features may not be available.")
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_location)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mActivity.startActivity(intent);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            positiveButtonLL.leftMargin = CommonMethods.convertDpToPixel(5);
            positiveButton.setLayoutParams(positiveButtonLL);
        }
    }

    public static void sendSMS(Context context, String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsSentReceiver.class), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(context, SmsDeliveredReceiver.class), 0);

        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> mSMSMessage = sms.divideMessage(message);
            for (int i = 0; i < mSMSMessage.size(); i++) {
                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);

        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(context, "SMS sending failed...", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public static boolean isNetworkOnline1(Context context) {
        boolean isOnline = false;
        try {
            ConnectivityManager manager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());  // need ACCESS_NETWORK_STATE permission
            isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOnline;
    }


    private static void sendOfflineAlert(final Context context, final AlertModel alertModel) {

        final ArrayList<String> receiverPhones = new ArrayList<>();
        final ArrayList<String> receiverNames = new ArrayList<>();

        if (alertModel.getReceiverIDs().isEmpty()) {
            Toast.makeText(context, "No " + context.getString(R.string.near_one) + "s added yet...", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .whereIn("id", alertModel.getReceiverIDs())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                            UserModel um = snap.toObject(UserModel.class);
                            receiverPhones.add(um.getPhone());
                            receiverNames.add(um.getName());
                        }

                        //todo uncomment it
                        for (int i = 0; i < receiverPhones.size(); i++) {
                            sendSMS(context, receiverPhones.get(i),
                                    buildAlertSMS(i, alertModel, receiverNames, receiverPhones));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });

    }

    /**
     * As per https://developers.google.com/maps/documentation/urls/get-started
     */
    public static String getGMapsLocUrl(String lat, String lon) {
        return "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private static String buildAlertSMS(int currRecvPos, AlertModel alert,
                                        ArrayList<String> allRecvNames, ArrayList<String> allRecvPhones) {

        StringBuilder msg = new StringBuilder();

        msg.append("!!! BLASON ALERT !!!\n\n");
        msg.append(allRecvNames.get(currRecvPos).toUpperCase() + ",\n" + alert.getTriggerText() + "\n\n");
//        msg.append(alert.getAlertStatus().toString() + " alert\n");
        msg.append("Danger level: " + alert.getAlertLevel().toString() + "\n\n");
        msg.append("TIME: " + getFormattedDateTime(alert.getTriggerTime()) + "\n");
        msg.append("CALL ME: " + MainActivity.CURR_USER_MODEL.getPhone() + "\n\n");
        msg.append("LOCATION:\n" + getGMapsLocUrl(
                String.valueOf(alert.getVictimLoc().getLatitude()),
                String.valueOf(alert.getVictimLoc().getLongitude())) + "\n\n");

        if (allRecvNames.size() > 1)
            msg.append("Others Notified:\n");

        for (int i = 0; i < allRecvNames.size(); i++) {
            // to skip the receiver's name in "Others Notified"
            if (i == currRecvPos)
                continue;
            msg.append(allRecvNames.get(i) + "\n");
            msg.append(allRecvPhones.get(i) + "\n\n");
        }

        msg.append("- " + MainActivity.CURR_USER_MODEL.getName().toUpperCase());

        return msg.toString();
    }

}
