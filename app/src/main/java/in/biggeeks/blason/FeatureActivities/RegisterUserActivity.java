package in.biggeeks.blason.FeatureActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import in.biggeeks.blason.BuildConfig;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.Constants;

public class RegisterUserActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "RegisterUsrAct";
    private static final int REQUEST_TAKE_PHOTO = 100;
    private DocumentReference mDocRef = FirebaseFirestore.getInstance().collection("users").document();
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;

    private EditText nameEditText, ageEditText;
    //    Switch genderSwitch;
    Spinner langSpinner;
    private String name, age;
    private String gender = "male";
    private String lang = "english";
    private String userPhoto;
    ArrayList<String> languages;
    private CommonMethods commonMethods;
    private CoordinatorLayout rootLayout;

    Activity mActivity;
    private Uri imageUri;
    private String currentPhotoPath;
    private File photoCompressedFile;
    private Uri photoCompressedUri;
    private AlertDialog.Builder alertBuilder;
    private ProgressDialog progressDialog;
    private MaterialRadioButton maleRadio, femaleRadio, otherGenderRadio;
    private LinearLayout maleLinLay, femaleLinLay, otherGenderLinLay;
    private Button uploadDetailsBtn;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        commonMethods = new CommonMethods();
        mActivity = this;
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        alertBuilder = new AlertDialog.Builder(mActivity);

        // Initialize Storage objects
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        rootLayout = findViewById(R.id.rootLayout);
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);
        maleLinLay = findViewById(R.id.maleLinLay);
        femaleLinLay = findViewById(R.id.femaleLinLay);
        otherGenderLinLay = findViewById(R.id.otherGenderLinLay);
        uploadDetailsBtn = findViewById(R.id.uploadDetailsBtn);

        otherGenderRadio = findViewById(R.id.otherGenderRadio);

        // Radio Buttons initialization

        maleLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleRadio.setChecked(true);
                femaleRadio.setChecked(false);
                otherGenderRadio.setChecked(false);
                gender = "male";
            }
        });

        femaleLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleRadio.setChecked(false);
                femaleRadio.setChecked(true);
                otherGenderRadio.setChecked(false);
                gender = "female";
            }
        });

        otherGenderLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleRadio.setChecked(false);
                femaleRadio.setChecked(false);
                otherGenderRadio.setChecked(true);
                gender = "others";
            }
        });

        langSpinner = findViewById(R.id.langSpinner);

        // Setup Dropdown Menu
        languages = new ArrayList<String>();
        languages.add("English");
        languages.add("Hindi");
        languages.add("Bengali");
        languages.add("Odia");
        languages.add("Tamil");
        languages.add("Telugu");

        ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                languages
        );

        langSpinner.setAdapter(langAdapter);
        langSpinner.setOnItemSelectedListener(this);


//        genderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    gender = "female";
//                } else {
//                    gender = "male";
//                }
//            }
//        });

    }

    public void uploadPhoto(View view) {
        // Camera Intent here
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // This type of creating file doesn't require WRITE_EXTERNAL_STORAGE permission above SDK-18
        File photo = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic_blason.jpg");

//        imageUri = Uri.fromFile(photo);
        imageUri = FileProvider.getUriForFile(
                mActivity,
                BuildConfig.APPLICATION_ID + ".provider",
                photo);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(Intent.createChooser(takePictureIntent, "Capture Picture"), REQUEST_TAKE_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    photoCompressedFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic_blason_comp.jpg");
                    photoCompressedUri = Uri.fromFile(photoCompressedFile);
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        OutputStream fout = new FileOutputStream(photoCompressedFile);
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fout);
//                        Toast.makeText(this, photoCompressedFile.getAbsolutePath().toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(mActivity, "Failed to capture Photo", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

        }
    }

    public void uploadDetails(View view) {
        name = nameEditText.getText().toString();
        age = ageEditText.getText().toString();

        if (name.isEmpty() || age.isEmpty()) {
            commonMethods.makeSnack(rootLayout, "Please fill all the fields");
            return;
        }

        if (photoCompressedUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            MainActivity.CURRENT_USER_ID = mDocRef.getId();
            final StorageReference ref = storageReference.child("user_images/" + MainActivity.CURRENT_USER_ID);
            UploadTask uploadTask = ref.putFile(photoCompressedUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.wtf("URL", downloadUri.toString());
                        userPhoto = downloadUri.toString();

                        photoCompressedFile = null;

                        uploadDataToFirestore(userPhoto);

                    } else {
                        // Handle failures
                        commonMethods.createAlert(alertBuilder, "Failed " + task.getException().getMessage());
                    }
                }
            });

        } else {
            commonMethods.createAlert(alertBuilder, "Please select a photo to upload the item");
        }


    }

    private void uploadDataToFirestore(String userPhoto) {
        // Data to be pushed to Firestore
//        Map<String, Object> userData = new HashMap<String, Object>();
//        userData.put("phone", MainActivity.CURRENT_PHONE);
//        userData.put("name", name);
//        userData.put("age", age);
//        userData.put("gender", gender);
//        userData.put("lang", lang);
//        userData.put("photo", userPhoto);
//        userData.put("details1Uploaded", true);
//        userData.put("details2Uploaded", false);

        UserModel newUserModel = new UserModel();
        newUserModel.setId(mDocRef.getId());
        newUserModel.setPhone(MainActivity.CURRENT_PHONE);
        newUserModel.setName(name);
        newUserModel.setAge(age);
        newUserModel.setGender(gender);
        newUserModel.setLang(lang);
        newUserModel.setPhoto(userPhoto);
        newUserModel.setDetails1Uploaded(true);
        newUserModel.setDetails2Uploaded(false);

        MainActivity.CURR_USER_MODEL = newUserModel;
        editor.putString(Constants.CURR_USER_MODEL, new Gson().toJson(MainActivity.CURR_USER_MODEL));
        editor.apply();

        mDocRef.set(newUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Log.i("STATUS", "User Data saved successfully.");
                    Intent i = new Intent(mActivity, RegisterStage2Activity.class);
                    startActivity(i);
                    finish();
                } else {
                    Log.e(TAG, "onComplete: ", task.getException());
                    commonMethods.makeSnack(rootLayout, "Oops... Please try again");
                }
            }
        });


//                addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentReference> task) {
//                progressDialog.dismiss();
//                if (task.isSuccessful()) {
//                    Log.i("STATUS", "User Data saved successfully.");
//                    Intent i = new Intent(mActivity, RegisterStage2Activity.class);
//                    startActivity(i);
//                } else {
//                    Log.e(TAG, "onComplete: ", task.getException());
//                    commonMethods.makeSnack(rootLayout, "Oops... Please try again");
//                }
//            }
//        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        lang = languages.get(position).toLowerCase();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        lang = languages.get(0).toLowerCase();
    }
}
