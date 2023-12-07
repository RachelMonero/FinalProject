package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.DropBoxManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

public class DetailsListViewFragment extends Fragment {

    private MainActivity.DateImageLink selectedDateImageLink;
    private AppCompatActivity parentActivity;
    SQLiteDatabase db;
    EditText userInput;
    ProgressBar progressBar;


    public DetailsListViewFragment() {

    }

    //Create View for Detail Page for the List
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d("DetailsListViewFragment","onCreateView");
       View result = inflater.inflate(R.layout.fragment_details_list_view, container,false);
       progressBar = result.findViewById(R.id.progressBar);
       Bundle args = getArguments();

       if(args != null){

           String date = args.getString("date");
           String dataUrl = args.getString("dataUrl");
           String hdurl = args.getString("hdurl");
           String imageTitle = args.getString("imageTitle");

           Log.d("ImageTitle",imageTitle);

           TextView content_date = (TextView) result.findViewById(R.id.content_date);
           TextView content_dataUrl = (TextView) result.findViewById(R.id.content_data_url);
           TextView content_hdurl = (TextView) result.findViewById(R.id.content_hdurl);
           ImageView content_imageView = (ImageView) result.findViewById(R.id.content_imageView);
           TextView content_imageName = (TextView) result.findViewById(R.id.content_imageName);

           content_date.setText(date);
           content_dataUrl.setText(dataUrl);
           content_hdurl.setText(hdurl);
           content_imageName.setText(imageTitle);

           Glide.with(requireContext()).load(dataUrl).into(content_imageView);

           //Set onClickListener on hdurl.
           content_hdurl.setOnClickListener(view -> {

               // Lead User to the Hurl Page in the Browser.
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(hdurl));
               startActivity(browserIntent);
           });

           Button saveButton = result.findViewById(R.id.savebutton);
           userInput = (EditText) result.findViewById(R.id.userInputTitle);


           //Create setOnClickListener for saveButton
           saveButton.setOnClickListener(view -> {

                       progressBar.setVisibility(View.VISIBLE);

                       // This Setup is only to show users the progress bar little longer.
                       new Thread(() -> {

                           try {
                               Thread.sleep(2000); // Set to sleep 2 seconds
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }

                           // Hide ProgressBar after the it is completed
                           parentActivity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                       }).start();

                       //Retrieved user Input from EditText & Set it as title
               String title = userInput.getText().toString();


               Log.d("title", title);
               saveToDatabase(title, date, dataUrl, hdurl,imageTitle);

               //Set snackbar message
               Snackbar snackbar = Snackbar.make(result,"Saving to the List",Snackbar.LENGTH_SHORT);
               snackbar.show();

                   Log.d("SaveBottonOnClick", "Saved");

           });
       }
       return result;
    }
    // Check if Data is in Database
    private boolean isSavedData(String date){
        try{
            if(db == null){
                db = DBManager.instanceOfDB(requireContext()).getReadableDatabase();
            }
            String[] targetColumn = {
                    DBManager.COL_DATE
            };
            String doFind = DBManager.COL_DATE+ " = ?";
            String[] target ={
                    date
            };
            Cursor cursor = db.query( DBManager.TABLE_NAME,targetColumn,doFind,target,null,null,null);
            boolean dataInDB = cursor.getCount() >0;
            cursor.close();
            return dataInDB;

        } catch (Exception e){
            return false;
        }

    }

    //Create Method to Save data to database.
    private void saveToDatabase(String title, String date, String dataUrl, String hdurl, String imageTitle) {

        //Check if it's saved data or not.
        if(!isSavedData(date)){

        Log.d("SaveToDatabase", "Saving to database");
        Log.d("SaveToDatabase", title);

        //Save data to database
        try {
            if (db == null) {
                db = DBManager.instanceOfDB(requireContext()).getWritableDatabase();
            }
            ContentValues values = new ContentValues();
            values.put(DBManager.COL_TITLE, title);
            values.put(DBManager.COL_DATE, date);
            values.put(DBManager.COL_URL, dataUrl);
            values.put(DBManager.COL_HDURL, hdurl);
            values.put(DBManager.COL_IMG_NAME,imageTitle);

            long result = db.insert(DBManager.TABLE_NAME, null, values);

            if (result == -1) {
                Log.e("Database", "Error inserting data into the database");
            } else {
                Log.d("Database", "Data inserted successfully");
            }
        } catch (Exception e) {
            Log.e("Database", "Exception during database operation: " + e.getMessage());
        }
    } else {
            // Toast message to notify user.
            Toast.makeText(requireContext(), "You've Saved this Image, already.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Close the database when the view is destroyed
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        parentActivity = (AppCompatActivity) context;
    }
}