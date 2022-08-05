package in.biggeeks.blason;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import in.biggeeks.blason.FeatureActivities.RegisterStage2Activity;
import in.biggeeks.blason.FeatureActivities.RegisterUserActivity;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.Utils.CommonMethods;

import static in.biggeeks.blason.MainActivity.CURR_USER_MODEL;
import static in.biggeeks.blason.Utils.Constants.COUNTRY_CODE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginAct";

    FirebaseAuth mAuth;
    private CollectionReference mDocRef = FirebaseFirestore.getInstance().collection("users");
    CommonMethods commonMethods = new CommonMethods();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    //    private EditText phone;
//    private EditText otp;
//    private Button otpBtn;
//    private Button loginBtn;
    Activity mActivity;
    private String otpStr;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private View rootLayout;
    //    private TextInputLayout otpLayout;
    private float toSetAlpha = 0f;
    SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private EditText editTextPhone, editTextOtp;
    private Button loginButton, getOtpButton;
    private ImageView imageViewLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = this;
        mAuth = FirebaseAuth.getInstance();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        initViews();
        initSendOtpBtn();
        initLoginBtn();
//        phone = findViewById(R.id.phone);
//        otp = findViewById(R.id.otp);
//        otpBtn = findViewById(R.id.otpBtn);
//        loginBtn = findViewById(R.id.loginBtn);
//        otpLayout = findViewById(R.id.otpLayout);
//
//        phone.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() == 10) {
//                    otpBtn.setAlpha(1.0f);
//                } else {
//                    otpBtn.setAlpha(0.5f);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        imageViewLock = findViewById(R.id.imageViewLock);
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextPhone = findViewById(R.id.editTextPhone);
        loginButton = findViewById(R.id.loginButton);
        getOtpButton = findViewById(R.id.getOtpButton);
    }

    private void initSendOtpBtn() {
        getOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.CURRENT_PHONE = COUNTRY_CODE + editTextPhone.getText().toString();

                mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        commonMethods.loadingDialogStop();
                        commonMethods.makeSnack(rootLayout, "Device Auto-Verified");
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        commonMethods.loadingDialogStop();
                        commonMethods.makeSnack(rootLayout, "Verification Failed");
                        Log.e(TAG, "onVerificationFailed: ", e);
                        FirebaseCrashlytics.getInstance().recordException(e);
                        updateUI(null);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        commonMethods.loadingDialogStop();
                        commonMethods.makeSnack(rootLayout, "OTP Sent");
                        mVerificationId = verificationId;
                        mResendToken = forceResendingToken;
                        editTextOtp.setEnabled(true);
                        imageViewLock.setAlpha(1.0f);
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String verificationId) {
                        super.onCodeAutoRetrievalTimeOut(verificationId);
//                        mVerificationId = verificationId;
                        commonMethods.loadingDialogStop();

                    }
                };

                if (MainActivity.CURRENT_PHONE.length() != (10 + COUNTRY_CODE.length())) {
                    commonMethods.makeSnack(rootLayout, "Invalid Phone Number");
                } else {
                    commonMethods.loadingDialogStart(mActivity, "Sending OTP");
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            MainActivity.CURRENT_PHONE,
                            60,
                            TimeUnit.SECONDS,
                            mActivity,
                            mCallback);
                    getOtpButton.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initLoginBtn() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpStr = editTextOtp.getText().toString();
                if (otpStr.length() > 0) {
                    // Creating the credential to send it to Firebase for verification
                    // through signInWithPhoneAuthCredential() method
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpStr);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    commonMethods.makeSnack(rootLayout, "Invalid OTP");
                }
            }
        });

    }

    public void getOtp(View view) {

    }

    public void login(View view) {

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        commonMethods.loadingDialogStart(mActivity, "Authenticating...");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        commonMethods.loadingDialogStop();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
//                            commonMethods.makeSnack(rootLayout, "Login Successful");
                            updateUI(user);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                commonMethods.makeSnack(rootLayout, "Invalid OTP");
                            } else {
                                commonMethods.makeSnack(rootLayout, "Authentication Failed");
                                if (task.getException() != null)
                                    FirebaseCrashlytics.getInstance().recordException(task.getException());
                                else {
                                    FirebaseCrashlytics.getInstance().log("Auth Obj Null");
                                    FirebaseCrashlytics.getInstance().recordException(new Exception());
                                }
                                updateUI(null);
                            }
                        }
                    }
                });
    }

    /**
     * Updates the Login Activity whether to Log the user in or to sanitize the UI
     *
     * @param user null to prepare UI for another attempt to Login or
     *             pass {@link FirebaseUser} object to login
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            SharedPreferences.Editor newEditor = pref.edit();
            newEditor.putBoolean("isFirstRun", false);
            newEditor.apply();

            commonMethods.loadingDialogStart(mActivity, "Authenticating...");
            mDocRef.whereEqualTo("phone", MainActivity.CURRENT_PHONE)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            commonMethods.loadingDialogStop();
                            editor.putString("CURRENT_PHONE", MainActivity.CURRENT_PHONE);
                            editor.putBoolean("isFirstRun", false);
                            editor.apply();
                            if (queryDocumentSnapshots.isEmpty()) {
                                // New user. Get all details from user here
                                Intent i = new Intent(mActivity, RegisterUserActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                // Exiting user
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    MainActivity.CURRENT_USER_ID = documentSnapshot.getId();
                                    editor.putString("CURRENT_USER_ID", MainActivity.CURRENT_USER_ID);
                                    editor.apply();

                                    MainActivity.details1Uploaded = documentSnapshot.getBoolean("details1Uploaded");
                                    MainActivity.photo2Uploaded = documentSnapshot.getBoolean("details2Uploaded");

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
                                    if (MainActivity.details1Uploaded && MainActivity.photo2Uploaded) {
                                        // Intent to Dashboard
                                        intent = new Intent(mActivity, DashboardActivity.class);
                                    } else if (MainActivity.details1Uploaded) {
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
                    commonMethods.loadingDialogStop();
                    Toast.makeText(mActivity, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Log.e(TAG, "onFailure: ", e);
                }
            });


//            Intent intent = new Intent(mActivity, DashboardActivity.class);
//            startActivity(intent);
        } else {
            // Authentication failed. Refreshing all fields
            commonMethods.makeSnack(rootLayout, "Login failed... Please Try again");

            editTextOtp.setText("");
            editTextOtp.setEnabled(false);
            imageViewLock.setAlpha(0.5f);
//            otpLayout.setAlpha(0.0f);
            loginButton.setVisibility(View.GONE);
            getOtpButton.setVisibility(View.VISIBLE);

//            otp.setText("");
//            otpLayout.setAlpha(0.0f);
//            loginBtn.setVisibility(View.GONE);
        }
    }

}
