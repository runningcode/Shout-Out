package com.osacky.shoutout;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.ui.Util;
import com.firebase.client.Firebase;
import com.osacky.shoutout.utils.Constants;
import com.osacky.shoutout.views.RoundedTransformation;
import com.osacky.shoutout.views.SpringyImageView;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EFragment;

import java.util.HashMap;
import java.util.Map;

@EFragment
public class PostStatusFragment extends DialogFragment implements SpringListener {

    private static final String LOG_TAG = PostStatusFragment.class.getSimpleName();

    private EditText editText;
    private View dialog;
    private SpringyImageView postButton;
    private SpringyImageView cameraButton;
    private SpringyImageView optionsButton;

    ParseUser parseUser = ParseUser.getCurrentUser();

    private Spring animSpring = Constants.springSystem.createSpring()
            .setSpringConfig(Constants.ORIGAMI_SPRING_CONFIG)
            .addListener(this);

    private boolean isDismissing = false;

    private static final int mStartY = 300;
    private int mStartDP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartDP = Util.dpToPx(mStartY, getResources());
        setStyle(R.style.DialogTheme, DialogFragment.STYLE_NO_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_update_status, container, false);
        assert root != null;
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().getWindow().setWindowAnimations(R.style.FadeAnimation);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog = root.findViewById(R.id.dialog_background);
        editText = (EditText) root.findViewById(R.id.status_text);
        postButton = (SpringyImageView) root.findViewById(R.id.post_button);
        postButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonClicked(event);
                return true;
            }
        });
        cameraButton = (SpringyImageView) root.findViewById(R.id.camera_button);
        optionsButton = (SpringyImageView) root.findViewById(R.id.options_button);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                postButton.setAnimSpring(1);
                cameraButton.setAnimSpring(1);
                optionsButton.setAnimSpring(1);
            }
        }, 500);
        final ImageView userImage = (ImageView) root.findViewById(R.id.profile_image);

        Picasso.with(getActivity()).load(parseUser.getString("picURL")).transform(new
                RoundedTransformation(200 / 2, 0, 0, 0)).into(userImage);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        animSpring.setEndValue(1);
        getDialog().getActionBar().hide();
    }

    void buttonClicked(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                postButton.setTouchValue(1);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                postButton.setTouchValue(0);
                final String text = editText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    Firebase ref = new Firebase(Constants.FIREBASE_URL);
                    final Map<String, Object> statusUpdate = new HashMap<>();
                    statusUpdate.put("status", text);
                    ref.child(Constants.LOC_PATH).child(parseUser.getObjectId())
                            .updateChildren(statusUpdate);
                    parseUser.put("status", text);
                    parseUser.saveInBackground();
                    isDismissing = true;
                    animSpring.setEndValue(0);
                }
                break;
        }

    }


    @Override
    public void onSpringUpdate(Spring spring) {
        double value = spring.getCurrentValue();
        float selectedTitleScale = (float) SpringUtil.mapValueFromRangeToRange(
                value, 0, 1, 0, 1);
        float titleTranslateY = (float) SpringUtil.mapValueFromRangeToRange(
                value, 0, 1, mStartDP, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dialog.setScaleX(selectedTitleScale);
            dialog.setScaleY(selectedTitleScale);
            dialog.setTranslationY(titleTranslateY);
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        if (isDismissing) {
            dismiss();
        }
    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }
}
