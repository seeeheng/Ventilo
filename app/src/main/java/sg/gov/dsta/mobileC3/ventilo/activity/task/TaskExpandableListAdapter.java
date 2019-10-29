package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private String mHeaderTitle;
    private List<String> mChildList;
    private String mSelectedChildTaskName;

    public TaskExpandableListAdapter(Context context, String headerTitle,
                                     List<String> childList) {
        mContext = context;
        mHeaderTitle = headerTitle;
        mChildList = childList;
        mSelectedChildTaskName = StringUtil.EMPTY_STRING;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_expandable_list_row_item_text_img,
                    null);

            // Set child text
            C2OpenSansRegularTextView tvChildListItem = convertView.
                    findViewById(R.id.tv_expandable_list_row_item_text);

            tvChildListItem.setText(childText);
            tvChildListItem.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));
        }

        // Set custom child radio button state UI based on selected child item
        AppCompatImageView imgChildItemSelectedIcon = convertView.
                findViewById(R.id.img_expandable_list_row_item_img);

        if (!mSelectedChildTaskName.equalsIgnoreCase(childText)) {
            imgChildItemSelectedIcon.setImageDrawable(ResourcesCompat.
                    getDrawable(mContext.getResources(),
                            R.drawable.icon_new_unselected, null));
        } else {
            imgChildItemSelectedIcon.setImageDrawable(ResourcesCompat.
                    getDrawable(mContext.getResources(),
                            R.drawable.icon_radio_button_selected, null));
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mHeaderTitle;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        AppCompatImageView imgHeaderIcon = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_expandable_list_row_header_text_img,
                    null);

            // Set height of group row when first created
            LinearLayout linearLayoutRowHeader = convertView.findViewById(
                    R.id.linear_layout_expandable_list_row_header_text_img);
            linearLayoutRowHeader.getLayoutParams().height = (int) mContext.getResources().
                    getDimension(R.dimen.input_others_layout_icon_height);

            // Set dimension of indicator icon when first created
            imgHeaderIcon = convertView.
                    findViewById(R.id.img_expandable_list_row_header_img);
            imgHeaderIcon.getLayoutParams().width = (int) mContext.getResources().
                    getDimension(R.dimen.expandable_list_view_row_header_icon_width);
            imgHeaderIcon.getLayoutParams().height = (int) mContext.getResources().
                    getDimension(R.dimen.expandable_list_view_row_header_icon_height);
            imgHeaderIcon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.icon_expand, null));
        }

        // Set header text to be selected item, if any child view is selected
        C2OpenSansRegularTextView tvHeaderTitle = convertView.
                findViewById(R.id.tv_expandable_list_row_header_text);

        if (StringUtil.EMPTY_STRING.equalsIgnoreCase(mSelectedChildTaskName)) {
            tvHeaderTitle.setText(headerTitle);
            tvHeaderTitle.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text_hint_dark_grey));
        } else {
            tvHeaderTitle.setText(mSelectedChildTaskName);
            tvHeaderTitle.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));
        }

        // Set custom group indicator icon state UI
        if (imgHeaderIcon == null) {
            imgHeaderIcon = convertView.
                    findViewById(R.id.img_expandable_list_row_header_img);
        }

        if (!isExpanded) {
            imgHeaderIcon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.icon_expand, null));
        } else {
            imgHeaderIcon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.icon_collapse, null));
            tvHeaderTitle.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public List<String> getChildList() {
        return mChildList;
    }

    public void setChildList(List<String> childList) {
        mChildList = childList;
    }

    public String getSelectedChildTaskName() {
        return mSelectedChildTaskName;
    }

    public void setSelectedChildItemValue(String selectedChildTaskName) {
        mSelectedChildTaskName = selectedChildTaskName;
    }

    public void setGroupTitle(String headerTitle) {
        mHeaderTitle = headerTitle;
    }
}
