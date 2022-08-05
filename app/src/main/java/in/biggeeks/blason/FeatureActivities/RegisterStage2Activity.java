package in.biggeeks.blason.FeatureActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.DashboardActivity;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.Constants;

public class RegisterStage2Activity extends AppCompatActivity {

    private ImageView gifImg;
    private Activity mActivity;
    private EditText heightEditText, weightEditText;
    private Spinner diseasesSpinner;
    private MaterialButton proceedBtn;
    private View rootLayout;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_stage2);
        mActivity = this;
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        initViews();

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (heightEditText.getText().toString().isEmpty() || weightEditText.getText().toString().isEmpty()) {
                    new CommonMethods().makeSnack(rootLayout, getString(R.string.fill_all_fields));
                } else {
                    Double height = Double.valueOf(heightEditText.getText().toString());
                    Double weight = Double.valueOf(weightEditText.getText().toString());
                    String disease = diseasesSpinner.getSelectedItem().toString();

                    MainActivity.CURR_USER_MODEL.setHeight(height);
                    MainActivity.CURR_USER_MODEL.setWeight(weight);
                    MainActivity.CURR_USER_MODEL.setChronicDisease(disease);
                    MainActivity.CURR_USER_MODEL.setDetails2Uploaded(true);

                    editor.putString(Constants.CURR_USER_MODEL, new Gson().toJson(MainActivity.CURR_USER_MODEL));
                    editor.apply();

                    FirebaseFirestore.getInstance()
                            .collection(Constants.FB_USERS_COL)
                            .document(MainActivity.CURR_USER_MODEL.getId())
                            .set(MainActivity.CURR_USER_MODEL)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(mActivity, DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new CommonMethods().makeSnack(rootLayout, getString(R.string.oops_error));
                        }
                    });

//                    Toast.makeText(mActivity, disease, Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        diseasesSpinner = findViewById(R.id.diseasesSpinner);
        proceedBtn = findViewById(R.id.proceedBtn);
    }
}
