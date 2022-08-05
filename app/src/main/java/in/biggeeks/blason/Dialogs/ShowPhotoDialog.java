package in.biggeeks.blason.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import in.biggeeks.blason.R;

public class ShowPhotoDialog extends Dialog {
    private Activity mActivity;
    private Bitmap resource;
    private String name;
    private TextView nameText;
    private ImageView userImage;
    private String imageUrl;

    public ShowPhotoDialog(Activity mActivity, Bitmap resource, String name) {
        super(mActivity);
        this.mActivity = mActivity;
        this.resource = resource;
        this.name = name;
    }

    public ShowPhotoDialog(Activity mActivity, String imageUrl, String name) {
        super(mActivity);
        this.mActivity = mActivity;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_photo_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initViews();
    }

    private void initViews() {
        userImage = findViewById(R.id.userImage);
        nameText = findViewById(R.id.nameText);

        if (resource != null) {
            userImage.setImageBitmap(resource);
        } else {
            Glide.with(mActivity)
                    .asBitmap()
                    .load(imageUrl)
                    .thumbnail(0.3f)
                    .into(userImage);
        }
        nameText.setText(name);
    }

}
