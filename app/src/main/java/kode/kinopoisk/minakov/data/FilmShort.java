package kode.kinopoisk.minakov.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Created by Alex on 12.10.2016.
 */
// хранит информацию о фильме
public class FilmShort implements Comparable<FilmShort>{
    public String type;
    public int id;
    public String nameRU;
    public String nameEN;
    public int year;
    public int hallCount;
    public boolean is3d;
    public boolean isNew;
    private String ratingString;
    public double rating;
    public String posterURL;
    public String length;
    public String country;
    public String genres;
    public String premiere;
    public boolean initialized = false;
    public Bitmap mPoster ;
    public boolean posterLoaded = false;
    public ImageView posterContainer = null;
//--------------------------------------------------------------------------------------------------
    public FilmShort(JSONObject object){
        initialized = false;
        try {
            type = object.getString("type");
            id = Integer.parseInt(object.getString("id"));
            nameRU = object.getString("nameRU");
            nameEN = object.getString("nameEN");
            year = Integer.parseInt(object.getString("year"));
            hallCount = Integer.parseInt(object.getString("cinemaHallCount"));

            if(object.has("is3D")) {
                if (object.getInt("is3D") == 1) is3d = true;
            }
            if(object.has("isNew")) {
                if (object.getInt("isNew") == 1) isNew = true;
            }
            ratingString = object.getString("rating");
            StringTokenizer r = new StringTokenizer( ratingString, " ", false);
            if(r.hasMoreTokens()) {
                rating = Double.parseDouble(r.nextToken());
            }
            posterURL = object.getString("posterURL");
            length = object.getString("filmLength");
            country = object.getString("country");
            genres = object.getString("genre");
            premiere = object.getString("premiereRU");


            String urlPosterString = "";
            int dotpos = posterURL.lastIndexOf('.');
            if(dotpos < 0) return ;
            int fPos = 19;
            if(dotpos <= fPos) return ;
            urlPosterString = posterURL.substring(fPos, dotpos);
            urlPosterString = "https://st.kp.yandex.net/images/film_big/"+urlPosterString+".jpg";
            new DownloadImageTask(posterContainer)
                    .execute(urlPosterString);
            initialized = true;
        }catch(NumberFormatException ex){
            initialized = false;
        }
        catch(JSONException exception){
            initialized = false;
        }
    }
//--------------------------------------------------------------------------------------------------
    public boolean loadPoster(ImageView view){
        posterContainer = view;
        if(posterLoaded) {
            if(mPoster != null) {
                if(posterContainer != null) {
                    posterContainer.setImageBitmap(mPoster);
                    posterContainer.postInvalidate();
                }
            }
        }
        return false;
    }
//--------------------------------------------------------------------------------------------------
    public String toString(){
        String s = "";
        s+=""+nameRU+"("+nameEN+")\n";
        s+="year:"+year+"\n";
        s+="hallCount "+ hallCount+"\n";
        s+="rating:"+rating+"\n";
        s+="poster:"+posterURL+"\n";
        s+="len:"+length+"\n";
        s+="country:"+country+"\n";
        s+="genres:"+genres+"\n";
        s+="premiere:"+premiere;
        return s;
    }

    @Override
    public int compareTo(FilmShort another) {
        if(another.rating == rating) return 0;
        if(another.rating > rating) return 1;
        else return -1;
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
                posterLoaded = true;
            }catch(OutOfMemoryError ex){
                mPoster = null;
                posterLoaded = false;
            } catch (Exception e) {
                mPoster = null;
                e.printStackTrace();
            }
            return mPoster;
        }

        protected void onPostExecute(Bitmap result) {
            if(mPoster != null) {
                if(posterContainer != null) {
                    posterContainer.setImageBitmap(mPoster);
                    posterContainer.postInvalidate();
                }
            }
        }
    }
}
