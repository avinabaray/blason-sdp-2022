package in.biggeeks.blason.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import in.biggeeks.blason.Dialogs.ShowPhotoDialog;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFrag";
    Activity mActivity;
    private ImageView userImage, genderImage;
    private TextView userNameText, phoneNoText, genderText, heightText, weightText, languageText, diseasesText;
    private CardView phoneCard;

    public ProfileFragment(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public static Fragment newInstance(Activity mActivity) {
        ProfileFragment fragment = new ProfileFragment(mActivity);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setData();

        return view;
    }

    private void initViews(View view) {
        userImage = view.findViewById(R.id.userImage);
        userNameText = view.findViewById(R.id.userNameText);
        phoneNoText = view.findViewById(R.id.phoneNoText);
        phoneCard = view.findViewById(R.id.phoneCard);
        genderImage = view.findViewById(R.id.genderImage);
        genderText = view.findViewById(R.id.genderText);
        heightText = view.findViewById(R.id.heightText);
        weightText = view.findViewById(R.id.weightText);
        languageText = view.findViewById(R.id.languageText);
        diseasesText = view.findViewById(R.id.diseasesText);
    }

    private void setData() {
        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .document(MainActivity.CURR_USER_MODEL.getId())
                .addSnapshotListener(mActivity, new EventListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            MainActivity.CURR_USER_MODEL = value.toObject(UserModel.class);
                            if (MainActivity.CURR_USER_MODEL != null) {
                                Glide.with(mActivity)
                                        .asBitmap()
                                        .load(MainActivity.CURR_USER_MODEL.getPhoto().trim())
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull final Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                userImage.setImageBitmap(resource);
                                                userImage.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ShowPhotoDialog dialog = new ShowPhotoDialog(mActivity, resource, MainActivity.CURR_USER_MODEL.getName());
                                                        dialog.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });
                                userNameText.setText(MainActivity.CURR_USER_MODEL.getName());
                                phoneNoText.setText(CommonMethods.formatPhone(MainActivity.CURR_USER_MODEL.getPhone()));
                                phoneCard.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CommonMethods.call(mActivity, MainActivity.CURR_USER_MODEL.getPhone());
                                    }
                                });
                                genderText.setText(CommonMethods.normalizeNameString(MainActivity.CURR_USER_MODEL.getGender()));
                                switch (MainActivity.CURR_USER_MODEL.getGender()) {
                                    case "male":
                                        genderImage.setImageResource(R.drawable.ic_man);
                                        break;
                                    case "female":
                                        genderImage.setImageResource(R.drawable.ic_woman);
                                        break;
                                    case "others":
                                        genderImage.setImageResource(R.drawable.ic_other_gen);
                                        break;
                                }

                                heightText.setText(String.valueOf(MainActivity.CURR_USER_MODEL.getHeight()) + " cm");
                                weightText.setText(String.valueOf(MainActivity.CURR_USER_MODEL.getWeight()) + " kg");
                                languageText.setText(CommonMethods.normalizeNameString(MainActivity.CURR_USER_MODEL.getLang()));
                                diseasesText.setText(MainActivity.CURR_USER_MODEL.getChronicDisease());

                            }
                        }

                    }
                });
    }


}
