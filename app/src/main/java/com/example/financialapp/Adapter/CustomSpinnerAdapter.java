package com.example.financialapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.financialapp.R;

public class CustomSpinnerAdapter extends BaseAdapter {
    Context context;
    String[] categories;
    int icons[];

    public CustomSpinnerAdapter(Context context, String[] categories, int[] icons) {
        this.context = context;
        this.categories = categories;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return categories.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_item, viewGroup, false);
        ImageView icon = rootView.findViewById(R.id.imageViewSpinner);
        TextView category = rootView.findViewById(R.id.textViewSpinner);
        icon.setImageResource(icons[i]);
        category.setText(categories[i]);
        return rootView;
    }
}
