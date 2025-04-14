package com.fetch.rewards;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constants for header and item view types
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // List to hold all the list items
    private List<MainActivity.ListItem> items;

    // Listener for handling item clicks
    private final OnItemClickListener listener;
    private static final String TAG = "ItemAdapter";

    // Interface to handle item and header click events
    public interface OnItemClickListener {
        void onItemClicked(MainActivity.Item item);
        void onHeaderClicked(int headerId);
    }

    // Constructor for the ItemAdapter
    public ItemAdapter(List<MainActivity.ListItem> items, OnItemClickListener listener) {
        this.items = items;    // Initialize the items list
        this.listener = listener;   // Initialize the listener
    }


    // Determines the view type (header or item) based on the position
    @Override
    public int getItemViewType(int position) {
        // Return TYPE_HEADER if the item at the position is a header; otherwise, return TYPE_ITEM
        return items.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    // Returns the total number of items in the list
    @Override
    public int getItemCount() {
        return items.size();
    }


    // Creates a new ViewHolder depending on the view type (header or item)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            // Inflate the header layout if the view type is TYPE_HEADER
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);   // Return a HeaderViewHolder
        } else {
            // Inflate the item layout if the view type is TYPE_ITEM
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            return new ItemViewHolder(view);   // Return an ItemViewHolder
        }
    }

    // Binds the data to the ViewHolder

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // Get the ListItem at the current position
        MainActivity.ListItem listItem = items.get(position);

        // Check if the ViewHolder is of type HeaderViewHolder
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            // Set the header text based on the list item
            headerHolder.headerText.setText("List ID: " + listItem.getHeaderId());
            Log.d(TAG, "List Header ID " +  listItem.getHeaderId());

            // Set up the click listener for the header view
            headerHolder.itemView.setOnClickListener(v -> listener.onHeaderClicked(listItem.getHeaderId()));

        } else if (holder instanceof ItemViewHolder) {

            // If the ViewHolder is of type ItemViewHolder, cast it
            MainActivity.Item item = listItem.getItem();   // Get the item data
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            // Set the name of the item to the TextView
            itemHolder.nameText.setText(item.getName());
            Log.d(TAG, "Display the Item " +  listItem.getHeaderId());

            // Set up the click listener for the item view
            itemHolder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        }
    }

    // Updates the list of items and notifies the adapter of data changes
    public void updateList(List<MainActivity.ListItem> newItems) {
        this.items = newItems;  // Update the items list
        notifyDataSetChanged();   // Notify the adapter to refresh the view
        Log.d(TAG, "List updated. New visible items count: " +  this.items.size());
    }

    // ViewHolder for header views
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText; // TextView to display the header text

        // Constructor for HeaderViewHolder
        public HeaderViewHolder(View view) {
            super(view);
            headerText = view.findViewById(R.id.headerText);
        }
    }

    // ViewHolder for item views
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;   // TextView to display the item name

        public ItemViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
        }
    }
}


