package ru.euphoriadev.vk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.TypefaceManager;

/**
 * Created by Igor on 13.12.15.
 * <p/>
 * {@link android.widget.ImageView} Round picture,
 * in which you can set text
 */
public class TextCircleView extends CircleImageView {
    private final TextPaint mTextPaint = new TextPaint();

    private String mText;
    private float mTextSize = 0;

    public TextCircleView(Context context) {
        super(context);
    }

    public TextCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mText != null) {
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTypeface(TypefaceManager.getBoldTypeface(getContext()));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setTextSize(AndroidUtils.pxFromDp(getContext(), mTextSize == 0 ? 16 : (int) mTextSize));

            int xPos = (getWidth() / 2);
            int yPos = (int) ((getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(mText, xPos, yPos, mTextPaint);
        }
    }


    public void setText(String text) {
        this.mText = text;
        invalidate();
    }


    public void setTextSize(float newTextSize) {
        if (newTextSize != mTextPaint.getTextSize()) {
            mTextSize = newTextSize;

            invalidate();
        }
    }
}
