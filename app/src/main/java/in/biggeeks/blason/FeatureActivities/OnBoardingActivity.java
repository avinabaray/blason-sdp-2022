package in.biggeeks.blason.FeatureActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.ArrayList;

import in.biggeeks.blason.LoginActivity;
import in.biggeeks.blason.R;
import in.biggeeks.blason.Utils.CommonMethods;

public class OnBoardingActivity extends AppCompatActivity {

    private CardView nextCard;
    private Activity mActivity;
    private CarouselView carouselView;
    private ArrayList<String> lottieSrc = new ArrayList<>();
    private ArrayList<String> lottieName = new ArrayList<>();
    private ArrayList<String> lottieSubText = new ArrayList<>();
    private ArrayList<LottieAnimationView> lotties = new ArrayList<>();
    private boolean[] lottiePlaying;
    private boolean firstPlay = true;
    private TextView swipeText;
    private boolean swipeCardEnabled = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_carousel);
        mActivity = this;

        initViews();
        initOnBoardLotties();
        enableSwipeCardView(false);

        nextCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    i = new Intent(mActivity, LoginActivity.class);
                    startActivity(i);
                } else {
                    // Closes onBoarding and returns where user
                    // was if the user was Logged in
                    mActivity.finish();
                }
            }
        });
    }

    private void initViews() {
        nextCard = findViewById(R.id.nextCard);
        carouselView = findViewById(R.id.carouselView);
        swipeText = findViewById(R.id.swipeText);

    }

    private void initOnBoardLotties() {
        // -------------------------- OnBoard View START --------------------------

        lottieSrc.add("danger_cactus.json");
        lottieSrc.add("phone_alert.json");
        lottieSrc.add("police_car.json");

        lottieName.add("Stuck in Danger");
        lottieName.add("Mighty Blason helps");
        lottieName.add("Police arrives On-Time");

        lottieSubText.add("When no one is near you to help you out\uD83D\uDE29... Don't even have time to call " +
                "your Near-Ones\uD83D\uDE35\uD83D\uDE28");
        lottieSubText.add("This app will trigger up automatically based on your movements to notify all your Near-Ones... " +
                "Now they can respond!!!\nNow Feel Safe\uD83D\uDE03");
        lottieSubText.add("The Police\uD83D\uDC6E is also informed in extreme cases. Voila!!! They are here to help you... " +
                "We got you covered under our Blason Shield");

        for (int i = 0; i < lottieSrc.size(); i++) {
            lotties.add(null);
        }
        lottiePlaying = new boolean[lottieSrc.size()];

        carouselView = findViewById(R.id.carouselView);
        carouselView.setPageCount(lottieSrc.size());
        carouselView.setViewListener(onBoardCarouselViewListener);
        carouselView.stopCarousel();
        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("onPageScrolled",
//                        "position:" + position +
//                                ", positionOffset:" + positionOffset +
//                                ", positionOffsetPixels:" + positionOffsetPixels);

                if (positionOffset >= 0.0f && positionOffset < 0.3f) {
                    animateLottie(position, true);
                } else {
                    animateLottie(position, false);
                }

                if (positionOffset > 0.7f && positionOffset < 0.99f) {
                    animateLottie(position + 1, true);
                } else {
                    animateLottie((position + 1), false);
                }

            }

            @Override
            public void onPageSelected(int position) {
                if (position == lottieSrc.size() - 1) {
                    enableSwipeCardView(true);
                } else {
                    enableSwipeCardView(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        carouselView.playCarousel();

        // -------------------------- OnBoard View END --------------------------

    }

    /**
     * Play or pause the Lottie file depending on its present animation status
     *
     * @param pos  position of the LottieFile in the {@link #lotties} ArrayList
     * @param play to Play or to Pause the animation
     */
    private void animateLottie(int pos, boolean play) {
        // To prevent ArrayIndexOutOfBoundsException
        if (pos >= 0 && pos < lottieSrc.size()) {

            if (play) { // For playing
                if (!lottiePlaying[pos]) {
                    lotties.get(pos).resumeAnimation();
                    lottiePlaying[pos] = true;
                }
            } else {    // For pausing
                if (lottiePlaying[pos]) {
                    lotties.get(pos).pauseAnimation();
                    lottiePlaying[pos] = false;
                }
            }

        }
    }

    private void enableSwipeCardView(boolean enable) {
        if (enable && !swipeCardEnabled) {
            nextCard.setEnabled(true);
            nextCard.setCardElevation(CommonMethods.convertDpToPixel(2f));
            swipeText.setText(R.string.next);
            nextCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
            swipeCardEnabled = true;
        } else if (!enable && swipeCardEnabled) {
            nextCard.setEnabled(false);
            nextCard.setCardElevation(CommonMethods.convertDpToPixel(0f));
            swipeText.setText(R.string.swipe_left);
            nextCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.disabled_download));
            swipeCardEnabled = false;
        }
    }

    ViewListener onBoardCarouselViewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {

            View customView = getLayoutInflater().inflate(R.layout.on_board_carousel_item, null);
            if (lotties.get(position) == null)
                lotties.set(position, (LottieAnimationView) customView.findViewById(R.id.lottieInCarousel));

            TextView buttonAct = customView.findViewById(R.id.buttonAct);
            TextView subCategoryTextView = customView.findViewById(R.id.subCategoryTextView);

            lotties.get(position).setAnimation(lottieSrc.get(position));
            lotties.get(position).enableMergePathsForKitKatAndAbove(true);
            lotties.get(position).loop(true);
            buttonAct.setText(lottieName.get(position));
            subCategoryTextView.setText(lottieSubText.get(position));

            if (firstPlay) {
                animateLottie(0, true);
//                lotties.get(0).resumeAnimation();
                firstPlay = false;
            }

            return customView;
        }
    };

    public void toLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
