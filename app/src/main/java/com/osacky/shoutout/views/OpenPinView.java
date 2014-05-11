package com.osacky.shoutout.views;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.osacky.shoutout.R;
import com.osacky.shoutout.models.ShoutOut;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.pin_layout_open)
public class OpenPinView extends FrameLayout {

    @ViewById(R.id.profile_image)
    ImageView imageView;

    @ViewById(R.id.pin_title)
    TextView titleView;

    @ViewById(R.id.pin_text)
    TextView textView;

    public OpenPinView(Context context) {
        super(context);
    }

    public void bind(ShoutOut shoutOut) {
        imageView.setImageBitmap(shoutOut.getBitmap());
        titleView.setText(shoutOut.getName());
        textView.setText(shoutOut.getStatus());
    }
}
