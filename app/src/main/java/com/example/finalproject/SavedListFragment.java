package com.example.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import androidx.appcompat.app.WindowDecorActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SavedListFragment extends Fragment {
    private SQLiteDatabase db;
    private RecyclerView recyclerView;
    private SavedListAdapter savedListAdapter;
    ArrayList<MainActivity.DateImageLink> savedItemList = new ArrayList<>();

    public SavedListFragment() {

    }

    public static SavedListFragment newInstance() {
        return new SavedListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBManager.instanceOfDB(requireContext()).getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_savedlist, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewSavedList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<MainActivity.DateImageLink> savedItemList = getSavedItemsFromDatabase();

        savedListAdapter =  new SavedListAdapter(savedItemList);
        recyclerView.setAdapter(savedListAdapter);
        Log.d("view", String.valueOf(view));

        return view;
    }

    //Retrieve data from database
    private ArrayList<MainActivity.DateImageLink> getSavedItemsFromDatabase() {
        ArrayList<MainActivity.DateImageLink> savedItemList = new ArrayList<>();

        if (db == null || !db.isOpen()) {
            return savedItemList;
        }

        String[] columns = {DBManager.COL_TITLE, DBManager.COL_DATE, DBManager.COL_URL, DBManager.COL_HDURL,DBManager.COL_IMG_NAME};
        Cursor cursor = db.query(DBManager.TABLE_NAME, columns, null, null, null, null, null);
        int titleColumnIndex = cursor.getColumnIndex(DBManager.COL_TITLE);
        int dateColumnIndex = cursor.getColumnIndex(DBManager.COL_DATE);
        int dataUrlColumnIndex = cursor.getColumnIndex(DBManager.COL_URL);
        int hdurlColumnIndex = cursor.getColumnIndex(DBManager.COL_HDURL);
        int imageNameColumnIndex = cursor.getColumnIndex(DBManager.COL_IMG_NAME);


        while (cursor.moveToNext()) {
            String title;

            if(titleColumnIndex != -1){
            title = cursor.getString(titleColumnIndex);
            } else{
                title="Default Title";
            }

            String date = cursor.getString(dateColumnIndex);
            String dataUrl =cursor.getString(dataUrlColumnIndex);
            String hdurl = cursor.getString(hdurlColumnIndex);
            String imageName = cursor.getString(imageNameColumnIndex);




            MainActivity.DateImageLink savedItem = new MainActivity.DateImageLink(title,date, dataUrl, hdurl);
            savedItem.setImageTitle(imageName);
            savedItem.setIsSaved(true);
            savedItemList.add(savedItem);
        }

        cursor.close();
        return savedItemList;
    }


    public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.ViewHolder> {
        private List<MainActivity.DateImageLink> savedItemList;

        public SavedListAdapter(List<MainActivity.DateImageLink> savedItemList) {
            this.savedItemList = savedItemList;
        }

        @Override
        public int getItemCount() {

            return savedItemList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_savedlist, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MainActivity.DateImageLink dateImageLinkItem = savedItemList.get(position);
            holder.titleTextView.setText(dateImageLinkItem.getTitle());
            holder.dateTextView.setText(dateImageLinkItem.getDate());
            holder.urlTextView.setText(dateImageLinkItem.getDataUrl());
            holder.hdurlTextView.setText(dateImageLinkItem.getHdurl());
            holder.imageNameTextView.setText(dateImageLinkItem.getImageTitle());
            Glide.with(requireContext()).load(dateImageLinkItem.getDataUrl()).into(holder.savedImageView);

            holder.itemView.setOnClickListener(v -> {

                showAlertDialog(dateImageLinkItem);
            });

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;
            public TextView dateTextView;
            public TextView urlTextView;
            public TextView hdurlTextView;
            public ImageView savedImageView;
            public TextView imageNameTextView;

            public ViewHolder(View view) {
                super(view);
                titleTextView = view.findViewById(R.id.item_title);
                dateTextView = view.findViewById(R.id.item_date);
                urlTextView = view.findViewById(R.id.item_url);
                hdurlTextView = view.findViewById(R.id.item_hdurl);
                savedImageView= view.findViewById(R.id.item_imageView);
                imageNameTextView = view.findViewById(R.id.item_imageName);
            }
        }
        private void showAlertDialog(MainActivity.DateImageLink dateImageLinkItem) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("Choose an action:")
                    .setNegativeButton("Delete", (dialog, which) -> {

                        deleteFromDatabase(dateImageLinkItem);
                    })
                    .setPositiveButton("Open HDURL", (dialog, which) -> {

                        openHdurlInBrowser(dateImageLinkItem);
                    })

                    .setNeutralButton("Cancel", (dialog, which) -> {

                    });

            builder.create().show();
        }

        private void deleteFromDatabase(MainActivity.DateImageLink dateImageLinkItem) {
            String dateToDelete = dateImageLinkItem.getDate();

            int position = -1;
            for (int i = 0; i < savedItemList.size(); i++) {
                if (savedItemList.get(i).getDate().equals(dateToDelete)) {
                    position = i;
                    break;
                }
            }

            if (position != -1) {
                String[] whereArgs = {dateToDelete};
                db.delete(DBManager.TABLE_NAME, DBManager.COL_DATE + "=?", whereArgs);
                savedItemList.remove(position);

                notifyItemRemoved(position);
            }
        }

        private void openHdurlInBrowser(MainActivity.DateImageLink dateImageLinkItem) {
            String hdurl = dateImageLinkItem.getHdurl();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(hdurl));
            startActivity(browserIntent);

        }
    }
}
