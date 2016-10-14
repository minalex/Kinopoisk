package kode.kinopoisk.minakov;

import java.util.ArrayList;
import kode.kinopoisk.minakov.data.City;
import kode.kinopoisk.minakov.data.FilmShort;

/**
 * Created by Alex on 11.10.2016.
 */

public class KinopoiskAPI {

    public static final String API_BASE_URL = "http://api.kinopoisk.cf/";
    public static  final String  API_GET_CITIES = "getCityList?countryID=2";//  для россии код страны 2
    public static  final String  API_GET_TODAY_FILMS = "/getTodayFilms?";
    public static  final String  API_GET_FILM = "/getFilm?filmID=";
    public static  final String  API_GET_SEANSES = "/getSeance?";
    public static City currentCity;
    public static FilmShort currentFilm;
    private static ArrayList<City> citiesList;
    public static String[] genres= new String[]{
    "биография",
    "боевик",
    "вестерн",
    "военный",
    "детектив",
    "детский",
    "документальный",
    "драма",
    "комедия",
    "короткометражка",
    "криминал",
    "мелодрама",
    "музыка",
    "мультфильм",
    "мюзикл",
    "приключения",
    "семейный",
    "триллер",
    "ужасы",
    "фантастика",
    "фэнтези"};


}
