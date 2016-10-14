package kode.kinopoisk.minakov.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Alex on 13.10.2016.
 */
public class Cinema {
    public String name ="";
    public String address ="";
    public double lat=0.0;
    public double lon=0.0;
    public ArrayList<String> seanses;
    public boolean initialized = false;
    public Cinema(JSONObject object) {
        seanses = new ArrayList<String>();
        initialized = false;
        try {
            if (object.has("address")) address = object.getString("address");
            if (object.has("cinemaName")) name = object.getString("cinemaName");
            if (object.has("lat")) {
                try{
                    lat = Double.parseDouble(object.getString("lat"));
                }catch(NumberFormatException ex){
                    lat = 0.0;
                }
            }
            if (object.has("lon")) {
                try{
                    lon = Double.parseDouble(object.getString("lon"));
                }catch(NumberFormatException ex){
                    lon = 0.0;
                }
            }
            if(object.has("seance")){
                JSONArray seances = object.getJSONArray("seance");
                for(int i = 0; i< seances.length(); i++){
                    JSONObject seance = (JSONObject)seances.get(i);
                    if(seance.has("time")) seanses.add(seance.getString("time"));
                }
            }
            initialized = true;
        } catch (JSONException exception) {
            initialized = false;
        }
    }

    @Override
    public String toString() {
        String s = name;
        s+= ": "+address+"\n";
        s+= "coordinates : "+lat+","+lon+"\n";
        for(int i = 0; i< seanses.size(); i++){
            s+=" "+seanses.get(i);
        }
        return s+"\n";
    }
}
