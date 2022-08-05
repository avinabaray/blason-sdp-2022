package in.biggeeks.blason.Adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import in.biggeeks.blason.Models.AlertModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class SentAlertsAdapter extends RecyclerView.Adapter<SentAlertsAdapter.ViewHolder> {

    private final Activity mActivity;
    private final View rootLayout;
    private final ArrayList<AlertModel> sentAlerts;
    private ArrayList<UserModel> allUserModels;
    private CommonMethods commonMethods = new CommonMethods();

    public SentAlertsAdapter(Activity mActivity, View rootLayout,
                             ArrayList<AlertModel> sentAlerts,
                             ArrayList<UserModel> allUserModels) {
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
        this.sentAlerts = sentAlerts;
        this.allUserModels = allUserModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_alerts_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
//        holder.setIsRecyclable(false);

        final AlertModel alert = sentAlerts.get(position);

        holder.alertStatusText.setText(alert.getAlertStatus().toString());
        holder.alertLevelText.setText(CommonMethods.normalizeNameString(alert.getAlertLevel().toString()));
        holder.triggerTimeText.setText(CommonMethods.getFormattedDateTime(alert.getTriggerTime()));
        holder.triggerMsgText.setText(alert.getTriggerText());

        switch (alert.getAlertStatus()) {
            case ACTIVE:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_red));
                break;
            case RESPONDED:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_ochre));
                break;
            case FINISHED:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_green));
                break;
            default:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_grey));
        }

        switch (alert.getAlertLevel()) {
            case EXTREME:
                holder.alertLevelText.setTextColor(mActivity.getColor(R.color.extreme));
                break;
            case MODERATE:
                holder.alertLevelText.setTextColor(mActivity.getColor(R.color.moderate));
                break;
            case MILD:
                holder.alertLevelText.setTextColor(mActivity.getColor(R.color.mild));
                break;
            default:
                holder.alertLevelText.setTextColor(mActivity.getColor(R.color.extreme));
        }

        holder.locateLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.openMap(mActivity, rootLayout,
                        alert.getVictimLoc().getLatitude(),
                        alert.getVictimLoc().getLongitude(),
                        "Your Alerted Location");
            }
        });

        ReceiversAdapter recvAdapter = new ReceiversAdapter(mActivity, rootLayout,
                alert.getReceiverIDs(), alert.getSeenByIDs(), allUserModels);

        holder.receiversRecycler.setAdapter(recvAdapter);
        holder.receiversRecycler.setLayoutManager(new LinearLayoutManager(mActivity));

        if (alert.getAlertStatus() == AlertModel.AlertStatus.FINISHED) {
            setNotifyBtnStatus(holder.imSafeBtn, true);

        } else {
            setNotifyBtnStatus(holder.imSafeBtn, false);

            holder.imSafeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNotifyBtnStatus(holder.imSafeBtn, true);
                    FirebaseFirestore.getInstance()
                            .collection(Constants.FB_ALERTS_COL)
                            .document(alert.getId())
                            .update("alertStatus", AlertModel.AlertStatus.FINISHED)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    commonMethods.makeSnack(rootLayout, "Marked you SAFE");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            setNotifyBtnStatus(holder.imSafeBtn, false);
                            commonMethods.makeSnack(rootLayout, "Failed to FINISH the Alert...");
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    });
                }
            });
        }
    }

    private void setNotifyBtnStatus(MaterialButton btn, boolean enable) {
        if (enable) {
            btn.setTypeface(btn.getTypeface(), Typeface.BOLD);
            btn.setStrokeColorResource(R.color.delivered_green);
            btn.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.delivered_green));
            btn.setText("I'm SAFE");
            btn.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
            btn.setEnabled(false);
        } else {
            btn.setTypeface(btn.getTypeface(), Typeface.NORMAL);
            btn.setStrokeColorResource(R.color.reject_red);
            btn.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.light_pink2));
            btn.setText("Mark me SAFE");
            btn.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_red2));
            btn.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return sentAlerts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView alertStatusText, alertLevelText, triggerTimeText, triggerMsgText;
        private LinearLayout locateLinLay;
        private RecyclerView receiversRecycler;
        private MaterialButton imSafeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            alertStatusText = itemView.findViewById(R.id.alertStatusText);
            alertLevelText = itemView.findViewById(R.id.alertLevelText);
            triggerTimeText = itemView.findViewById(R.id.triggerTimeText);
            triggerMsgText = itemView.findViewById(R.id.triggerMsgText);
            locateLinLay = itemView.findViewById(R.id.locateLinLay);
            receiversRecycler = itemView.findViewById(R.id.receiversRecycler);
            imSafeBtn = itemView.findViewById(R.id.imSafeBtn);
        }
    }
}
