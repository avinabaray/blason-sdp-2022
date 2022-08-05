package in.biggeeks.blason.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import in.biggeeks.blason.Dialogs.NotifyNearOnesDialog;
import in.biggeeks.blason.FeatureActivities.AlertsTabActivity;
import in.biggeeks.blason.FeatureActivities.MyNearOnesActivity;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Services.BlasonLiveForegroundService;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class UserDashFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    public static final String TAG = "UserDashFragment";
    public static final int LOC_REQ_CODE = 100;
    public static String policePhone = "100";
    private Activity mActivity;
    private View rootLayout;
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView userImage;
    private TextView userNameTextView;
    private MaterialButton myNearOnesBtn, myAlertsBtn, triggerAlertBtn;
    private CardView myNearOnesCard, allAlertsCard, triggerSOSCard, callPoliceCard;
    private CommonMethods commonMethods = new CommonMethods();

    private MapView userLocMapView;
    private ExtendedFloatingActionButton liveExtendedFab;
    public static GoogleMap map;
    private boolean isLiveOn = false;

    public UserDashFragment(Activity mActivity, View rootLayout) {
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
    }


    public static UserDashFragment newInstance(Activity mActivity, View rootLayout) {
        UserDashFragment fragment = new UserDashFragment(mActivity, rootLayout);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dash, container, false);

        initViews(view);
        setUserData();
        fetchConstants();

        // --------------------- Maps START ------------------------
        initGoogleMap(savedInstanceState);
        // --------------------- Maps END ------------------------

        return view;
    }

    private void initViews(View view) {
        userImage = view.findViewById(R.id.userImage);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        myNearOnesBtn = view.findViewById(R.id.myNearOnesBtn);
        myAlertsBtn = view.findViewById(R.id.myAlertsBtn);
        triggerAlertBtn = view.findViewById(R.id.triggerAlertBtn);
        userLocMapView = view.findViewById(R.id.userLocMapView);
        liveExtendedFab = view.findViewById(R.id.liveExtendedFab);

        myNearOnesCard = view.findViewById(R.id.myNearOnesCard);
        allAlertsCard = view.findViewById(R.id.allAlertsCard);
        triggerSOSCard = view.findViewById(R.id.triggerSOSCard);
        callPoliceCard = view.findViewById(R.id.callPoliceCard);

        myNearOnesBtn.setOnClickListener(this);
        myAlertsBtn.setOnClickListener(this);
        triggerAlertBtn.setOnClickListener(this);

        myNearOnesCard.setOnClickListener(this);
        allAlertsCard.setOnClickListener(this);
        triggerSOSCard.setOnClickListener(this);
        callPoliceCard.setOnClickListener(this);
        liveExtendedFab.setOnClickListener(this);

        initLiveStatus();
    }

    private void initLiveStatus() {
        isLiveOn = CommonMethods.isServiceRunningInForeground(mActivity, BlasonLiveForegroundService.class);
        Log.wtf(TAG, "init LIVE: " + String.valueOf(isLiveOn));
        setLiveButtonStatus(isLiveOn);
    }

    private void setLiveButtonStatus(boolean setOn) {
        if (setOn) {
            liveExtendedFab.setIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_stop_live));
            liveExtendedFab.setIconTint(ContextCompat.getColorStateList(mActivity, R.color.dark_red));
            liveExtendedFab.setText(getString(R.string.stop_live));
            liveExtendedFab.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.dark_red));
            liveExtendedFab.setBackgroundTintList(ContextCompat.getColorStateList(mActivity, R.color.light_pink));
            liveExtendedFab.setStrokeColorResource(R.color.dark_red);
        } else {
            liveExtendedFab.setIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_start_live));
            liveExtendedFab.setIconTint(ContextCompat.getColorStateList(mActivity, R.color.dark_green2));
            liveExtendedFab.setText(getString(R.string.start_live));
            liveExtendedFab.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.dark_green2));
            liveExtendedFab.setBackgroundTintList(ContextCompat.getColorStateList(mActivity, R.color.light_green));
            liveExtendedFab.setStrokeColorResource(R.color.dark_green2);
        }
    }

    private void startLiveService(boolean start) {
        if (start) {
            CommonMethods.gpsStatusCheck(mActivity);
            Intent startLiveIntent = new Intent(mActivity, BlasonLiveForegroundService.class);
            ContextCompat.startForegroundService(mActivity, startLiveIntent);
        } else {
            Intent stopLiveIntent = new Intent(mActivity, BlasonLiveForegroundService.class);
            mActivity.stopService(stopLiveIntent);
        }
        Log.wtf(TAG, "SERVICE triggered: " + CommonMethods.isServiceRunningInForeground(mActivity, BlasonLiveForegroundService.class));
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY);
        }
        userLocMapView.onCreate(mapViewBundle);
        userLocMapView.getMapAsync(this);
    }

    @SuppressLint("SetTextI18n")
    private void setUserData() {
        userNameTextView.setText(getString(R.string.greetings) + ", " +
                CommonMethods.normalizeNameString(MainActivity.CURR_USER_MODEL.getName()));
        Glide.with(mActivity)
                .asBitmap()
                .load(MainActivity.CURR_USER_MODEL.getPhoto().trim())
                .into(userImage);

    }

    private void fetchConstants() {
        FirebaseFirestore.getInstance()
                .collection("constants")
                .document("emergency")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myNearOnesCard:
                Intent i1 = new Intent(mActivity, MyNearOnesActivity.class);
                startActivity(i1);
                break;
            case R.id.allAlertsCard:
//                Intent i2 = new Intent(mActivity, AllAlertsActivity.class);
                Intent i2 = new Intent(mActivity, AlertsTabActivity.class);
                startActivity(i2);
                break;
            case R.id.triggerSOSCard:
                NotifyNearOnesDialog notifyDialog = new NotifyNearOnesDialog(mActivity, rootLayout);
                notifyDialog.show();
                break;
            case R.id.callPoliceCard:
                CommonMethods.call(mActivity, "100");
                break;
            case R.id.liveExtendedFab:
                isLiveOn = !isLiveOn;
                setLiveButtonStatus(isLiveOn);
                startLiveService(isLiveOn);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
                alertDialogBuilder.setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                if (isLiveOn) {
                    alertDialogBuilder.setTitle("Start Blason LIVE")
                            .setIcon(R.drawable.ic_notifications_active)
                            .setMessage("Now, all your physical activities will be detected precisely by our app to detect " +
                                    "any Dangerous situations for you. All your added " + getString(R.string.near_one) + "s " +
                                    "will be notified accordingly");
                } else {
                    alertDialogBuilder.setTitle("Stop Blason LIVE")
                            .setIcon(R.drawable.ic_notifications_off)
                            .setMessage("All your physical activities detection has been stopped");
                }
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        userLocMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        userLocMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        userLocMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        UserDashFragment.map = map;
//        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            performMapJobs();
        }
    }

    @SuppressLint("MissingPermission")
    public static void performMapJobs() {
        map.setMyLocationEnabled(true);
        centerMapOnMyLocation();
    }

    private static void centerMapOnMyLocation() {
//        Location location = map.getMyLocation();
//        LatLng myLocation;
//        if (location != null) {
//            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
//        } else {
//            Log.wtf("Map_ERR", "Cant load");
//        }
        LatLng myLocation = new LatLng(26.0, 82.0);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 4.0f));

    }

    @Override
    public void onPause() {
        userLocMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        userLocMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        userLocMapView.onLowMemory();
    }

//    @SuppressLint("MissingPermission")

}
