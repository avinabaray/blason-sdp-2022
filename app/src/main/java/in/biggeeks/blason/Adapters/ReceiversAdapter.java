package in.biggeeks.blason.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;

public class ReceiversAdapter extends RecyclerView.Adapter<ReceiversAdapter.ViewHolder> {

    private final Activity mActivity;
    private final View rootLayout;
    private ArrayList<String> receiverIDs;
    private ArrayList<String> seenByIDs;
    private final ArrayList<UserModel> allUserModels;
    private CommonMethods commonMethods = new CommonMethods();

    public ReceiversAdapter(Activity mActivity, View rootLayout,
                            ArrayList<String> receiverIDs,
                            ArrayList<String> seenByIDs,
                            ArrayList<UserModel> allUserModels) {
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
        this.receiverIDs = receiverIDs;
        this.seenByIDs = seenByIDs;
        this.allUserModels = allUserModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receivers_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String receiverID = receiverIDs.get(position);
        UserModel um = new UserModel();
        um.setId(receiverID);
        final UserModel recvUserModel = allUserModels.get(allUserModels.indexOf(um));

        holder.recvNameText.setText(recvUserModel.getName());
        if (seenByIDs.contains(receiverID)) {
            holder.checkedImage.setVisibility(View.VISIBLE);
//            holder.recvNameText.leaveSpaceOnRight(true);
        } else {
            holder.checkedImage.setVisibility(View.GONE);
//            holder.recvNameText.leaveSpaceOnRight(false);
        }

        String genPrep = "them";
        if (recvUserModel.getGender().trim().toUpperCase().toCharArray()[0] == 'M') genPrep = "him";
        if (recvUserModel.getGender().trim().toUpperCase().toCharArray()[0] == 'F') genPrep = "her";

        final String finalGenPrep = genPrep;
        holder.checkedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commonMethods.makeSnack(rootLayout, recvUserModel.getName().split("\\s+")[0] + " has RESPONDED...\nCall " + finalGenPrep + " to get assistance");
            }
        });

//        holder.recvAgeGenderText.setText(recvUserModel.getAge() + ", " + recvUserModel.getGender().toUpperCase().toCharArray()[0]);
        holder.recvCallCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.call(mActivity, recvUserModel.getPhone().trim());
            }
        });
    }

    @Override
    public int getItemCount() {
        return receiverIDs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView recvAgeGenderText;
        private TextView recvNameText;
        private CardView recvCallCard;
        private ImageView checkedImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recvNameText = itemView.findViewById(R.id.recvNameText);
            recvAgeGenderText = itemView.findViewById(R.id.recvAgeGenderText);
            recvCallCard = itemView.findViewById(R.id.recvCallCard);
            checkedImage = itemView.findViewById(R.id.checkedImage);
        }
    }
}
