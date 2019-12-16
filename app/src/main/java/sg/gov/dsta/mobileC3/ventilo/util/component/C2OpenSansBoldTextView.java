package sg.gov.dsta.mobileC3.ventilo.util.component;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;

public class C2OpenSansBoldTextView extends AppCompatTextView {

    public C2OpenSansBoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public C2OpenSansBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public C2OpenSansBoldTextView(Context context) {
        super(context);
    }

    private void init() {
        Typeface tf = ResourcesCompat.getFont(MainApplication.getAppContext(), R.font.open_sans_bold);
        setTypeface(tf);
    }

}
