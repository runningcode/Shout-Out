package com.osacky.shoutout.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringUtil;
import com.osacky.shoutout.R;
import com.osacky.shoutout.utils.Constants;

public class SpringyImageView extends ImageView implements SpringListener {

    private static final float SHRINK_PERCENT = 0.2f;
    private Spring animSpring = Constants.springSystem.createSpring()
            .setSpringConfig(Constants.ORIGAMI_SPRING_CONFIG)
            .addListener(new AnimListener());
    private Spring buttonSpring = Constants.springSystem.createSpring()
            .setSpringConfig(Constants.BUTTON_SPRING_CONFIG)
            .addListener(this);

    private final float mStartX;
    private final float mStartY;

    public SpringyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SpringyButton);

        assert a != null;
        mStartX = a.getDimension(R.styleable.SpringyButton_startX, 0);
        mStartY = a.getDimension(R.styleable.SpringyButton_startY, 0);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        float scale = 1f - (value * SHRINK_PERCENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setScaleX(scale);
            setScaleY(scale);
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {
    }

    @Override
    public void onSpringActivate(Spring spring) {
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {
    }

    public void setTouchValue(double endValue) {
        buttonSpring.setEndValue(endValue);
    }

    public void setAnimSpring(double endValue) {
        animSpring.setEndValue(endValue);
    }

    private class AnimListener implements SpringListener {

        @Override
        public void onSpringUpdate(Spring spring) {
            setVisibility(View.VISIBLE);
            double value = spring.getCurrentValue();

            float selectedTitleScale = (float) SpringUtil.mapValueFromRangeToRange(
                    value, 0, 1, 0, 1);
            float titleTranslateX = (float) SpringUtil.mapValueFromRangeToRange(
                    value, 0, 1, mStartX, 0);
            float titleTranslateY = (float) SpringUtil.mapValueFromRangeToRange(
                    value, 0, 1, mStartY, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setScaleX(selectedTitleScale);
                setScaleY(selectedTitleScale);
                setTranslationX(titleTranslateX);
                setTranslationY(titleTranslateY);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {

        }

        @Override
        public void onSpringActivate(Spring spring) {

        }

        @Override
        public void onSpringEndStateChange(Spring spring) {

        }
    }
}
