package in.biggeeks.blason.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import in.biggeeks.blason.Dialogs.ShowPhotoDialog;
import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.AlertModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class AllAlertsAdapter extends RecyclerView.Adapter<AllAlertsAdapter.ViewHolder> {

    private final Activity mActivity;
    private final ArrayList<AlertModel> alerts;
    private final ArrayList<UserModel> victimUserModelArray;
    private final View rootLayout;
    private ViewPager tabsViewPager;
    private CommonMethods commonMethods = new CommonMethods();
    ArrayList<UserModel> usersInSerial = new ArrayList<>();

    public AllAlertsAdapter(Activity mActivity,
                            ArrayList<AlertModel> alerts,
                            ArrayList<UserModel> victimUserModelArray,
                            View rootLayout, ViewPager tabsViewPager) {

        this.mActivity = mActivity;
        this.alerts = alerts;
        this.rootLayout = rootLayout;
        this.tabsViewPager = tabsViewPager;

        for (int i = 0; i < alerts.size(); i++) {
            UserModel um = new UserModel();
            um.setId(alerts.get(i).getVictimID());
            usersInSerial.add(victimUserModelArray.get(victimUserModelArray.indexOf(um)));
        }
        this.victimUserModelArray = usersInSerial;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_alerts_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        holder.setIsRecyclable(false);
        final AlertModel alert = alerts.get(position);
        UserModel userModel = victimUserModelArray.get(position);

        final UserModel finalUserModel = userModel;
        holder.victimImage.setOnClickListener(null);

        Glide.with(mActivity)
                .asBitmap()
                .load(userModel.getPhoto().trim())
                .thumbnail(0.05f)
//                .into(holder.customTarget);
                .into(holder.victimImage);

        holder.victimImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPhotoDialog dialog = new ShowPhotoDialog(mActivity, finalUserModel.getPhoto().trim(),
                        CommonMethods.normalizeNameString(finalUserModel.getName()));
                dialog.show();
            }
        });

        holder.alertStatusText.setText(alert.getAlertStatus().toString());
        holder.alertLevelText.setText(CommonMethods.normalizeNameString(alert.getAlertLevel().toString()));
        holder.triggerTimeText.setText(CommonMethods.getFormattedDateTime(alert.getTriggerTime()));
        holder.victimNameText.setText(CommonMethods.normalizeNameString(userModel.getName()));
        holder.victimPhoneText.setText(userModel.getPhone().substring(3));
        holder.victimAgeText.setText(userModel.getAge() + " yrs");
        holder.victimGenderText.setText(CommonMethods.normalizeNameString(userModel.getGender()));
        holder.victimDiseaseText.setText(userModel.getChronicDisease());
        holder.triggerMsgText.setText(alert.getTriggerText());

        holder.notifyBtn.setVisibility(View.VISIBLE);
        switch (alert.getAlertStatus()) {
            case ACTIVE:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_red));
                break;
            case RESPONDED:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_ochre));
                break;
            case FINISHED:
                holder.alertStatusText.setBackground(mActivity.getDrawable(R.drawable.alert_header_green));
                holder.notifyBtn.setVisibility(View.GONE);
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

        holder.victimCallLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.call(mActivity, finalUserModel.getPhone());
            }
        });

        holder.victimCallCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.call(mActivity, finalUserModel.getPhone());
            }
        });

        holder.locateLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.openMap(mActivity, rootLayout,
                        alert.getVictimLoc().getLatitude(),
                        alert.getVictimLoc().getLongitude(),
                        finalUserModel.getName().split("\\s+")[0] + " is here");
            }
        });

        if (alert.getSeenByIDs().contains(MainActivity.CURR_USER_MODEL.getId())) {
            setNotifyBtnStatus(holder.notifyBtn, true);
        } else {
            setNotifyBtnStatus(holder.notifyBtn, false);
            holder.notifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNotifyBtnStatus(holder.notifyBtn, true);
                    FirebaseFirestore.getInstance()
                            .collection(Constants.FB_ALERTS_COL)
                            .document(alert.getId())
                            .update("seenByIDs",
                                    FieldValue.arrayUnion(MainActivity.CURR_USER_MODEL.getId()),
                                    "alertStatus", AlertModel.AlertStatus.RESPONDED.toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    commonMethods.makeSnack(rootLayout, "Victim has been Notified");
                                    // Todo Notify Victim here
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            setNotifyBtnStatus(holder.notifyBtn, false);
                            commonMethods.makeSnack(rootLayout, "Notifying FAILED");
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
            btn.setText("Victim Notified");
            btn.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
            btn.setEnabled(false);
        } else {
            btn.setTypeface(btn.getTypeface(), Typeface.NORMAL);
            btn.setStrokeColorResource(R.color.reject_red);
            btn.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.light_pink2));
            btn.setText("Tap to Acknowledge");
            btn.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_red2));
            btn.setEnabled(true);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(mActivity).clear(holder.victimImage);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView alertStatusText, alertLevelText, triggerTimeText, victimNameText, victimPhoneText,
                victimAgeText, victimGenderText, victimDiseaseText, triggerMsgText;
        private ImageView victimImage;
        private LinearLayout victimCallLinLay, locateLinLay;
        private CardView victimCallCard;
//        private SwipeButton notifySwipeButton;
        private NestedScrollView swipeHoldingLinLay;
        private MaterialButton notifyBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            alertStatusText = itemView.findViewById(R.id.alertStatusText);
            alertLevelText = itemView.findViewById(R.id.alertLevelText);
            triggerTimeText = itemView.findViewById(R.id.triggerTimeText);
            victimImage = itemView.findViewById(R.id.victimImage);
            victimNameText = itemView.findViewById(R.id.victimNameText);
            victimCallLinLay = itemView.findViewById(R.id.victimCallLinLay);
            victimPhoneText = itemView.findViewById(R.id.victimPhoneText);
            victimCallCard = itemView.findViewById(R.id.victimCallCard);
            victimAgeText = itemView.findViewById(R.id.victimAgeText);
            victimGenderText = itemView.findViewById(R.id.victimGenderText);
            victimDiseaseText = itemView.findViewById(R.id.victimDiseaseText);
            triggerMsgText = itemView.findViewById(R.id.triggerMsgText);
            locateLinLay = itemView.findViewById(R.id.locateLinLay);
//            notifySwipeButton = itemView.findViewById(R.id.notifySwipeButton);
            notifyBtn = itemView.findViewById(R.id.notifyBtn);
        }
    }
}
