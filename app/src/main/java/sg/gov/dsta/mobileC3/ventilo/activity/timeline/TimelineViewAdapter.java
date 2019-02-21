package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.qap.ctimelineview.R.id;
import org.qap.ctimelineview.R.layout;
import org.qap.ctimelineview.TimelineRow;

import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;

public class TimelineViewAdapter extends ArrayAdapter<TimelineRow> {
    private List<TimelineRow> mRowDataList;

    public TimelineViewAdapter(Context context, int resource, ArrayList<TimelineRow> objects, boolean orderTheList) {
        super(context, resource, objects);

        if (orderTheList) {
            this.mRowDataList = this.rearrangeByDate(objects);
        } else {
            this.mRowDataList = objects;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TimelineRow row = (TimelineRow) this.mRowDataList.get(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout.ctimeline_row, (ViewGroup) null);
        TextView rowDate = (TextView) view.findViewById(id.crowDate);
        TextView rowTitle = (TextView) view.findViewById(id.crowTitle);
        TextView rowDescription = (TextView) view.findViewById(id.crowDesc);
        ImageView rowImage = (ImageView) view.findViewById(id.crowImg);
        View rowUpperLine = view.findViewById(id.crowUpperLine);
        View rowLowerLine = view.findViewById(id.crowLowerLine);
        float scale = this.getContext().getResources().getDisplayMetrics().density;
        int pixels;
        if (position == 0 && position == this.mRowDataList.size() - 1) {
            rowUpperLine.setVisibility(View.INVISIBLE);
            rowLowerLine.setVisibility(View.INVISIBLE);
        } else if (position == 0) {
            pixels = (int) ((float) row.getBellowLineSize() * scale + 0.5F);
            rowUpperLine.setVisibility(View.INVISIBLE);
            rowLowerLine.setBackgroundColor(row.getBellowLineColor());
            rowLowerLine.getLayoutParams().width = pixels;
        } else if (position == this.mRowDataList.size() - 1) {
            pixels = (int) ((float) ((TimelineRow) this.mRowDataList.get(position - 1)).getBellowLineSize() * scale + 0.5F);
            rowLowerLine.setVisibility(View.INVISIBLE);
            rowUpperLine.setBackgroundColor(((TimelineRow) this.mRowDataList.get(position - 1)).getBellowLineColor());
            rowUpperLine.getLayoutParams().width = pixels;
        } else {
            pixels = (int) ((float) row.getBellowLineSize() * scale + 0.5F);
            int pixels2 = (int) ((float) ((TimelineRow) this.mRowDataList.get(position - 1)).getBellowLineSize() * scale + 0.5F);
            rowLowerLine.setBackgroundColor(row.getBellowLineColor());
            rowUpperLine.setBackgroundColor(((TimelineRow) this.mRowDataList.get(position - 1)).getBellowLineColor());
            rowLowerLine.getLayoutParams().width = pixels;
            rowUpperLine.getLayoutParams().width = pixels2;
        }

        rowDate.setText(DateTimeUtil.getTimeDifference(this.getContext(), row.getDate()));
        if (row.getDateColor() != 0) {
            rowDate.setTextColor(row.getDateColor());
        }

        if (row.getTitle() == null) {
            rowTitle.setVisibility(View.GONE);
        } else {
            rowTitle.setText(row.getTitle());
            if (row.getTitleColor() != 0) {
                rowTitle.setTextColor(row.getTitleColor());
            }
        }

        if (row.getDescription() == null) {
            rowDescription.setVisibility(View.GONE);
        } else {
            rowDescription.setText(row.getDescription());
            if (row.getDescriptionColor() != 0) {
                rowDescription.setTextColor(row.getDescriptionColor());
            }
        }

        if (row.getImage() != null) {
            rowImage.setImageBitmap(row.getImage());
        }

        pixels = (int) ((float) row.getImageSize() * scale + 0.5F);
        rowImage.getLayoutParams().width = pixels;
        rowImage.getLayoutParams().height = pixels;
        View backgroundView = view.findViewById(id.crowBackground);
        if (row.getBackgroundColor() == 0) {
            backgroundView.setBackground((Drawable) null);
        } else {
            if (row.getBackgroundSize() == -1) {
                backgroundView.getLayoutParams().width = pixels;
                backgroundView.getLayoutParams().height = pixels;
            } else {
                int BackgroundPixels = (int) ((float) row.getBackgroundSize() * scale + 0.5F);
                backgroundView.getLayoutParams().width = BackgroundPixels;
                backgroundView.getLayoutParams().height = BackgroundPixels;
            }

            GradientDrawable background = (GradientDrawable) backgroundView.getBackground();
            if (background != null) {
                background.setColor(row.getBackgroundColor());
            }
        }

        MarginLayoutParams marginParams = (MarginLayoutParams) rowImage.getLayoutParams();
        marginParams.setMargins(0, pixels / 2 * -1, 0, pixels / 2 * -1);
        return view;
    }

    private ArrayList<TimelineRow> rearrangeByDate(ArrayList<TimelineRow> objects) {
        if (objects.get(0) == null) {
            return objects;
        } else {
            int size = objects.size();

            for (int i = 0; i < size - 1; ++i) {
                for (int j = i + 1; j < size; ++j) {
                    if (((TimelineRow) objects.get(i)).getDate() != null && ((TimelineRow) objects.get(j)).getDate() != null && ((TimelineRow) objects.get(i)).getDate().compareTo(((TimelineRow) objects.get(j)).getDate()) <= 0) {
                        Collections.swap(objects, i, j);
                    }
                }
            }

            return objects;
        }
    }
}

