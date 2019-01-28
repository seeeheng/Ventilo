package sg.gov.dsta.mobileC3.ventilo.util.component;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import sg.gov.dsta.mobileC3.ventilo.R;

public class C2LatoBlackButton extends AppCompatButton {

    public C2LatoBlackButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public C2LatoBlackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public C2LatoBlackButton(Context context) {
        super(context);
    }

    private void init() {
        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.lato_black);
        setTypeface(tf);
    }

}
