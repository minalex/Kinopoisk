package kode.kinopoisk.minakov;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.support.v7.app.AlertDialog;

import kode.kinopoisk.minakov.data.City;

public class MainActivity extends AppCompatActivity {
    public static final int DATA_LOADED = 1;
    public static final int NETWORK_ERROR = -1;
    public static final int PARSER_ERROR = -2;
    private  ArrayList<City> citiesList;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            if(message.what == DATA_LOADED){
                mLayoutManager = new LinearLayoutManager(MainActivity.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new MyAdapter(citiesList);
                mRecyclerView.setAdapter(mAdapter);
            }else{
               showAlert("Ошибка :  сетевое соединение отсутствует или ошибка сети");
            }
        }
    };
//--------------------------------------------------------------------------------------------------
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            int index = (Integer)v.getTag(R.id.cityTextView);
            KinopoiskAPI.currentCity = citiesList.get(index);
            Intent intent = new Intent(MainActivity.this, CinemaActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
        }
   };
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Выберите город");
        setSupportActionBar(toolbar);
        mRecyclerView =  (RecyclerView) findViewById(R.id.city_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        citiesList = new ArrayList<City>();
        new GetCitiesList().execute();
        new GetGenresList().execute();
        showAlert("Тест", "Тестовое приложение для KODE. Список городов импортируется, но согласно ТЗ используется только Калининград. В данной версии нет работы с геопозиционирование и картами");
    }
//--------------------------------------------------------------------------------------------------
    private  void showAlert(String message){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Ошибка");
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
//##################################################################################################
    private class GetCitiesList  extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(KinopoiskAPI.API_BASE_URL+KinopoiskAPI.API_GET_CITIES);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            resultJson = buffer.toString();
            } catch (Exception e) {
                handler.sendEmptyMessage(NETWORK_ERROR);
                e.printStackTrace();
            }
            return resultJson;
        }
        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;
            try {
                dataJsonObj = new JSONObject(strJson);
                JSONArray cities = dataJsonObj.getJSONArray("cityData");
                for (int i = 0; i< cities.length(); i++){
                    JSONObject city = cities.getJSONObject(i);
                    String id = city.getString("cityID");
                    String name = city.getString("cityName");
                    citiesList.add(new City(id, name));
                }
                handler.sendEmptyMessage(DATA_LOADED);
            } catch (JSONException e) {
                handler.sendEmptyMessage(PARSER_ERROR);
                e.printStackTrace();
            }
        }
    }
//##################################################################################################
//##################################################################################################
private class GetGenresList  extends AsyncTask<Void, Void, String> {
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(KinopoiskAPI.API_BASE_URL+"getGenres");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            resultJson = buffer.toString();
        } catch (Exception e) {
            handler.sendEmptyMessage(NETWORK_ERROR);
            e.printStackTrace();
        }
        Log.w("GENRES", resultJson);
        return resultJson;
    }
    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);

        JSONObject dataJsonObj = null;
        try {
            dataJsonObj = new JSONObject(strJson);
            JSONArray genres = dataJsonObj.getJSONArray("genreData");
            for (int i = 0; i< genres.length(); i++){
                JSONObject city = genres.getJSONObject(i);

                String name = city.getString("genreName");
               Log.w("GENRE", name);
            }
            handler.sendEmptyMessage(DATA_LOADED);
        } catch (JSONException e) {
            handler.sendEmptyMessage(PARSER_ERROR);
            e.printStackTrace();
        }
    }
}
    private  void showAlert(String title, String message){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

//##################################################################################################
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<City>  mDataset;
        public  class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
            public TextView mTextView;
            public LinearLayout layout;
            public ViewHolder(LinearLayout l) {
                super(l);
                layout = l;
                mTextView = (TextView)l.findViewById(R.id.cityTextView);
            }
        }
        public MyAdapter( ArrayList<City> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_text_view, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout)v);
            v.setOnClickListener(clickListener);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mDataset.get(position).getCityName());
            holder.layout.setTag(R.id.cityTextView, new Integer(position));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
//##################################################################################################
}
