package in.biggeeks.blason;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import in.biggeeks.blason.FeatureActivities.OnBoardingActivity;
import in.biggeeks.blason.FeatureActivities.RegisterStage2Activity;
import in.biggeeks.blason.FeatureActivities.RegisterUserActivity;
import in.biggeeks.blason.Models.UserModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
//    private CollectionReference mDocRef = FirebaseFirestore.getInstance().collection("users");

    // public static variables accessible throughout the app
    public static String CURRENT_USER_ID;
    public static String CURRENT_PHONE;
    public static Boolean details1Uploaded = false;
    public static Boolean photo2Uploaded = false;
    public static UserModel CURR_USER_MODEL = new UserModel();

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    public static FirebaseUser currentUser;
    private Activity mActivity;

    @Override
    protected void onStart() {
        super.onStart();
        CURRENT_USER_ID = pref.getString("CURRENT_USER_ID", "");
        CURRENT_PHONE = pref.getString("CURRENT_PHONE", "");

        currentUser = mAuth.getCurrentUser();
//        Toast.makeText(mActivity, "onStart", Toast.LENGTH_SHORT).show();
//        Toast.makeText(mActivity, String.valueOf(currentUser), Toast.LENGTH_SHORT).show();
        updateUI(currentUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setTheme(R.style.AppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        mAuth = FirebaseAuth.getInstance();

//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
    }

    private void updateUI(FirebaseUser currentUser) {
//        Toast.makeText(this, "UpdateUI", Toast.LENGTH_SHORT).show();
        if (currentUser != null) {
            CURRENT_PHONE = currentUser.getPhoneNumber();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("phone", CURRENT_PHONE)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                // New user. Get all details from user here
                                Intent i = new Intent(mActivity, RegisterUserActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    CURRENT_USER_ID = documentSnapshot.getId();
                                    editor.putString("CURRENT_USER_ID", CURRENT_USER_ID);
                                    editor.apply();

//                                    Toast.makeText(mActivity, "OnSuccess", Toast.LENGTH_SHORT).show();
                                    details1Uploaded = documentSnapshot.getBoolean("details1Uploaded");
                                    photo2Uploaded = documentSnapshot.getBoolean("details2Uploaded");

                                    CURR_USER_MODEL = documentSnapshot.toObject(UserModel.class);

//                                    CURR_USER_MODEL.setId(documentSnapshot.getId());
//                                    CURR_USER_MODEL.setPhone(documentSnapshot.getString("phone"));
//                                    CURR_USER_MODEL.setName(documentSnapshot.getString("name"));
//                                    CURR_USER_MODEL.setAge(documentSnapshot.getString("age"));
//                                    CURR_USER_MODEL.setGender(documentSnapshot.getString("gender"));
//                                    CURR_USER_MODEL.setLang(documentSnapshot.getString("lang"));
//                                    CURR_USER_MODEL.setPhoto(documentSnapshot.getString("photo"));
//                                    CURR_USER_MODEL.setDetails1Uploaded(documentSnapshot.getBoolean("details1Uploaded"));
//                                    CURR_USER_MODEL.setDetails2Uploaded(documentSnapshot.getBoolean("details2Uploaded"));
//                                    CURR_USER_MODEL.setHeight(documentSnapshot.getDouble("height"));
//                                    CURR_USER_MODEL.setWeight(documentSnapshot.getDouble("weight"));
//                                    CURR_USER_MODEL.setChronicDisease(documentSnapshot.getString("chronicDisease"));

                                    editor.putString("CURR_USER_MODEL", new Gson().toJson(CURR_USER_MODEL));
                                    editor.apply();

                                    Intent intent;
                                    if (details1Uploaded && photo2Uploaded) {
                                        // Intent to Dashboard
                                        intent = new Intent(mActivity, DashboardActivity.class);
                                    } else if (details1Uploaded) {
                                        // Intent to photo upload page
                                        intent = new Intent(mActivity, RegisterStage2Activity.class);
                                    } else {
                                        // Intent to details fill page
                                        intent = new Intent(mActivity, RegisterUserActivity.class);
                                    }
                                    startActivity(intent);
                                    finish();

                                }

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mActivity, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ", e);
                }
            });

        } else {
            boolean isFirstRun = pref.getBoolean("isFirstRun", true);
            Intent intent;
//            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();
            if (isFirstRun) {
                // Intent to Intro Carousel
                intent = new Intent(mActivity, OnBoardingActivity.class);
            } else {
                intent = new Intent(mActivity, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }

    }

}
