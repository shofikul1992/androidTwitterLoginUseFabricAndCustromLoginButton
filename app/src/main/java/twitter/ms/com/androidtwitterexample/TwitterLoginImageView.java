package twitter.ms.com.androidtwitterexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.lang.ref.WeakReference;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.CommonUtils;

/**
 * Created by admin on 12/03/2017.
 */

public class TwitterLoginImageView extends ImageView {


    final static String TAG = TwitterCore.TAG;
    static final String ERROR_MSG_NO_ACTIVITY = "TwitterLoginImageView requires an activity."
            + " Override getActivity to provide the activity for this button.";

    final WeakReference<Activity> activityRef;
    volatile TwitterAuthClient authClient;
    OnClickListener onClickListener;
    Callback<TwitterSession> callback;

    public TwitterLoginImageView(Context context) {
        this(context, null);
    }

    public TwitterLoginImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 0 = no style will be applied
    }

    public TwitterLoginImageView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, null);
    }

    TwitterLoginImageView(Context context, AttributeSet attrs, int defStyle,
                          TwitterAuthClient authClient) {
        super(context, attrs, defStyle);
        this.activityRef = new WeakReference<>(getActivity());
        this.authClient = authClient;
//        setupImageView();

        super.setOnClickListener(new LoginClickListener());

        checkTwitterCoreAndEnable();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupImageView() {
//        final Resources res = getResources();
//        super.setCompoundDrawablesWithIntrinsicBounds(
//                res.getDrawable(com.twitter.sdk.android.core.R.drawable.tw__ic_logo_default), null, null, null);
//        super.setCompoundDrawablePadding(
//                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_drawable_padding));
//        super.setText(com.twitter.sdk.android.core.R.string.tw__login_btn_txt);
//        super.setTextColor(res.getColor(com.twitter.sdk.android.core.R.color.tw__solid_white));
//        super.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_text_size));
//        super.setTypeface(Typeface.DEFAULT_BOLD);
//        super.setPadding(res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_left_padding), 0,
//                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_right_padding), 0);
//        super.setBackgroundResource(com.twitter.sdk.android.core.R.drawable.tw__login_btn);
        super.setOnClickListener(new LoginClickListener());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.setAllCaps(false);
//        }
    }

    /**
     * Sets the {@link com.twitter.sdk.android.core.Callback} to invoke when login completes.
     *
     * @param callback The callback interface to invoke when login completes.
     * @throws java.lang.IllegalArgumentException if callback is null.
     */
    public void setCallback(Callback<TwitterSession> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        this.callback = callback;
    }

    /**
     * @return the current {@link com.twitter.sdk.android.core.Callback}
     */
    public Callback<TwitterSession> getCallback() {
        return callback;
    }

    /**
     * Call this method when {@link android.app.Activity#onActivityResult(int, int, Intent)}
     * is called to complete the authorization flow.
     *
     * @param requestCode the request code used for SSO
     * @param resultCode the result code returned by the SSO activity
     * @param data the result data returned by the SSO activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getTwitterAuthClient().getRequestCode()) {
            getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets the activity. Override this method if this button was created with a non-Activity
     * context.
     */
    protected Activity getActivity() {
        if (getContext() instanceof Activity) {
            return (Activity) getContext();
        } else if (isInEditMode()) {
            return null;
        } else {
            throw new IllegalStateException(ERROR_MSG_NO_ACTIVITY);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            checkCallback(callback);
            checkActivity(activityRef.get());

            getTwitterAuthClient().authorize(activityRef.get(), callback);

            if (onClickListener != null) {
                onClickListener.onClick(view);
            }
        }

        private void checkCallback(Callback callback) {
            if (callback == null) {
                CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,
                        "Callback must not be null, did you call setCallback?");
            }
        }

        private void checkActivity(Activity activity) {
            if (activity == null || activity.isFinishing()) {
                CommonUtils.logOrThrowIllegalStateException(TwitterCore.TAG,
                        ERROR_MSG_NO_ACTIVITY);
            }
        }
    }

    TwitterAuthClient getTwitterAuthClient() {
        if (authClient == null) {
            synchronized (TwitterLoginImageView.class) {
                if (authClient == null) {
                    authClient = new TwitterAuthClient();
                }
            }
        }
        return authClient;
    }

    private void checkTwitterCoreAndEnable() {
        //Default (Enabled) in edit mode
        if (isInEditMode()) return;

        try {
            TwitterCore.getInstance();
        } catch (IllegalStateException ex) {
            //Disable if TwitterCore hasn't started
            Fabric.getLogger().e(TAG, ex.getMessage());
            setEnabled(false);
        }
    }


}
