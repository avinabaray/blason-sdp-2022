package in.biggeeks.blason.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.NearOnesModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class AddNewNearOneDialog extends Dialog implements View.OnClickListener {

    private Activity mActivity;
    private View rootLayout;
    private EditText nearOnePhoneEdit;
    private Spinner relationsSpinner;
    private ProgressBar verifyPhoneProgress;
    private ImageView userImage;
    private TextView errorText, nearOneNameText, nearOnePhoneText;
    private LinearLayout getNearOneLinLay, viewNearOneLinLay;
    private MaterialButton cancelButton, verifyButton, addButton;

    private boolean verified = false;
    private UserModel newNearOneUserModel;

    public AddNewNearOneDialog(@NonNull Activity mActivity, View rootLayout) {
        super(mActivity);
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_near_one_dialog);

        initViews();
        cancelButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        addButton.setOnClickListener(this);

    }

    private void initViews() {
        nearOnePhoneEdit = findViewById(R.id.nearOnePhoneEdit);
        verifyPhoneProgress = findViewById(R.id.verifyPhoneProgress);
        errorText = findViewById(R.id.errorText);
        userImage = findViewById(R.id.userImage);
        nearOneNameText = findViewById(R.id.nearOneNameText);
        nearOnePhoneText = findViewById(R.id.nearOnePhoneText);
        getNearOneLinLay = findViewById(R.id.getNearOneLinLay);
        viewNearOneLinLay = findViewById(R.id.viewNearOneLinLay);
        relationsSpinner = findViewById(R.id.relationsSpinner);
        cancelButton = findViewById(R.id.cancelButton);
        verifyButton = findViewById(R.id.verifyButton);
        addButton = findViewById(R.id.addButton);

        updateUI(false);
    }

    private void updateUI(boolean isVerified) {
        if (!isVerified) {
            getNearOneLinLay.setVisibility(View.VISIBLE);
            verifyButton.setVisibility(View.VISIBLE);
            verifyPhoneProgress.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);

            viewNearOneLinLay.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        } else {
            getNearOneLinLay.setVisibility(View.GONE);
            verifyButton.setVisibility(View.GONE);
            verifyPhoneProgress.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);

            viewNearOneLinLay.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                dismiss();
                break;
            case R.id.verifyButton:
                if (nearOnePhoneEdit.getText().toString().trim().length() != 10) {
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(R.string.invalid_phn);
                    break;
                }
                FirebaseFirestore.getInstance()
                        .collection(Constants.FB_USERS_COL)
                        .whereEqualTo("phone", Constants.COUNTRY_CODE + nearOnePhoneEdit.getText().toString().trim())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                verifyPhoneProgress.setVisibility(View.GONE);
                                if (queryDocumentSnapshots.isEmpty()) {
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(R.string.user_not_found);
                                } else {
                                    updateUI(true);
                                    newNearOneUserModel = queryDocumentSnapshots.getDocuments().get(0)
                                            .toObject(UserModel.class);

                                    if (newNearOneUserModel != null) {
                                        Glide.with(mActivity)
                                                .asBitmap()
                                                .load(newNearOneUserModel.getPhoto())
                                                .into(userImage);
                                        nearOneNameText.setText(newNearOneUserModel.getName());
                                        nearOnePhoneText.setText(CommonMethods.formatPhone(newNearOneUserModel.getPhone()));
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        verifyPhoneProgress.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(R.string.try_again);
                        e.printStackTrace();
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                });

                verifyPhoneProgress.setVisibility(View.VISIBLE);

                break;
            case R.id.addButton:
                dismiss();
                if (newNearOneUserModel != null) {
                    NearOnesModel nearOnesModel = new NearOnesModel();
                    nearOnesModel.setId(newNearOneUserModel.getId());
                    nearOnesModel.setRelation(relationsSpinner.getSelectedItem().toString().trim());
                    nearOnesModel.setAddedOn(Timestamp.now());

                    FirebaseFirestore.getInstance()
                            .collection(Constants.FB_USERS_COL)
                            .document(MainActivity.CURR_USER_MODEL.getId())
                            .collection(Constants.FB_NEAR_ONES_COL)
                            .document(newNearOneUserModel.getId())
                            .set(nearOnesModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Do nothing
                                    new CommonMethods().makeSnack(rootLayout, mActivity.getString(R.string.near_one_added));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new CommonMethods().makeSnack(rootLayout, mActivity.getString(R.string.try_again));
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    });
                } else {
                    new CommonMethods().makeSnack(rootLayout, mActivity.getString(R.string.try_again));
                }
                break;

        }
    }
}
