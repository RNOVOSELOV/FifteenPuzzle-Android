package ru.mydroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class SquareButton extends Button {
    public Coordinate coordinate = new Coordinate();

    public SquareButton(Context context) {
        super(context);
    }

    public SquareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
    }
}