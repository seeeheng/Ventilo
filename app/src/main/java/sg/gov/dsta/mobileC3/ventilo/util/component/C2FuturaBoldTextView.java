package sg.gov.dsta.mobileC3.ventilo.util.component;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import sg.gov.dsta.mobileC3.ventilo.R;

public class C2FuturaBoldTextView extends AppCompatTextView {

    public C2FuturaBoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public C2FuturaBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public C2FuturaBoldTextView(Context context) {
        super(context);
    }

    private void init() {
        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.futura_bold);
        setTypeface(tf);
    }
}

