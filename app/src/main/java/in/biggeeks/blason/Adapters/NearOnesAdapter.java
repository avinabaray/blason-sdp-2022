package in.biggeeks.blason.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import in.biggeeks.blason.MainActivity;
import in.biggeeks.blason.Models.NearOnesModel;
import in.biggeeks.blason.Models.UserModel;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;
import in.biggeeks.blason.Utils.Constants;

public class NearOnesAdapter extends RecyclerView.Adapter<NearOnesAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<NearOnesModel> nearOnesArray = new ArrayList<>();
    private ArrayList<UserModel> nearOnesUserArray;
    private View rootLayout;

    public NearOnesAdapter(Activity mActivity,
                           ArrayList<NearOnesModel> nearOnesArray,
                           ArrayList<UserModel> nearOnesUserArray,
                           View rootLayout) {
        this.mActivity = mActivity;
        this.nearOnesArray = nearOnesArray;
        this.nearOnesUserArray = nearOnesUserArray;
        this.rootLayout = rootLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.near_ones_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.relationText.setText(nearOnesArray.get(position).getRelation());

        UserModel userModel = new UserModel();
        for (int i = 0; i < Math.min(nearOnesArray.size(), nearOnesUserArray.size()); i++) {
            if (nearOnesArray.get(position).getId().equals(nearOnesUserArray.get(i).getId())) {
                userModel = nearOnesUserArray.get(i);
                break;
            }
        }
        Glide.with(mActivity)
                .asBitmap()
                .load(userModel.getPhoto().trim())
                .into(holder.userImage);
        holder.nameText.setText(CommonMethods.normalizeNameString(userModel.getName()));
        holder.phoneText.setText(userModel.getPhone().substring(3));
        holder.genderText.setText(CommonMethods.normalizeNameString(userModel.getGender()));
        final UserModel finalUserModel = userModel;
        holder.callNearOneCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.call(mActivity, finalUserModel.getPhone());
            }
        });

        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
                alertDialogBuilder.setTitle("Delete " + mActivity.getString(R.string.near_one))
                        .setMessage("Are you sure?")
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FB_USERS_COL)
                                        .document(MainActivity.CURR_USER_MODEL.getId())
                                        .collection(Constants.FB_NEAR_ONES_COL)
                                        .document(nearOnesArray.get(position).getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                new CommonMethods().makeSnack(rootLayout,
                                                        mActivity.getString(R.string.near_one) + " deleted");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new CommonMethods().makeSnack(rootLayout, "Error while deleting");
                                        e.printStackTrace();
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                });

                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                positiveButtonLL.leftMargin = CommonMethods.convertDpToPixel(5);
                positiveButton.setLayoutParams(positiveButtonLL);

            }
        });

    }

    @Override
    public int getItemCount() {
        return Math.min(nearOnesArray.size(), nearOnesUserArray.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImage, deleteImage;
        private CardView callNearOneCard;
        private TextView nameText, relationText, phoneText, genderText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            nameText = itemView.findViewById(R.id.nameText);
            relationText = itemView.findViewById(R.id.relationText);
            phoneText = itemView.findViewById(R.id.phoneText);
            callNearOneCard = itemView.findViewById(R.id.callNearOneCard);
            genderText = itemView.findViewById(R.id.genderText);
            deleteImage = itemView.findViewById(R.id.deleteImage);
        }
    }
}
