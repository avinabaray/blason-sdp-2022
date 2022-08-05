package in.biggeeks.blason.Fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;

import in.biggeeks.blason.Adapters.AllAlertsAdapter;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.AlertModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class AlertsRecvFragment extends Fragment {

    public static final int QUERY_MAX_LEN = 10;
    private RecyclerView allAlertsRecycler;
    private ArrayList<AlertModel> alerts = new ArrayList<>();
    private CommonMethods commonMethods = new CommonMethods();
    private ArrayList<UserModel> victimUserModelArray = new ArrayList<>();
    private boolean initialCall = true;
    private boolean firstLoad = true;
    private AllAlertsAdapter allAlertsAdapter;
    private Activity mActivity;
    private View rootLayout;
    private ViewPager tabsViewPager;
    private ListenerRegistration listenRegister1;
    private ArrayList<ListenerRegistration> listenRegistersInLoop = new ArrayList<>();
    private View errorLayout;
    private TextView errorMsg;
    private ImageView errorImage;

    public AlertsRecvFragment(Activity mActivity, View rootLayout, ViewPager tabsViewPager) {
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
        this.tabsViewPager = tabsViewPager;
    }

    public AlertsRecvFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts_recv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
//        setData(true);    // This method is being called automatically in onResume()

    }

    private void initViews(View view) {
        allAlertsRecycler = view.findViewById(R.id.allAlertsRecycler);
        errorLayout = view.findViewById(R.id.errorLayout);
        errorMsg = view.findViewById(R.id.errorMsg);
        errorImage = view.findViewById(R.id.errorImage);
        errorMsg.setText(R.string.no_alert_recv);
        errorImage.setImageResource(R.drawable.ic_happy);
    }

    private void setData(boolean firstTime) {
        if (firstTime)
            commonMethods.loadingDialogStart(mActivity);

        listenRegister1 = FirebaseFirestore.getInstance()
                .collection(Constants.FB_ALERTS_COL)
                .whereArrayContains("receiverIDs", MainActivity.CURR_USER_MODEL.getId())
                .orderBy("triggerTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            alerts.clear();

                            if (value.getDocuments().size() == 0)
                                errorLayout.setVisibility(View.VISIBLE);
                            else
                                errorLayout.setVisibility(View.GONE);

                            ArrayList<String> victimUserIDs = new ArrayList<>();
                            for (QueryDocumentSnapshot docSnap : value) {
                                AlertModel al = docSnap.toObject(AlertModel.class);
                                alerts.add(al);
                                victimUserIDs.add(al.getVictimID());
                            }

                            victimUserIDs = new ArrayList<>(new HashSet<>(victimUserIDs));

                            Log.wtf("ArrayList", "Size: " + victimUserIDs.size());


                            // The below code splits the victimIDs into groups of 10 as
                            // .whereIn() query of Cloud Firestore doesn't support a List
                            // of length more than 10

                            ArrayList<ArrayList<String>> victimIDGroups = new ArrayList<>();

                            int totSize = victimUserIDs.size();
                            int toAddIndex = 0;

                            while (totSize > 0) {
                                ArrayList<String> victimIDBatch = new ArrayList<>();
                                if (totSize >= QUERY_MAX_LEN) {
                                    for (int i = toAddIndex; i < toAddIndex + QUERY_MAX_LEN; i++) {
                                        victimIDBatch.add(victimUserIDs.get(i));
                                    }
                                    victimIDGroups.add(victimIDBatch);
                                    toAddIndex += QUERY_MAX_LEN;
                                    totSize -= QUERY_MAX_LEN;
                                } else {
                                    for (int i = toAddIndex; i < victimUserIDs.size(); i++) {
                                        victimIDBatch.add(victimUserIDs.get(i));
                                    }
                                    victimIDGroups.add(victimIDBatch);
                                    totSize = 0;
                                }
                            }

                            Log.wtf("victimUserIDs", "Size: " + victimUserIDs.size());
                            Log.wtf("victimIDGroups", "Size: " + victimIDGroups.size());

                            if (!victimUserIDs.isEmpty()) {
                                for (ArrayList<String> victimIDGroup : victimIDGroups) {
                                    Log.wtf("victimIDGroup in loop", "Size: " + victimIDGroup.size());
                                    ListenerRegistration lis = FirebaseFirestore.getInstance()
                                            .collection(Constants.FB_USERS_COL)
                                            .whereIn("id", victimIDGroup)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    commonMethods.loadingDialogStop();
//                                                    victimUserModelArray.clear();
                                                    if (value != null) {
                                                        for (QueryDocumentSnapshot documentSnapshot : value) {
                                                            victimUserModelArray.add(documentSnapshot.toObject(UserModel.class));
                                                        }
                                                        if (initialCall) {
                                                            allAlertsAdapter = new AllAlertsAdapter(
                                                                    mActivity,
                                                                    alerts,
                                                                    victimUserModelArray,
                                                                    rootLayout,
                                                                    tabsViewPager
                                                            );
                                                            allAlertsRecycler.setAdapter(allAlertsAdapter);
                                                            allAlertsRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
                                                            initialCall = false;
                                                        } else {
                                                            allAlertsAdapter.notifyDataSetChanged();
                                                        }

                                                    } else {
                                                        //noinspection ConstantConditions
                                                        FirebaseCrashlytics.getInstance().recordException(error);
                                                    }
                                                }
                                            });

                                    listenRegistersInLoop.add(lis);
                                }


                            } else {
                                if (initialCall) {
                                    commonMethods.loadingDialogStop();
                                    allAlertsAdapter = new AllAlertsAdapter(
                                            mActivity,
                                            alerts,
                                            victimUserModelArray,
                                            rootLayout,
                                            tabsViewPager);
                                    allAlertsRecycler.setAdapter(allAlertsAdapter);
                                    allAlertsRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
                                    initialCall = false;
                                } else {
                                    allAlertsAdapter.notifyDataSetChanged();
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

    @Override
    public void onResume() {
        super.onResume();
        Log.wtf("onResume", "CALLED");
        setData(firstLoad);
        firstLoad = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.wtf("onPause", "CALLED");
        if (listenRegister1 != null)
            listenRegister1.remove();
        for (ListenerRegistration lis : listenRegistersInLoop) {
            Log.wtf(String.valueOf(lis), "REMOVED");
            if (lis != null)
                lis.remove();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.wtf("onStop", "CALLED");
        for (ListenerRegistration lis : listenRegistersInLoop)
            lis.remove();
    }
}