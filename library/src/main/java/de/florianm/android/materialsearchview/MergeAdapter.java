package de.florianm.android.materialsearchview;

import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;

public class MergeAdapter extends BaseAdapter {
    private ArrayList<AdapterEntry> adapterEntries = new ArrayList<>();

    public void addAdapter(@NonNull ListAdapter adapter, boolean active) {
        adapterEntries.add(new AdapterEntry(adapter, active));
        adapter.registerDataSetObserver(new CascadeDataSetObserver());
        notifyDataSetChanged();
    }

    public void addAdapter(@NonNull ListAdapter adapter) {
        addAdapter(adapter, true);
    }

    public void setActive(@NonNull ListAdapter adapter, boolean active) {
        for (AdapterEntry entry : adapterEntries) {
            if(entry.adapter.equals(adapter)) {
                if (active != entry.active) {
                    entry.active = active;
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                if (!entry.adapter.areAllItemsEnabled()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                int size = entry.adapter.getCount();

                if (size > position) {
                    return entry.adapter.isEnabled(position);
                }

                position -= size;
            }
        }

        return false;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                count += entry.adapter.getCount();
            }
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                int size = entry.adapter.getCount();

                if (size > position) {
                    return entry.adapter.getItem(position);
                }

                position -= size;
            }
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                int size = entry.adapter.getCount();

                if (size > position) {
                    return entry.adapter.getItemId(position);
                }

                position -= size;
            }
        }

        return -1;
    }

    @Override
    public boolean hasStableIds() {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                if (!entry.adapter.hasStableIds()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                int size = entry.adapter.getCount();

                if (size > position) {
                    return entry.adapter.getView(position, convertView, parent);
                }

                position -= size;
            }
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        int typeOffset = 0;

        for (AdapterEntry entry : adapterEntries) {
            if (entry.active) {
                int size = entry.adapter.getCount();

                if (size < position) {
                    return typeOffset + entry.adapter.getItemViewType(position);
                }

                position -= size;
            }

            typeOffset += entry.adapter.getViewTypeCount();
        }

        return -1;
    }

    @Override
    public int getViewTypeCount() {
        int count = 0;
        for (AdapterEntry entry : adapterEntries) {
            count += entry.adapter.getViewTypeCount();
        }

        return Math.max(0, count);
    }

    @Override
    public boolean isEmpty() {
        return 0 < getCount();
    }

    private class AdapterEntry {
        ListAdapter adapter;
        boolean active;

        public AdapterEntry(ListAdapter adapter, boolean active) {
            this.adapter = adapter;
            this.active = active;
        }
    }

    private class CascadeDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    }
}
