package de.florianm.android.materialsearchview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class SuggestionAdapter extends BaseAdapter {
    private CharSequence[] items;
    private int iconResId;
    private LayoutInflater inflater;

    public SuggestionAdapter() {
        this.iconResId = 0;
    }

    public void setItems(CharSequence[] items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setItemIcon(int id) {
        this.iconResId = id;
        notifyDataSetChanged();
    }

    private LayoutInflater getLayoutInflater(Context context){
        if(null == inflater){
            inflater = LayoutInflater.from(context);
        }

        return inflater;
    }

    @Override
    public int getCount() {
        return null == items ? 0 : items.length;
    }

    @Override
    public CharSequence getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(null == convertView){
            viewHolder = createViewHolder(parent);
            convertView = viewHolder.view;
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CharSequence text = getItem(position);
        viewHolder.setText(text);
        if(0 != iconResId){
            viewHolder.setIcon(iconResId);
        }

        return convertView;
    }

    private ViewHolder createViewHolder(ViewGroup parent){
        View view = getLayoutInflater(parent.getContext()).inflate(R.layout.view_suggestion_item, parent, false);
        return new ViewHolder(view);
    }

    private static class ViewHolder{
        private final View view;
        private final TextView textView;
        private final ImageView icon;

        public ViewHolder(View view) {
            this.view = view;

            textView = (TextView) view.findViewById(android.R.id.text1);
            icon = (ImageView) view.findViewById(android.R.id.icon);
        }

        public void setText(CharSequence text){
            if(null != textView){
                textView.setText(text);
            }
        }

        public void setIcon(int id){
            if(null != icon){
                icon.setImageResource(id);
            }
        }
    }
}
