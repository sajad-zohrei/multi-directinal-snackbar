package com.programmerhive.app.multidirectinsnackbar;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.programmerhive.app.multidirectinsnackbar.enums.SnackbarType;
import com.programmerhive.app.multidirectinsnackbar.layouts.SnackbarLayout;
import com.programmerhive.app.multidirectinsnackbar.listeners.ActionClickListener;
import com.programmerhive.app.multidirectinsnackbar.listeners.EventListener;
import com.programmerhive.app.multidirectinsnackbar.listeners.SwipeDismissTouchListener;

/**
 * View that provides quick feedback about an operation in a small popup at the base of the screen
 */
public class Snackbar extends SnackbarLayout {

    public enum SnackbarDuration {
        LENGTH_SHORT(2000), LENGTH_LONG(3500), LENGTH_INDEFINITE(-1);

        private long duration;

        SnackbarDuration(long duration) {
            this.duration = duration;
        }

        public long getDuration() {
            return duration;
        }
    }

    public static int RTL = 1;
    public static int LTR = 0;

    private SnackbarType mType = SnackbarType.SINGLE_LINE;
    private SnackbarDuration mDuration = SnackbarDuration.LENGTH_LONG;
    private CharSequence mText;
    private int mDirection = 0;
    private int mColor = -1;
    private int mTextColor = -1;
    private int mOffset;
    private long mSnackbarStart;
    private long mSnackbarFinish;
    private long mTimeRemaining = -1;
    private CharSequence mActionLabel;
    private int mActionColor = -1;
    private boolean mAnimated = true;
    private long mCustomDuration = -1;
    private ActionClickListener mActionClickListener;
    private boolean mShouldDismissOnActionClicked = true;
    private EventListener mEventListener;
    private boolean mIsShowing = false;
    private boolean mCanSwipeToDismiss = true;
    private boolean mIsDismissing = false;
    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    private Snackbar(Context context) {
        super(context);
    }

    public static Snackbar with(Context context) {
        return new Snackbar(context);
    }

    /**
     * Sets the type of {@link Snackbar} to be displayed.
     *
     * @param type the {@link SnackbarType} of this instance
     * @return
     */
    public Snackbar type(SnackbarType type) {
        mType = type;
        return this;
    }

    /**
     * Sets the text to be displayed in this {@link Snackbar}
     *
     * @param text
     * @return
     */
    public Snackbar text(CharSequence text) {
        mText = text;
        return this;
    }
/*    *//**
     * Sets the direction to be displayed in this {@link Snackbar}
     *
     * @param direction
     * @return
     *//*
    public Snackbar direction(int direction) {
        mDirection = direction;
        return this;
    }*/

    /**
     * Sets the text to be displayed in this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar text(@StringRes int resId) {
        return text(getContext().getText(resId));
    }

    /**
     * Sets the background color of this {@link Snackbar}
     *
     * @param color
     * @return
     */
    public Snackbar color(int color) {
        mColor = color;
        return this;
    }

    /**
     * Sets the background color of this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar colorResource(@ColorRes int resId) {
        return color(getResources().getColor(resId));
    }

    /**
     * Sets the text color of this {@link Snackbar}
     *
     * @param textColor
     * @return
     */
    public Snackbar textColor(int textColor) {
        mTextColor = textColor;
        return this;
    }

    /**
     * Sets the text color of this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar textColorResource(@ColorRes int resId) {
        return textColor(getResources().getColor(resId));
    }

    /**
     * Sets the action label to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param actionButtonLabel
     * @return
     */
    public Snackbar actionLabel(CharSequence actionButtonLabel) {
        mActionLabel = actionButtonLabel;
        return this;
    }

    /**
     * Sets the action label to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param resId
     * @return
     */
    public Snackbar actionLabel(@StringRes int resId) {
        return actionLabel(getContext().getString(resId));
    }

    /**
     * Sets the color of the action button label. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param actionColor
     * @return
     */
    public Snackbar actionColor(int actionColor) {
        mActionColor = actionColor;
        return this;
    }

    /**
     * Sets the color of the action button label. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param resId
     * @return
     */
    public Snackbar actionColorResource(@ColorRes int resId) {
        return actionColor(getResources().getColor(resId));
    }

    /**
     * Determines whether this {@link Snackbar} should dismiss when the action button is touched
     *
     * @param shouldDismiss
     * @return
     */
    public Snackbar dismissOnActionClicked(boolean shouldDismiss) {
        mShouldDismissOnActionClicked = shouldDismiss;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link Snackbar} action is
     * selected. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param listener
     * @return
     */
    public Snackbar actionListener(ActionClickListener listener) {
        mActionClickListener = listener;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link Snackbar} is dismissed.
     *
     * @param listener
     * @return
     */
    public Snackbar eventListener(EventListener listener) {
        mEventListener = listener;
        return this;
    }

    /**
     * Sets on/off animation for this {@link Snackbar}
     *
     * @param withAnimation
     * @return
     */
    public Snackbar animation(boolean withAnimation) {
        mAnimated = withAnimation;
        return this;
    }

    public Snackbar swipeToDismiss(boolean canSwipeToDismiss) {
        mCanSwipeToDismiss = canSwipeToDismiss;
        return this;
    }

    /**
     * Sets the duration of this {@link Snackbar}. See
     * {@link Snackbar.SnackbarDuration} for available options
     *
     * @param duration
     * @return
     */
    public Snackbar duration(SnackbarDuration duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Sets a custom duration of this {@link Snackbar}
     *
     * @param duration custom duration. Value must be greater than 0 or it will be ignored
     * @return
     */
    public Snackbar duration(long duration) {
        mCustomDuration = duration > 0 ? duration : mCustomDuration;
        return this;
    }

    /**
     * Attaches this {@link Snackbar} to an AbsListView (ListView, GridView, ExpandableListView) so
     * it dismisses when the list is scrolled
     *
     * @param absListView
     * @return
     */
    public Snackbar attachToAbsListView(AbsListView absListView) {
        absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                dismiss();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        });

        return this;
    }


//    public Snackbar attachToRecyclerView(RecyclerView recyclerView) {
//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                dismiss();
//            }
//        });
//
//        return this;
//    }

    private FrameLayout.LayoutParams init(Activity parent, int lan) {
        SnackbarLayout layout;
        if (lan == 1) {
            layout = (SnackbarLayout) LayoutInflater.from(parent)
                    .inflate(R.layout.sb__template_rtl, this, true);
        } else {
            layout = (SnackbarLayout) LayoutInflater.from(parent)
                    .inflate(R.layout.sb__template_ltr, this, true);
        }
        Resources res = getResources();
        mColor = mColor != -1 ? mColor : res.getColor(R.color.sb__background);
        mOffset = res.getDimensionPixelOffset(R.dimen.sb__offset);
        float scale = res.getDisplayMetrics().density;

        FrameLayout.LayoutParams params;
        if (res.getBoolean(R.bool.sb__is_phone)) {
            // Phone
            layout.setMinimumHeight(dpToPx(mType.getMinHeight(), scale));
            layout.setMaxHeight(dpToPx(mType.getMaxHeight(), scale));
            layout.setBackgroundColor(mColor);
            params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        } else {
            // Tablet/desktop
            mType = SnackbarType.SINGLE_LINE; // Force single-line
            layout.setMinimumWidth(res.getDimensionPixelSize(R.dimen.sb__min_width));
            layout.setMaxWidth(res.getDimensionPixelSize(R.dimen.sb__max_width));
            layout.setBackgroundResource(R.drawable.sb__bg);
            GradientDrawable bg = (GradientDrawable) layout.getBackground();
            bg.setColor(mColor);

            params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, dpToPx(mType.getMaxHeight(), scale));

            params.leftMargin = mOffset;
            params.bottomMargin = mOffset;
        }

        params.gravity = Gravity.BOTTOM;

        TextView snackbarText = (TextView) layout.findViewById(R.id.sb__text);
        snackbarText.setText(mText);

        if (mTextColor != -1) {
            snackbarText.setTextColor(mTextColor);
        }

        snackbarText.setMaxLines(mType.getMaxLines());

        TextView snackbarAction = (TextView) layout.findViewById(R.id.sb__action);
        if (!TextUtils.isEmpty(mActionLabel)) {
            requestLayout();
            snackbarAction.setText(mActionLabel);

            if (mActionColor != -1) {
                snackbarAction.setTextColor(mActionColor);
            }

            snackbarAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionClickListener != null) {
                        mActionClickListener.onActionClicked(Snackbar.this);
                    }
                    if (mShouldDismissOnActionClicked) {
                        dismiss();
                    }
                }
            });
            snackbarAction.setMaxLines(mType.getMaxLines());
        } else {
            snackbarAction.setVisibility(GONE);
        }

        setClickable(true);

        if (mCanSwipeToDismiss && res.getBoolean(R.bool.sb__is_swipeable)) {
            setOnTouchListener(new SwipeDismissTouchListener(this, null,
                    new SwipeDismissTouchListener.DismissCallbacks() {
                        @Override
                        public boolean canDismiss(Object token) {
                            return true;
                        }

                        @Override
                        public void onDismiss(View view, Object token) {
                            if (view != null) {
                                dismiss(false);
                            }
                        }

                        @Override
                        public void pauseTimer(boolean shouldPause) {
                            if (isIndefiniteDuration()) {
                                return;
                            }
                            if (shouldPause) {
                                removeCallbacks(mDismissRunnable);

                                mSnackbarFinish = System.currentTimeMillis();
                            } else {
                                mTimeRemaining -= (mSnackbarFinish - mSnackbarStart);

                                startTimer(mTimeRemaining);
                            }
                        }
                    }));
        }

        return params;
    }

    private static int dpToPx(int dp, float scale) {
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Displays the {@link Snackbar} at the bottom of the
     * {@link android.app.Activity} provided.
     *
     * @param targetActivity
     */
    public void show(Activity targetActivity, int direction) {
        FrameLayout.LayoutParams params = init(targetActivity, direction);

        ViewGroup root = (ViewGroup) targetActivity.findViewById(android.R.id.content);

        root.removeView(this);

        if (isNavigationBarHidden(root)) {
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                params.bottomMargin = resources.getDimensionPixelSize(resourceId);
            }
        }

        root.addView(this, params);

        bringToFront();

        // As requested in the documentation for bringToFront()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            root.requestLayout();
            root.invalidate();
        }

        mIsShowing = true;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (mEventListener != null) {
                    mEventListener.onShow(Snackbar.this);
                    if (!mAnimated) {
                        mEventListener.onShown(Snackbar.this);
                    }
                }
                return true;
            }
        });

        if (!mAnimated) {
            if (shouldStartTimer()) {
                startTimer();
            }
            return;
        }

        Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.sb__in);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mEventListener != null) {
                    mEventListener.onShown(Snackbar.this);
                }

                post(new Runnable() {
                    @Override
                    public void run() {
                        mSnackbarStart = System.currentTimeMillis();

                        if (mTimeRemaining == -1) {
                            mTimeRemaining = getDuration();
                        }
                        if (shouldStartTimer()) {
                            startTimer();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideIn);
    }

    private boolean shouldStartTimer() {
        return !isIndefiniteDuration();
    }

    private boolean isIndefiniteDuration() {
        return getDuration() == SnackbarDuration.LENGTH_INDEFINITE.getDuration();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isNavigationBarHidden(ViewGroup root) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }

        int viewFlags = root.getWindowSystemUiVisibility();
        return (viewFlags & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) ==
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    }

    private void startTimer() {
        postDelayed(mDismissRunnable, getDuration());
    }

    private void startTimer(long duration) {
        postDelayed(mDismissRunnable, duration);
    }

    public void dismiss() {
        dismiss(mAnimated);
    }

    private void dismiss(boolean animate) {
        if (mIsDismissing) {
            return;
        }

        mIsDismissing = true;

        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismiss(Snackbar.this);
        }

        if (!animate) {
            finish();
            return;
        }

        final Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.sb__out);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideOut);
    }

    private void finish() {
        clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismissed(this);
        }
        mIsShowing = false;
    }

    public int getActionColor() {
        return mActionColor;
    }

    public CharSequence getActionLabel() {
        return mActionLabel;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getColor() {
        return mColor;
    }

    public CharSequence getText() {
        return mText;
    }

    public long getDuration() {
        return mCustomDuration == -1 ? mDuration.getDuration() : mCustomDuration;
    }

    public SnackbarType getType() {
        return mType;
    }


    public int getOffset() {
        return mOffset;
    }

    public boolean isAnimated() {
        return mAnimated;
    }

    public boolean shouldDismissOnActionClicked() {
        return mShouldDismissOnActionClicked;
    }

    public boolean isShowing() {
        return mIsShowing;
    }


    public boolean isDismissed() {
        return !mIsShowing;
    }

    @AnimRes
    public static int getInAnimationResource() {
        return R.anim.sb__in;
    }


    @AnimRes
    public static int getOutAnimationResource() {
        return R.anim.sb__out;
    }
}
