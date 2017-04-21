package com.dgroup.blurlayoutlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dimon.
 */

public class BlurLayout extends FrameLayout {

    private static final String LOG = BlurLayout.class.getSimpleName();

    public static int DEFAULT_BLUR_RADIUS = 5;
    public static float DEFAULT_SCALE_FACTOR = 5f;

    private static final int RENDER_PERIOD = 100;

    private int mBlurRadius = DEFAULT_BLUR_RADIUS;
    private float mScaleFactor = DEFAULT_SCALE_FACTOR;
    private boolean cropSubArea;

    private int mTargetViewId;
    private View mTargetView;
    private ImageView mBlurredImageView;

    private Timer mTimer;

    public BlurLayout(@NonNull Context context) {
        super(context);
        init(null);
    }

    public BlurLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BlurLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public BlurLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.BlurLayout, 0, 0);
            try {
                mBlurRadius = ta.getInteger(R.styleable.BlurLayout_blurRadius,
                        DEFAULT_BLUR_RADIUS);
                mScaleFactor = ta.getFloat(R.styleable.BlurLayout_scaleFactor,
                        DEFAULT_SCALE_FACTOR);
                mTargetViewId = ta.getResourceId(R.styleable.BlurLayout_targetView, 0);
                cropSubArea = ta.getBoolean(R.styleable.BlurLayout_cropSubArea, false);
            } finally {
                ta.recycle();
            }
        }
        mBlurredImageView = new ImageView(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mBlurredImageView.setLayoutParams(params);
        mBlurredImageView.setClickable(false);
        mBlurredImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(mBlurredImageView);

//        Button start = new Button(getContext());
//        Button stop = new Button(getContext());
//        start.setText("start");
//        stop.setText("stop");
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                start();
//            }
//        });
//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stop();
//            }
//        });
//        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
//                300, 200);
//        params1.gravity = Gravity.BOTTOM | Gravity.START;
//
//        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
//                300, 200);
//        params2.gravity = Gravity.BOTTOM | Gravity.END;
//
//        addView(start, params1);
//        addView(stop, params2);

    }

    public void setTargetView(View targetView) {
        mTargetViewId = targetView.getId();
        this.mTargetView = targetView;
    }

    public void setBlurRadius(int blurRadius) {
        mBlurRadius = blurRadius;
    }

    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTargetViewId != 0) {
            mTargetView = getRootView().findViewById(mTargetViewId);
        }
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getHandler().removeCallbacksAndMessages(null);
        stop();
    }

    private synchronized void start() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                render();
            }
        }, 0, RENDER_PERIOD);
    }

    private synchronized void stop() {
        mTimer.cancel();
    }

    private void invalidateBlurFrame(final Bitmap bitmap) {
        if(getHandler()!=null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mBlurredImageView.setImageBitmap(bitmap);
                }
            });
        }
    }

    private void render() {
        if (mTargetView != null) {
            Bitmap bitmap = obtainBitmap(mTargetView);
            if (bitmap != null) {
                bitmap = scaleBitmap(bitmap);
                bitmap = Utils.getBlurBitmap(bitmap, mBlurRadius);
                invalidateBlurFrame(bitmap);
            }
        }
    }

    private Bitmap obtainBitmap(View mView) {
        if (mView instanceof TextureView) {
            TextureView textureView = (TextureView) mView;
            return textureView.getBitmap();
        } else {
            Bitmap b = Bitmap.createBitmap(
                    mView.getWidth(),
                    mView.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            mView.draw(c);
            return b;
        }
//        mView.setDrawingCacheEnabled(true);
//        mView.buildDrawingCache();
//        return mView.getDrawingCache();
    }

    private Bitmap scaleBitmap(Bitmap myBitmap) {
        int width = (int) (myBitmap.getWidth() / mScaleFactor);
        int height = (int) (myBitmap.getHeight() / mScaleFactor);
        return Bitmap.createScaledBitmap(myBitmap, width, height, false);
    }
}
