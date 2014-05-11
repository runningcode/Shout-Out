package com.osacky.shoutout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.osacky.shoutout.utils.Constants;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

public class PostStatusFragment extends DialogFragment {

    ParseUser parseUser = ParseUser.getCurrentUser();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText editText = new EditText(getActivity());
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.action_post_status))
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String text = editText.getText().toString();
                                if (!TextUtils.isEmpty(text)) {
                                    Firebase ref = new Firebase(Constants.FIREBASE_URL);
                                    final Map<String, Object> statusUpdate = new HashMap<>();
                                    statusUpdate.put("status", text);
                                    ref.child(Constants.LOC_PATH).child(parseUser.getObjectId())
                                            .updateChildren(statusUpdate);
                                    parseUser.put("status", text);
                                    parseUser.saveInBackground();
                                    dismiss();
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        }
                );
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = android.R.style
                .Animation_Translucent;
        return alertDialog;
    }
}
