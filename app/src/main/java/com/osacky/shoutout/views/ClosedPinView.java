package com.osacky.shoutout.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.osacky.shoutout.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.pin_layout_closed)
public class ClosedPinView extends FrameLayout {

    @ViewById(R.id.profile_image)
    ImageView imageView;

    public ClosedPinView(Context context) {
        super(context);
    }

    public void bind(Bitmap image) {
        imageView.setImageBitmap(image);
    }
}
