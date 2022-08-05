package in.biggeeks.blason.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import in.biggeeks.blason.BuildConfig;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;

public class AboutUsFragment extends Fragment {

    Activity mActivity;
    public static final String TAG = "AboutUsFragment";

    private TextView developerName, devTeamNameText, contactDev, legalAndPrivacy, version;
    private TextView appIntroText;
    private CardView visitUsCard;
    private String devUrl;
    private CommonMethods commonMethods = new CommonMethods();
    private View rootLayout;
    //    private ArrayList<String> legals = new ArrayList<>();
    private String[] legalNames;
    private String[] legalLinks;
    private String websiteUrl = null;

    public AboutUsFragment(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public static AboutUsFragment newInstance(Activity mActivity) {
        AboutUsFragment fragment = new AboutUsFragment(mActivity);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        initViews(view);
        setData();

        return view;
    }

    private void initViews(View view) {

        rootLayout = view.findViewById(R.id.rootLayout);
        appIntroText = view.findViewById(R.id.appIntroText);
        developerName = view.findViewById(R.id.developerName);
        devTeamNameText = view.findViewById(R.id.devTeamNameText);
        visitUsCard = view.findViewById(R.id.visitUsCard);
        contactDev = view.findViewById(R.id.contactDev);
//        legalAndPrivacy = view.findViewById(R.id.legalAndPrivacy);
        version = view.findViewById(R.id.version);

    }

    private void setData() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version : " + versionName);

        contactDev.setText(Html.fromHtml("<u>Contact Developer</u>"));
        devTeamNameText.setText("  Team " + getString(R.string.app_name) + "  ");
//        legalAndPrivacy.setText(Html.fromHtml("<u>" + getString(R.string.legal_and_privacy) + "</u>"));

//        legalNames = new String[]{"Privacy Policy", "Terms & Conditions", "Refund Policy"};
//        legalNames = getResources().getStringArray(R.array.legalNames);
//        legalLinks = new String[legalNames.length];
//        legalAndPrivacy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new MaterialAlertDialogBuilder(mActivity, R.style.AlertDialogTheme_Center)
//                        .setTitle(getString(R.string.legal_and_privacy))
//                        .setIcon(R.drawable.ic_judge)
//                        .setItems(legalNames, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                try {
//                                    switch (which) {
//                                        case 0:
//                                            CommonMethods.openUrlInBrowser(mActivity, legalLinks[0]);
//                                            break;
//                                        case 1:
//                                            CommonMethods.openUrlInBrowser(mActivity, legalLinks[1]);
//                                            break;
//                                        case 2:
//                                            CommonMethods.openUrlInBrowser(mActivity, legalLinks[2]);
//                                            break;
//                                    }
//                                } catch (Exception e) {
//                                    Toast.makeText(mActivity, R.string.conn_error, Toast.LENGTH_SHORT).show();
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).show();
//
//            }
//        });
        visitUsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CommonMethods.openUrlInBrowser(mActivity, websiteUrl);
                } catch (Exception e) {
                    commonMethods.makeSnack(rootLayout, getString(R.string.conn_error));
                }
            }
        });

        FirebaseFirestore.getInstance()
                .collection("constants")
                .document("aboutUs")
                .addSnapshotListener(mActivity, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.getString("introText") != null) {
                                appIntroText.setText(documentSnapshot.getString("introText"));
                            }
                            if (documentSnapshot.getString("developer") != null) {
                                developerName.setText(documentSnapshot.getString("developer"));
                            }
                            devUrl = documentSnapshot.getString("contactDev");
                            contactDev.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        CommonMethods.openUrlInBrowser(mActivity, devUrl);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        commonMethods.makeSnack(rootLayout, getString(R.string.conn_error));
                                    }
                                }
                            });

                            websiteUrl = documentSnapshot.getString("website");
//                            Toast.makeText(mActivity, "UPDATED", Toast.LENGTH_SHORT).show();
//                            legalLinks[0] = documentSnapshot.getString("privacy_policy");
//                            legalLinks[1] = documentSnapshot.getString("terms_condition");
//                            legalLinks[2] = documentSnapshot.getString("refund_policy");

                        }
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
