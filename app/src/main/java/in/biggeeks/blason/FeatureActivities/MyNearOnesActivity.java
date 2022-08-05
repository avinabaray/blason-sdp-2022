package in.biggeeks.blason.FeatureActivities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.biggeeks.blason.Adapters.NearOnesAdapter;
import in.biggeeks.blason.Dialogs.AddNewNearOneDialog;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.NearOnesModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class MyNearOnesActivity extends AppCompatActivity {

    private Activity mActivity;
    private RecyclerView nearOnesRecycler;
    private ExtendedFloatingActionButton addNearOneExtFab;
    private ArrayList<NearOnesModel> nearOnesArray = new ArrayList<>();
    private ArrayList<UserModel> nearOnesUserModelArray = new ArrayList<>();
    private View rootLayout;
    private CommonMethods commonMethods = new CommonMethods();
    private boolean initialCall = true;
    private NearOnesAdapter nearOnesAdapter;
    private Toolbar toolbar;
    private View errorLayout;
    private TextView errorMsg;
    private ImageView errorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_near_ones);
        mActivity = this;

        initViews();
        setData();

        addNearOneExtFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewNearOneDialog addNearOneDialog = new AddNewNearOneDialog(mActivity, rootLayout);
                addNearOneDialog.show();
            }
        });
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        nearOnesRecycler = findViewById(R.id.nearOnesRecycler);
        addNearOneExtFab = findViewById(R.id.addNearOneExtFab);
        errorLayout = findViewById(R.id.errorLayout);
        errorMsg = findViewById(R.id.errorMsg);
        errorImage = findViewById(R.id.errorImage);
        errorMsg.setText(R.string.no_near_ones_added);
        errorImage.setImageResource(R.drawable.ic_sad);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(getString(R.string.my_near_ones));

        addNearOneExtFab.hide();
    }

    private void setData() {
        commonMethods.loadingDialogStart(mActivity);
        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .document(MainActivity.CURR_USER_MODEL.getId())
                .collection(Constants.FB_NEAR_ONES_COL)
                .orderBy("addedOn", Query.Direction.DESCENDING)
                .addSnapshotListener(mActivity, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            nearOnesArray.clear();

                            if (value.getDocuments().size() < 5) {
                                addNearOneExtFab.show();
                            } else {
                                addNearOneExtFab.hide();
                            }

                            if (value.getDocuments().size() == 0)
                                errorLayout.setVisibility(View.VISIBLE);
                            else
                                errorLayout.setVisibility(View.GONE);

                            ArrayList<String> nearOneUserIDs = new ArrayList<>();
                            for (QueryDocumentSnapshot documentSnapshot : value) {
                                NearOnesModel nm = documentSnapshot.toObject(NearOnesModel.class);
                                nearOnesArray.add(nm);
                                nearOneUserIDs.add(nm.getId());
                            }

                            if (!nearOneUserIDs.isEmpty()) {
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FB_USERS_COL)
                                        .whereIn("id", nearOneUserIDs)
                                        .addSnapshotListener(mActivity, new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                commonMethods.loadingDialogStop();
                                                nearOnesUserModelArray.clear();
                                                if (value != null) {
                                                    for (QueryDocumentSnapshot documentSnapshot : value) {
                                                        nearOnesUserModelArray.add(documentSnapshot.toObject(UserModel.class));
                                                    }
                                                    if (initialCall) {
                                                        nearOnesAdapter = new NearOnesAdapter(
                                                                mActivity,
                                                                nearOnesArray,
                                                                nearOnesUserModelArray,
                                                                rootLayout
                                                        );
                                                        nearOnesRecycler.setAdapter(nearOnesAdapter);
                                                        nearOnesRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
                                                        initialCall = false;
                                                    } else {
                                                        nearOnesAdapter.notifyDataSetChanged();
                                                    }

                                                } else {
                                                    //noinspection ConstantConditions
                                                    FirebaseCrashlytics.getInstance().recordException(error);
                                                }
                                            }
                                        });
                            } else {
                                if (initialCall) {
                                    commonMethods.loadingDialogStop();
                                    nearOnesAdapter = new NearOnesAdapter(
                                            mActivity,
                                            nearOnesArray,
                                            nearOnesUserModelArray,
                                            rootLayout
                                    );
                                    nearOnesRecycler.setAdapter(nearOnesAdapter);
                                    nearOnesRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
                                    initialCall = false;
                                } else {
                                    nearOnesAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            commonMethods.loadingDialogStop();
                            //noinspection ConstantConditions
                            FirebaseCrashlytics.getInstance().recordException(error);
                        }
                    }
                });
    }
}
