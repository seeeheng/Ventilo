package sg.gov.dsta.mobileC3.ventilo.util.component;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

import sg.gov.dsta.mobileC3.ventilo.R;

public class C2OpenSansBlackEditTextView extends AppCompatEditText {

    public C2OpenSansBlackEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public C2OpenSansBlackEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public C2OpenSansBlackEditTextView(Context context) {
        super(context);
    }

    private void init() {
        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.open_sans_extrabold);
        setTypeface(tf);
    }
    
}
