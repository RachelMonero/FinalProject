
package com.example.finalproject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar tBar;
    DrawerLayout drawer;
    NavigationView navigationView;
    ListView listView;
    ArrayList<DateImageLink> dateImageLinkList;
    ArrayList<String> source = new ArrayList<>(Arrays.asList());
    static String newSourceAddress;
    static String selectedDate;


    //DateImageLink class for holding data
    public static class DateImageLink implements Serializable {
        private String date;
        private String dataUrl;
        private String hdurl;
        private String title;
        private String imageTitle;
        private boolean isSaved;

        //Initiating DataImageLink
        public DateImageLink(String title, String date, String dataUrl, String hdurl){
            this.date = date;
            this.dataUrl = dataUrl;
            this.hdurl=hdurl;
            this.title= title;
            this.imageTitle = imageTitle;
            this.isSaved =false;
        }

        public boolean isSaved(){

            return isSaved;
        }
        public void setIsSaved(boolean isSaved){

            this.isSaved = isSaved;
        }
        public String getDate(){

            return date;
        }
        public String getDataUrl(){

            return dataUrl;
        }
        public String getHdurl(){

            return hdurl;
        }

        public String getTitle() {

            return title;
        }
        public void setTitle(String title){

            this.title = title;
        }
        public String getImageTitle() {

            return imageTitle;
        }
        public void setImageTitle(String imageTitle){

            this.imageTitle = imageTitle;
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);

        tBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = findViewById(R.id.listView);

        dateImageLinkList = new ArrayList<>();

        //Set onClickListener for the refresh button
        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        VisitCount.countVisitPlusOne(this);
        int count = VisitCount.getVisitCount(this);
        String message = "visit: "+count;

        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();




        //HTTP request to fetch data from NASA
        MyHTTPRequest req = new MyHTTPRequest();
        if(selectedDate == null) {

            req.execute("https://api.nasa.gov/planetary/apod?api_key=MShM4oqqqOsNJQd44h7M4S0WMXEEDOWFCXUUvgpj");
        } else if(selectedDate != null){
            req.execute(newSourceAddress);

        }
        //Set OnItemClickListener for the listView
        listView.setOnItemClickListener((parent,view,position,id)-> {
            DateImageLink selectedDateImageLink = dateImageLinkList.get(position);

            if(selectedDateImageLink != null){
                DetailsListViewFragment detailsListViewFragment = new DetailsListViewFragment();
                Bundle dataToPass = new Bundle();
                dataToPass.putString("date", selectedDateImageLink.getDate());
                dataToPass.putString("dataUrl", selectedDateImageLink.getDataUrl());
                dataToPass.putString("hdurl", selectedDateImageLink.getHdurl());
                dataToPass.putString("imageTitle", selectedDateImageLink.getImageTitle());

                detailsListViewFragment.setArguments(dataToPass);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.placeholder, detailsListViewFragment,"DetailsFragmentTag")
                        .addToBackStack(null)
                        .commit();
                Log.d("FragmentTransaction","Transaction committed");

            }
        });
    }

    //Methods - Open Detail page (fragment)
    private void openDetailsFragment(DateImageLink dateImageLink){
        Fragment detailsFragment = new DetailsListViewFragment();
        Bundle args = new Bundle();
        args.putSerializable("dateImageLink", dateImageLink);
        detailsFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.placeholder,detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    //HTTP request and AsyncTask
    private class MyHTTPRequest extends AsyncTask< String, Integer, List<DateImageLink>> {

        @Override
        protected List<DateImageLink> doInBackground(String... urls) {
            try {
                String url = urls[0];
                //Connection to the URL
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                //Retrieve inputStream
                InputStream inStrm = urlConnection.getInputStream();

                //Convert InputStream result to String
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStrm));
                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                String result = buffer.toString();

                Log.d("MyHTTPRequest", "Received JSON: " + result);

                JSONObject jsonObject = new JSONObject(result);

                //Parse JSON data to extract information of date,url,hdurl,title
                String date = jsonObject.getString("date");
                String dataUrl = jsonObject.getString("url");
                String hdurl = jsonObject.getString("hdurl");
                String imageTitle = jsonObject.getString("title");


                //Create dateImageLinkObject that contains extracted information
                DateImageLink dateImageLinkObject = new DateImageLink(null,date, dataUrl, hdurl);
                dateImageLinkObject.setTitle(null);
                dateImageLinkObject.setImageTitle(imageTitle);

                //Create a list that contains DataImageLinkObject
                List<DateImageLink> dateImageLinkListSet = new ArrayList<>();
                dateImageLinkListSet.add(dateImageLinkObject);

                return dateImageLinkListSet;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer ... values){

        }
        protected void onPostExecute(List<DateImageLink> dateImageLinkListSet) {
            if (dateImageLinkListSet != null) {
                //Update dateImageLinkList
                dateImageLinkList = new ArrayList<>(dateImageLinkListSet);
                Log.d("MyHTTPRequest", "Received " + dateImageLinkList.size() + " items");

                BaseAdapter adapter = new MyAdapter();
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            } else {
                Log.d("MyHTTPRequest", "Received empty list");
            }
        }

        public ArrayList<DateImageLink> getDateImageLinkList(){

            return dateImageLinkList;
        }
    }
    private class MyAdapter extends BaseAdapter{
        public int getCount(){
            return dateImageLinkList.size();
        }
        public Object getItem(int position){
            return dateImageLinkList.get(position);
        }
        public long getItemId(int position){
            return position;
        }
        private class ViewHolder {
            ImageView thumbnailImageView;
            TextView dateTextView;
            TextView urlTextView;
            TextView hdurlTextView;
        }
        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.activity_listview_item,null);
                holder = new ViewHolder();
                holder.thumbnailImageView = convertView.findViewById(R.id.thumbnailImageView);
                holder.dateTextView = convertView.findViewById(R.id.dateView);
                holder.urlTextView = convertView.findViewById(R.id.urlView);
                holder.hdurlTextView = convertView.findViewById(R.id.hdurlView);
                convertView.setTag(holder);

            } else{
                holder = (ViewHolder) convertView.getTag();
            }
            DateImageLink dateImageLink = dateImageLinkList.get(position);

            Glide.with(MainActivity.this).load(dateImageLink.getDataUrl()).into(holder.thumbnailImageView);

            holder.dateTextView.setText(dateImageLink.getDate());
            holder.urlTextView.setText(dateImageLink.getDataUrl());
            holder.hdurlTextView.setText(dateImageLink.getHdurl());
            return convertView;
        }

        }

    //Menu Items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    //Create onOptionsItemSelected for menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.savedItem) {

            replaceFragment(new SavedListFragment());

        } else if (id == R.id.pickDate) {

            showDatePickerDialog();

          }
        return true;
    }

    // Create new instance of DatePickerFragment and show it
    private void showDatePickerDialog() {

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment

            implements DatePickerDialog.OnDateSetListener {

        // Use the current date as the default date in the picker.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create new DatePickerDialog and return it.
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            //Set Source Address to Selected Date.
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            newSourceAddress ="https://api.nasa.gov/planetary/apod?api_key=MShM4oqqqOsNJQd44h7M4S0WMXEEDOWFCXUUvgpj&date="+selectedDate;

            // Start new MainActivity to refresh the page
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            // Close the current activity
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    //Create onNavigationItemSelected for Navigation Bar Items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String message = null;

        int id = item.getItemId();
        if (id == R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            finish();

        } else if (id == R.id.help) {
            // Set AlertDialog on help Item.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.help_context1)
                    .setTitle(R.string.help_title);
            // Set positive button on AlertDialog
            builder.setPositiveButton(R.string.ok, (dialog,which) -> {

                    //Toast message
                    Toast.makeText(MainActivity.this, "Awesome!", Toast.LENGTH_SHORT).show();

            });
            AlertDialog dialog =  builder.create();
            dialog.show();

        } else if (id == R.id.exit) {
            finishAffinity();
        }

        return false;
    }

    protected void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.placeholder, fragment);
        transaction.commit();
        drawer.closeDrawers();
    }


}