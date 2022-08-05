package in.biggeeks.blason.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;

public class NotifyNearOnesDialog extends Dialog {
    private Activity mActivity;
    private final View rootLayout;
    private ProgressBar notifyingProgress;
    private MaterialButton cancelButton;
    private long total = 1500L;
    private CountDownTimer timer;

    public NotifyNearOnesDialog(@NonNull Activity mActivity, View rootLayout) {
        super(mActivity);
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_near_ones_dialog);

        initViews();
        startProgressBar();
    }


    private void initViews() {
        notifyingProgress = findViewById(R.id.notifyingProgress);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void startProgressBar() {
        notifyingProgress.setMax((int) total);
        timer = new CountDownTimer(total, 1) {
            public void onTick(long millisUntilFinished) {
                notifyingProgress.setProgress((int) (total - millisUntilFinished));
//                Log.d("Time", String.valueOf((int) (total - millisUntilFinished)));
            }

            public void onFinish() {
                notifyingProgress.setProgress((int) total);
                CommonMethods.triggerSOS(
                        mActivity,
                        rootLayout,
                        false,
                        null,
                        null
                );
                dismiss();
            }
        }.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        timer.cancel();
    }
}
