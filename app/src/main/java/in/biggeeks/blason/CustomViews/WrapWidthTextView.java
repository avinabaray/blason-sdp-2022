package in.biggeeks.blason.CustomViews;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;

import in.biggeeks.blason.Utils.CommonMethods;

public class WrapWidthTextView extends androidx.appcompat.widget.AppCompatTextView {

    private int rightSpace = CommonMethods.convertDpToPixel(27);
//    private int rightSpace = 0;

    public WrapWidthTextView(Context context) {
        super(context);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // constructors here

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = getLayout();
        if (layout != null) {
            int width = (int) Math.ceil(getMaxLineWidth(layout))
                    + getCompoundPaddingLeft() + getCompoundPaddingRight();
            int height = getMeasuredHeight();
            setMeasuredDimension(width - rightSpace, height);
        }
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }

    /**
     * leaves a space of 27dp on the TextView's right
     */
    public void leaveSpaceOnRight(boolean b) {
        if (b) {
            rightSpace = CommonMethods.convertDpToPixel(27);
        } else {
            rightSpace = 0;
        }
        postInvalidate();
    }
}