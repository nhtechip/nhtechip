package com.jmartin.temaki.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.jmartin.temaki.R;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;

import java.util.ArrayList;

/**
 * Created by jeff on 2013-08-24.
 */
public class ListItemsAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final ArrayList<TemakiItem> data;
    private  ArrayList<TemakiItem> filteredData;
    private int selectedItemPosition;

    public ListItemsAdapter(Context context, ArrayList<TemakiItem> items) {
        this.context = context;
        this.data = items;
        this.filteredData = items;
        this.selectedItemPosition = -1;
    }

    public void setSelectedItemPosition(int position) {
        this.selectedItemPosition = position;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Custom indexOf for ArrayList<String> listItems so we can do case-insensitive comparisons
     * within indexOf.
     */
    public int indexOfItem(String item) {
        for (int i = 0; i < this.filteredData.size(); i++) {
            if (this.filteredData.get(i).getText().equalsIgnoreCase(item))
                return i;
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String theme = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_theme_key), "");

        View row = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView rowTextView = (TextView) row.findViewById(R.id.main_list_item);

        SharedPreferences prefMgr = PreferenceManager.getDefaultSharedPreferences(context);

        TemakiItem item = (TemakiItem) getItem(position);
        String itemText = item.getText();

        // If user wants to force auto-capitalization, make sure first letters are capitalized
        if (prefMgr.getBoolean(Constants.KEY_PREF_LIST_ITEMS_CAPITALIZE_OPTION, true)) {
            itemText = itemText.substring(0, 1).toUpperCase() + itemText.substring(1);
        }

        rowTextView.setText(itemText);

        if (item.isHighlighted()) {
            rowTextView.setTypeface(null, Typeface.BOLD);
            rowTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_highlight, 0);
        } else {
            rowTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            rowTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        // Set colours according to app theme
        if (!theme.equals("")) {
            if (theme.equals(context.getString(R.string.theme_dark))) {
                rowTextView.setTextColor(context.getResources().getColor(android.R.color.white));
                row.setBackgroundResource(R.drawable.main_list_item_dark);
            } else {
                rowTextView.setTextColor(context.getResources().getColor(R.color.dark_grey));
                row.setBackgroundResource(R.drawable.main_list_item);
            }
        }

        // Item is marked as finished
        if (item.isFinished()) {
            rowTextView.setPaintFlags(rowTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            rowTextView.setTextColor(context.getResources().getColor(R.color.light_grey));
        } else {
            rowTextView.setPaintFlags(rowTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Make sure the selection acts properly when scrolling
        if (position == selectedItemPosition) {
            row.setBackgroundResource(R.drawable.main_list_item_selected);
            rowTextView.setTextColor(context.getResources().getColor(R.color.light_grey));
        }

        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults searchResults = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    searchResults.values = data;
                    searchResults.count = data.size();
                } else {
                    ArrayList<TemakiItem> searchResultsData = new ArrayList<TemakiItem>();

                    for (TemakiItem item : data) {
                        if (item.getText().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            searchResultsData.add(item);
                        }
                    }

                    searchResults.values = searchResultsData;
                    searchResults.count = searchResultsData.size();
                }
                return searchResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<TemakiItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
