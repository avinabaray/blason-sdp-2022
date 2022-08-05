package in.biggeeks.blason;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import in.biggeeks.blason.FeatureActivities.OnBoardingActivity;
import in.biggeeks.blason.Fragments.AboutUsFragment;
import in.biggeeks.blason.Fragments.ProfileFragment;
import in.biggeeks.blason.Fragments.UserDashFragment;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int GET_ALL_PERMS = 183;
    private View rootLayout;
    Activity mActivity = this;
    CommonMethods commonMethods = new CommonMethods();
    private Toolbar toolbar;
    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String shareURL = null;
    private String shareUrlPreText = "Oops...";
    private Menu defaultMenu;
    private long backPressedTime;
    private Toast backToast;
    private ArrayList<String> reqPermissions = new ArrayList<>();
    private ArrayList<String> permsToGet = new ArrayList<>();
    //    private boolean onDashFragment = true;
    private ActionBarDrawerToggle toggle;


    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            super.onBackPressed();
//            toggle.syncState();
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        reqPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        reqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        reqPermissions.add(Manifest.permission.SEND_SMS);
        reqPermissions.add(Manifest.permission.READ_PHONE_STATE);
        reqPermissions.add(Manifest.permission.RECORD_AUDIO);

        checkPermissions();


        uploadFcmToken();
        CommonMethods.gpsStatusCheck(mActivity);
        initViews();
        initNavbar();
        initFragment();
        loadFirebaseConstants();

    }

    private void checkPermissions() {
        for (String perm : reqPermissions) {
            if (ActivityCompat.checkSelfPermission(mActivity, perm) == PackageManager.PERMISSION_DENIED)
                permsToGet.add(perm);
        }

        if (permsToGet.size() != 0) {
            String[] strArray = new String[permsToGet.size()];
            strArray = permsToGet.toArray(strArray);
            ActivityCompat.requestPermissions(mActivity, strArray, GET_ALL_PERMS);
        }
    }

    private void uploadFcmToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();

                            Log.wtf("TOKEN", "Refreshed token in Dash: " + token);
                            CommonMethods.updateFcmTokenInDB(token);
                        }
                    }
                });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);
        rootLayout = drawer_layout;
        nav_view = findViewById(R.id.nav_view);
    }

    private void initNavbar() {
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.dashboard));
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(
                mActivity,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void initFragment() {
        getSupportFragmentManager()
                .beginTransaction()
//                .addToBackStack(null)
                .replace(R.id.fragment_container,
                        UserDashFragment.newInstance(mActivity, rootLayout),
                        "UserDashFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        nav_view.setCheckedItem(R.id.nav_user_dash);

    }

    private void loadFirebaseConstants() {
        FirebaseFirestore.getInstance()
                .collection("constants")
                .document("fields")
                .addSnapshotListener(mActivity, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            shareURL = documentSnapshot.getString("shareURL");
                            shareUrlPreText = documentSnapshot.getString("shareUrlPreText")
                                    .replace("\\n", "\n");
                        } else {
                            shareURL = null;
                        }
                    }
                });
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        String latestFragmentTag = getSupportFragmentManager()
                .getFragments()
                .get(getSupportFragmentManager().getFragments().size() - 1)
                .getTag();
        Log.d("DashAct", "onNavigationItemSelected: latestFragmentTag is " + latestFragmentTag);

        switch (item.getItemId()) {
            case R.id.nav_user_dash:
                if ((latestFragmentTag == null || !latestFragmentTag.equals(UserDashFragment.TAG))) {
                    getSupportFragmentManager()
                            .beginTransaction()
//                            .addToBackStack(UserDashFragment.TAG)
                            .replace(R.id.fragment_container,
                                    UserDashFragment.newInstance(mActivity, rootLayout),
                                    UserDashFragment.TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .commit();
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                    onDashFragment = true;
                }
                break;
            case R.id.nav_profile:
                if (latestFragmentTag == null || !latestFragmentTag.equals(ProfileFragment.TAG)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(ProfileFragment.TAG)
//                            .setCustomAnimations(
//                                    R.anim.enter_from_right, R.anim.exit_to_right)
                            .replace(R.id.fragment_container,
                                    ProfileFragment.newInstance(mActivity),
                                    ProfileFragment.TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .commit();
//                    onDashFragment = false;
                }
                break;
            case R.id.nav_about_us:
                if (latestFragmentTag == null || !latestFragmentTag.equals(AboutUsFragment.TAG)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
//                            .setCustomAnimations(
//                                    R.anim.enter_from_right, R.anim.exit_to_right)
                            .replace(R.id.fragment_container,
                                    AboutUsFragment.newInstance(mActivity),
                                    AboutUsFragment.TAG)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .commit();
//                    onDashFragment = false;
                }

                break;
            case R.id.nav_know_the_app:
                Intent i = new Intent(mActivity, OnBoardingActivity.class);
                startActivity(i);
                break;
            case R.id.nav_share:
                if (shareURL != null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrlPreText + shareURL);
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, getResources().getString(R.string.app_name));
                    startActivity(shareIntent);
                } else {
                    commonMethods.makeSnack(rootLayout, "Try again in some time...");
                }
                break;
            case R.id.nav_logout:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
                alertDialogBuilder.setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_logout)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                MainActivity.CURRENT_USER_ID = null;
//                                MainActivity.CURRENT_USER_ROLE = null;
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FB_USERS_COL)
                                        .document(MainActivity.CURR_USER_MODEL.getId())
                                        .update("fcmToken", null)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Do nothing
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                });

                                FirebaseAuth.getInstance().signOut();
                                MainActivity.currentUser = null;
                                editor.clear();
                                editor.commit();

                                Intent intentToMainActivity = new Intent(mActivity, MainActivity.class);
                                startActivity(intentToMainActivity);
                                finish();
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
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

                break;
        }


        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_ALL_PERMS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    checkPermissions();
                    break;
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            UserDashFragment.performMapJobs();
        }
    }
}
