package in.biggeeks.blason.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import in.biggeeks.blason.Adapters.SentAlertsAdapter;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.AlertModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class AlertsSentFragment extends Fragment {
    private RecyclerView sentAlertsRecycler;
    private CommonMethods commonMethods = new CommonMethods();
    private Activity mActivity;
    private View rootLayout;
    private ArrayList<AlertModel> sentAlerts = new ArrayList<>();
    private ArrayList<UserModel> allUserModels = new ArrayList<>();
    private boolean initialCall = true;
    private SentAlertsAdapter sentAlertsAdapter;
    private boolean firstLoad = true;
    private ListenerRegistration lisRegd;
    private View errorLayout;
    private TextView errorMsg;
    private ImageView errorImage;

    public AlertsSentFragment(Activity mActivity, View rootLayout) {
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
    }

    public AlertsSentFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts_sent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        sentAlertsRecycler = view.findViewById(R.id.sentAlertsRecycler);
        errorLayout = view.findViewById(R.id.errorLayout);
        errorMsg = view.findViewById(R.id.errorMsg);
        errorImage = view.findViewById(R.id.errorImage);
        errorMsg.setText(R.string.no_alert_sent);
        errorImage.setImageResource(R.drawable.ic_happy);
    }

    private void setData(boolean firstTime) {
        if (firstTime)
            commonMethods.loadingDialogStart(mActivity);

        FirebaseFirestore.getInstance()
                .collection(Constants.FB_USERS_COL)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allUserModels.clear();
                        for (QueryDocumentSnapshot userDocSnap : queryDocumentSnapshots) {
                            allUserModels.add(userDocSnap.toObject(UserModel.class));
                        }

                        lisRegd = FirebaseFirestore.getInstance()
                                .collection(Constants.FB_ALERTS_COL)
                                .whereEqualTo("victimID", MainActivity.CURR_USER_MODEL.getId())
                                .orderBy("triggerTime", Query.Direction.DESCENDING)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                        commonMethods.loadingDialogStop();
                                        if (value != null) {
                                            if (value.getDocuments().size() == 0)
                                                errorLayout.setVisibility(View.VISIBLE);
                                            else
                                                errorLayout.setVisibility(View.GONE);

                                            sentAlerts.clear();
                                            for (QueryDocumentSnapshot docSnap : value) {
                                                AlertModel al = docSnap.toObject(AlertModel.class);
                                                sentAlerts.add(al);
                                            }

                                            Log.wtf("LISTNER", "called");
                                            if (initialCall) {
                                                sentAlertsAdapter = new SentAlertsAdapter(
                                                        mActivity,
                                                        rootLayout,
                                                        sentAlerts,
                                                        allUserModels
                                                );
                                                sentAlertsRecycler.setAdapter(sentAlertsAdapter);
                                                sentAlertsRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
                                                initialCall = false;
                                                Log.wtf("initialCall", "TRUE");
                                            } else {
                                                sentAlertsAdapter.notifyDataSetChanged();
                                                Log.wtf("initialCall", "FALSE");
                                            }
                                        } else {
                                            //noinspection ConstantConditions
                                            FirebaseCrashlytics.getInstance().recordException(error);
                                        }
                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        setData(firstLoad);
        firstLoad = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lisRegd != null)
            lisRegd.remove();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lisRegd != null)
            lisRegd.remove();
    }
}
