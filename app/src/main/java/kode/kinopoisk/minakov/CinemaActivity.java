package kode.kinopoisk.minakov;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Collections;

import kode.kinopoisk.minakov.data.City;
import kode.kinopoisk.minakov.data.FilmShort;

public class CinemaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int DATA_LOADED = 1;
    public static final int NETWORK_ERROR = -1;
    public static final int PARSER_ERROR = -2;
    private ArrayList<FilmShort> films;
    private ArrayList<FilmShort> sortedFilms;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String sortedGenre = " все жанры";
    private Toolbar toolbar;
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            if(message.what == DATA_LOADED){
                mLayoutManager = new LinearLayoutManager(CinemaActivity.this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                if(sortedFilms.size() == 0){
                    showAlert("Поиск", sortedGenre+": Ничего не найдено");
                    sortedGenre = " все жанры";
                    for(FilmShort film: films) sortedFilms.add(film);
                }
                toolbar.setTitle("Кинопоиск: "+sortedGenre);
                mAdapter = new FilmsAdapter(sortedFilms);
                mRecyclerView.removeAllViews();
                mRecyclerView.setAdapter(mAdapter);

            }else{
                showAlert("Ошибка", "Ошибка :  сетевое соединение отсутствует или ошибка сети");
            }
        }
    };
//--------------------------------------------------------------------------------------------------
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            int index = (Integer)v.getTag(R.id.filmtitle);
            FilmShort  film =sortedFilms.get(index);
            Intent intent = new Intent(CinemaActivity.this, AboutFilmActivity.class);
            KinopoiskAPI.currentFilm = film;
            CinemaActivity.this.startActivity(intent);
        }
    };
    //--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema2);
        initComponents();
        new GetFilmsList().execute();
    }
//--------------------------------------------------------------------------------------------------
    private void initComponents(){
        films = new ArrayList<FilmShort>();
        sortedFilms = new ArrayList<FilmShort>();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Кинопоиск: все жанры");
        toolbar.setOverflowIcon(null);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.cinema_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mRecyclerView =  (RecyclerView) findViewById(R.id.films_recycler_view);
        mRecyclerView.setHasFixedSize(true);
    }
//--------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.filmsortbyrating) {
            sortedFilms.clear();
            for(FilmShort film: films){
                sortedFilms.add(film);
            }
            Collections.sort(sortedFilms);
            sortedGenre= " все жанры по рейтингу";
            handler.sendEmptyMessage(DATA_LOADED);
        } else if (id == R.id.nav_biography) {
            findFilmsByGenre(0);
        } else if (id == R.id.nav_boevik) {
            findFilmsByGenre(1);
        } else if (id == R.id.nav_vestern) {
            findFilmsByGenre(2);
        }else if (id == R.id.nav_voen) {
            findFilmsByGenre(3);
        }else if (id == R.id.nav_detective) {
            findFilmsByGenre(4);
        }else if (id == R.id.nav_child) {
            findFilmsByGenre(5);
        }else if (id == R.id.nav_documental) {
            findFilmsByGenre(6);
        }else if (id == R.id.nav_drama) {
            findFilmsByGenre(7);
        }else if (id == R.id.nav_comedy) {
            findFilmsByGenre(8);
        }else if (id == R.id.nav_shortfilm) {
            findFilmsByGenre(9);
        }else if (id == R.id.nav_crime) {
            findFilmsByGenre(10);
        }else if (id == R.id.nav_melo) {
            findFilmsByGenre(11);
        }else if (id == R.id.nav_music) {
            findFilmsByGenre(12);
        }else if (id == R.id.nav_cartoon) {
            findFilmsByGenre(13);
        }else if (id == R.id.nav_musicle) {
            findFilmsByGenre(14);
        }else if (id == R.id.nav_advernture) {
            findFilmsByGenre(15);
        }else if (id == R.id.nav_family) {
            findFilmsByGenre(16);
        }else if (id == R.id.nav_triller) {
            findFilmsByGenre(17);
        }else if (id == R.id.nav_horror) {
            findFilmsByGenre(18);
        }else if (id == R.id.nav_fantastic) {
            findFilmsByGenre(19);
        }else if (id == R.id.nav_fantasy) {
            findFilmsByGenre(20);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
//--------------------------------------------------------------------------------------------------
    void findFilmsByGenre(int genre){
        String genr = KinopoiskAPI.genres[genre];
        sortedGenre = genr ;
        sortedFilms.clear();
        for(FilmShort film: films){
            if(film.genres.contains(genr)){
                sortedFilms.add(film);
            }
        }
        Collections.sort(sortedFilms);
        handler.sendEmptyMessage(DATA_LOADED);
    }
    //##################################################################################################
    private class GetFilmsList  extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected String doInBackground(Void... params) {
            try {
                films.clear();
                String request = KinopoiskAPI.API_BASE_URL+KinopoiskAPI.API_GET_TODAY_FILMS+"date="+(String)DateFormat.format("dd.MM.yyyy", new java.util.Date())+"&cityID=490";
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
                JSONArray filmList = dataJsonObj.getJSONArray("filmsData");
                for (int i = 0; i< filmList.length(); i++){
                    JSONObject filmJSON = filmList.getJSONObject(i);
                    FilmShort film = new FilmShort(filmJSON);
                    if(film.initialized){
                        films.add(film);
                        sortedFilms.add(film);
                    }
                    Log.w("TAG", film.toString());
                }
                handler.sendEmptyMessage(DATA_LOADED);
            } catch (JSONException e) {
                handler.sendEmptyMessage(PARSER_ERROR);
                e.printStackTrace();
            }
        }
    }
//##################################################################################################
    public class FilmsAdapter extends RecyclerView.Adapter<FilmsAdapter.ViewHolder> {
        private ArrayList<FilmShort>  mDataset;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;
            public TextView title;
            public TextView subtitle;
            public TextView genre;
            public TextView len;
            public TextView premiere;
            public TextView rating;
            public ImageView poster;
            public ViewHolder(LinearLayout l) {
                super(l);
                layout = l;
                title = (TextView)l.findViewById(R.id.filmtitle);
                subtitle = (TextView)l.findViewById(R.id.filmsubtitle);
                genre = (TextView)l.findViewById(R.id.filmgenre);
                len = (TextView)l.findViewById(R.id.filmlen);
                premiere = (TextView)l.findViewById(R.id.filmpremier);
                rating = (TextView)l.findViewById(R.id.filmrating);
                poster = (ImageView)l.findViewById(R.id.poster);
            }
        }
        public FilmsAdapter( ArrayList<FilmShort> myDataset) {
        mDataset = myDataset;
    }

        @Override
        public FilmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.film_short_view, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout)v);
            v.setOnClickListener(clickListener);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String name = mDataset.get(position).nameRU;
            if((mDataset.get(position).nameEN!= null)&&(mDataset.get(position).nameEN.length()>0)) name +="  ("+mDataset.get(position).nameEN+")";
            holder.title.setText(name);
            name = "";
            if(mDataset.get(position).country!= null)name = mDataset.get(position).country;
            if(mDataset.get(position).year != 0)name += ", "+mDataset.get(position).year;
            holder.subtitle.setText(name);
            name = "Жанр: ";
            if(mDataset.get(position).genres != null) name += mDataset.get(position).genres;
            holder.genre.setText(name);
            name = "Продолжительность: ";
            if(mDataset.get(position).length != null) name += mDataset.get(position).length+" ч.";
            holder.len.setText(name);
            name = "рейтинг: "+mDataset.get(position).rating;
            holder.rating.setText(name);
            name = "Премьера: ";
            if(mDataset.get(position).premiere!= null) name += mDataset.get(position).premiere;
            holder.premiere.setText(name);
            mDataset.get(position).loadPoster(holder.poster);
            holder.layout.setTag(R.id.filmtitle, new Integer(position));
        }
        @Override
        public int getItemCount() {
        return mDataset.size();
    }
}
//##################################################################################################
}

