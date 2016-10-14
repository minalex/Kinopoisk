package kode.kinopoisk.minakov.data;

/**
 * Created by Alex on 11.10.2016.
 */
public class City {
    private String cityName = "";
    private int cityID = 0;
//--------------------------------------------------------------------------------------------------
    public City(){}
    public City(String id, String name){
        cityName = name;
        try{
            cityID = Integer.parseInt(id);
        }catch(NumberFormatException ex){
            cityID = -1;
        }
    }
//--------------------------------------------------------------------------------------------------
    public int getCityID(){
        return cityID;
    }
//--------------------------------------------------------------------------------------------------
    public String getCityName(){
        return cityName;
    }
}
