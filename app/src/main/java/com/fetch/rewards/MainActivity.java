package com.fetch.rewards;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<ListItem> allItems;
    private Map<Integer, Boolean> expandedState = new LinkedHashMap<>();
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RecyclerView and set up the layout manager
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(recyclerView);

        new Thread(() -> {
            try {
                allItems = fetchAndGroupItems();
                runOnUiThread(() -> {

                    // Set up the adapter
                    adapter = new ItemAdapter(buildVisibleList(), this);
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "Setting up RecyclerView with adapter");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Loads the JSON data from the Url, parses it, and filters it.
     */
    private List<ListItem> fetchAndGroupItems() throws Exception {

        // Define the URL to fetch the JSON data from
        URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");

        // Open a connection to the URL and set the request method to GET
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Log the URL being fetched
        Log.d(TAG, "Fetching data from URL..." + url);

        // Create a BufferedReader to read the response from the server
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();


        // Read the entire response line by line and append it to the response StringBuilder
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // Close the BufferedReader once data is read
        reader.close();

        // Parse the JSON response into a JSONArray
        JSONArray jsonArray = new JSONArray(response.toString());
        Log.d(TAG, "Received " + jsonArray.length() + " items from server");

        // Loop through the JSON array and extract individual item details
        List<Item> itemList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            // Extract item details from the JSON object
            int id = obj.getInt("id");
            int listId = obj.getInt("listId");
            String name = obj.optString("name");

            // If the name is not null, blank, or equal to the string "null", add it to the itemList
            if (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("null")) {
                itemList.add(new Item(id, listId, name));
            }
        }

        // Sort the items first by listId, and then by the item name
        Collections.sort(itemList, Comparator.comparingInt(Item::getListId).thenComparing(Item::getName));
        Log.d(TAG, "Sorted items by listId and name");

        // Create a map to group the items by their listId
        Map<Integer, List<Item>> groupedMap = new LinkedHashMap<>();

        // Group the items into a map where the key is the listId and the value is a list of items
        for (Item item : itemList) {
            groupedMap.computeIfAbsent(item.getListId(), k -> new ArrayList<>()).add(item);
        }

        // Create a list to store the display items (both headers and items)
        List<ListItem> displayList = new ArrayList<>();

        // For each entry in the grouped map, create a ListItem for the header (listId) and the item
        for (Map.Entry<Integer, List<Item>> entry : groupedMap.entrySet()) {
            int listId = entry.getKey();

            // Set the default expanded state for the section (false - collapsed)
            expandedState.put(listId, false); // default expanded

            // Add a ListItem representing the header (group) for this listId
            displayList.add(new ListItem(listId)); // Header

            // Add the individual items belonging to this listId to the displayList
            for (Item item : entry.getValue()) {
                displayList.add(new ListItem(item));
            }
        }

        // Return the display list which contains both the headers and items
        return displayList;
    }


    /**
     * Filters the visible items based on the expanded sections.
     * Only the expanded sections or headers will be visible.
     */
    private List<ListItem> buildVisibleList() {
        List<ListItem> visibleList = new ArrayList<>();
        Integer currentHeader = null;

        for (ListItem item : allItems) {
            if (item.isHeader()) {
                // Always show headers, and items belonging to expanded sections
                currentHeader = item.getHeaderId();
                visibleList.add(item);
            } else if (currentHeader != null && expandedState.getOrDefault(currentHeader, false)) {
                visibleList.add(item);
            }
        }
        return visibleList;
    }

    @Override
    public void onItemClicked(Item item) {

        // Handle item click
        Toast.makeText(this, "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Item clicked in MainActivity: " + item.getName());
    }

    @Override
    public void onHeaderClicked(int headerId) {

        // Handle header click
        expandedState.put(headerId, !expandedState.get(headerId));
        adapter.updateList(buildVisibleList());
        Log.d(TAG, "Header clicked in MainActivity: ListId " + headerId);
    }

    // Nested classes
    static class Item {
        private final int id;
        private final int listId;
        private final String name;

        // Getters and setters...
        public Item(int id, int listId, String name) {
            this.id = id;
            this.listId = listId;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public int getListId() {
            return listId;
        }

        public String getName() {
            return name;
        }
    }

    // This represents an item grouped by ListId (can be a header or an item)
    static class ListItem {
        private final boolean isHeader;
        private final int headerId;
        private final Item item;

        public ListItem(int headerId) {
            this.isHeader = true;
            this.headerId = headerId;
            this.item = null;
        }

        public ListItem(Item item) {
            this.isHeader = false;
            this.headerId = -1;
            this.item = item;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public int getHeaderId() {
            return headerId;
        }

        public Item getItem() {
            return item;
        }
    }
}



