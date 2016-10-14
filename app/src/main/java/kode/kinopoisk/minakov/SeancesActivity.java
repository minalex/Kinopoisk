package kode.kinopoisk.minakov;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import kode.kinopoisk.minakov.data.Cinema;
import kode.kinopoisk.minakov.data.City;
import kode.kinopoisk.minakov.data.FilmShort;

public class SeancesActivity extends AppCompatActivity {

    public static final int DATA_LOADED = 1;
    public static final int NETWORK_ERROR = -1;
    public static final int PARSER_ERROR = -2;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Cinema> cinemas;
//--------------------------------------------------------------------------------------------------
private Handler handler = new Handler(){
    public void handleMessage(Message message){
        if(message.what == DATA_LOADED){
            mLayoutManager = new LinearLayoutManager(SeancesActivity.this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new SeancesAdapter(cinemas);
            mRecyclerView.removeAllViews();
            mRecyclerView.setAdapter(mAdapter);
            if(cinemas.size() == 0)
                showAlert("Кинотеатры", "Ничего не найдено");
        }else{
            showAlert("Ошибка", "Ошибка :  сетевое соединение отсутствует или ошибка сети");
        }
    }
};
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seances);
        cinemas = new ArrayList<Cinema>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  " + KinopoiskAPI.currentFilm.nameRU + " : сеансы");
        toolbar.setNavigationIcon(R.mipmap.back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //setSupportActionBar(toolbar);
        //initComponents();
        mRecyclerView =  (RecyclerView) findViewById(R.id.seanses_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        new GetSeancesList().execute();
    }
//--------------------------------------------------------------------------------------------------
    private  void showAlert(String title, String message){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
//##################################################################################################
    private class GetSeancesList  extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected String doInBackground(Void... params) {
            try {
                String request = KinopoiskAPI.API_BASE_URL+KinopoiskAPI.API_GET_SEANSES+"filmID="+KinopoiskAPI.currentFilm.id+ "&cityID=490"+"&date="+(String) DateFormat.format("dd.MM.yyyy", new java.util.Date());
                URL url = new URL(request);
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
                JSONArray cinemaData = dataJsonObj.getJSONArray("items");
                for (int i = 0; i< cinemaData.length(); i++){
                    JSONObject cdata = cinemaData.getJSONObject(i);
                    Cinema cinema = new Cinema(cdata);
                    if(cinema.initialized) {
                        cinemas.add(cinema);
                    }
                }
                handler.sendEmptyMessage(DATA_LOADED);
            } catch (JSONException e) {
                handler.sendEmptyMessage(PARSER_ERROR);
                e.printStackTrace();
            }
        }
    }
    //##################################################################################################
    public class SeancesAdapter extends RecyclerView.Adapter<SeancesAdapter.ViewHolder> {
        private ArrayList<Cinema>  mDataset;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;
            public TextView cinematitle;
            public TextView address;
            public TextView seances_all;

            public ViewHolder(LinearLayout l) {
                super(l);
                layout = l;
                cinematitle = (TextView)l.findViewById(R.id.cinematitle);
                address = (TextView)l.findViewById(R.id.cinemaaddress);
                seances_all = (TextView)l.findViewById(R.id.seances);
            }
        }
        public SeancesAdapter( ArrayList<Cinema> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public SeancesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cinema_view, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout)v);
           //v.setOnClickListener(clickListener);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.cinematitle.setText( mDataset.get(position).name);
            holder.address.setText(mDataset.get(position).address);
            String seanceList = "";
            for(int i = 0; i < mDataset.get(position).seanses.size(); i++ ){
                seanceList+=" "+ mDataset.get(position).seanses.get(i);
            }
            holder.seances_all.setText("Сеансы: "+seanceList);
            holder.layout.setTag(R.id.cinematitle, new Integer(position));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
//##################################################################################################
}
