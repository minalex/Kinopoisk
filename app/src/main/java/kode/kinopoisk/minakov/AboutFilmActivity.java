package kode.kinopoisk.minakov;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import kode.kinopoisk.minakov.data.FilmShort;

public class AboutFilmActivity extends AppCompatActivity {
    private String sloganText="";
    private String descriptionText = "";
    private String webURLText = "";
    private String directorText = "";
    private ArrayList<String> actors;
    private TextView title;
    private TextView slogan;
    private TextView subtitle;
    private TextView genre;
    private TextView len;
    private TextView description;
    private TextView webURL;
    private TextView rating;
    private TextView director;
    private TextView actor;
    private ImageView posterView;
    private Bitmap mPoster;
    private Button seanceButton;

    public static final int DATA_LOADED = 1;
    public static final int NETWORK_ERROR = -1;
    public static final int PARSER_ERROR = -2;
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            if(message.what == DATA_LOADED){
                slogan.setText(sloganText);
                description.setText("\t"+descriptionText);
                webURL.setText(webURLText);
                director.setText("Режиссер : "+ directorText);
                String acts = "В ролях: ";
                for(String s: actors) acts+= s+"  ";
                actor.setText(acts);
            }else{
                showAlert("Ошибка", "Ошибка :  сетевое соединение отсутствует или ошибка сети");
            }
        }
    };

//--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_film);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  " + KinopoiskAPI.currentFilm.nameRU);
        toolbar.setNavigationIcon(R.mipmap.back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //setSupportActionBar(toolbar);
        initComponents();

        seanceButton = (Button)findViewById(R.id.selectseancebutton);

       seanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AboutFilmActivity.this, SeancesActivity.class);
                AboutFilmActivity.this.startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
    }
//--------------------------------------------------------------------------------------------------
    private void initComponents(){
        actors = new ArrayList<String>();
        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);
        slogan = (TextView) findViewById(R.id.slogan);
        genre = (TextView) findViewById(R.id.about_genre);
        len = (TextView) findViewById(R.id.about_len);
        description = (TextView) findViewById(R.id.description);
        webURL = (TextView) findViewById(R.id.weblink);
        rating = (TextView) findViewById(R.id.rating);
        posterView = (ImageView)findViewById(R.id.poster);
        director = (TextView) findViewById(R.id.director);
        actor = (TextView)findViewById(R.id.actors);

        if(KinopoiskAPI.currentFilm.mPoster != null)
            posterView.setImageBitmap(KinopoiskAPI.currentFilm.mPoster);
        else{
            String posterURL = KinopoiskAPI.currentFilm.posterURL;
            String urlPosterString = "";
            int dotpos = posterURL.lastIndexOf('.');
            if(dotpos < 0)return;
            int fPos = 19;
            if(dotpos <= fPos) return;
            urlPosterString = posterURL.substring(fPos,dotpos);
            urlPosterString = "https://st.kp.yandex.net/images/film_big/"+urlPosterString+".jpg";
            new DownloadImageTask(posterView)
                    .execute(urlPosterString);
        }
        String text = KinopoiskAPI.currentFilm.nameRU;
        if((KinopoiskAPI.currentFilm.nameEN != null) &&(KinopoiskAPI.currentFilm.nameEN.length() >0)) text+= "  ("+KinopoiskAPI.currentFilm.nameEN+")";
        title.setText(text);
        text = "";
        if(KinopoiskAPI.currentFilm.country!= null)text = KinopoiskAPI.currentFilm.country;
        if(KinopoiskAPI.currentFilm.year != 0)text += ",  "+KinopoiskAPI.currentFilm.year;
        subtitle.setText(text);
        text = "Жанр: ";
        if(KinopoiskAPI.currentFilm.genres != null) text += KinopoiskAPI.currentFilm.genres;
        genre.setText(text);
        text = "Продолжительность: ";
        if(KinopoiskAPI.currentFilm.length != null) text += KinopoiskAPI.currentFilm.length+" ч.";
        len.setText(text);
        text = "Рейтинг:   "+KinopoiskAPI.currentFilm.rating;
        rating.setText(text);
        new GetFilm().execute();
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
    private class GetFilm  extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected String doInBackground(Void... params) {
            try {
                String request = KinopoiskAPI.API_BASE_URL+KinopoiskAPI.API_GET_FILM+KinopoiskAPI.currentFilm.id;
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
                if(dataJsonObj.has("slogan")) sloganText = dataJsonObj.getString("slogan");
                if(dataJsonObj.has("description")) descriptionText = dataJsonObj.getString("description");
                if(dataJsonObj.has("webURL")) webURLText = dataJsonObj.getString("webURL");
                JSONArray creators = dataJsonObj.getJSONArray("creators");
                JSONArray dir = creators.getJSONArray(0);
                if(dir != null){
                    JSONObject d =dir.getJSONObject(0);
                    if((d!= null)&&(d.has("nameRU"))) directorText = d.getString("nameRU");
                }
                JSONArray acts = creators.getJSONArray(1);
                if(acts != null) {
                    for(int i =0; i< acts.length(); i++){
                        JSONObject actor = acts.getJSONObject(i);
                        if(actor.has("nameRU")) actors.add(actor.getString("nameRU"));
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
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            mPoster = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                if(mPoster != null) mPoster.recycle();
                System.gc();
                mPoster = BitmapFactory.decodeStream(in);
            }catch(OutOfMemoryError ex){
                mPoster = null;
            } catch (Exception e) {
                mPoster = null;
                e.printStackTrace();
            }
            return mPoster;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                bmImage.setImageBitmap(result);
                bmImage.postInvalidate();
            }
        }
    }
}
