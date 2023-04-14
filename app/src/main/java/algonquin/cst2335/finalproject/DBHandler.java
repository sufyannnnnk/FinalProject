package algonquin.cst2335.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    // Create variables for database.
    private static final String DB_NAME = "weatherApp";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "cities";
    private static final String ID = "id";
    private static final String CITY = "name";

    private static ArrayList<DBCitiesRecord> cities;
    private static DBHandler instance = null;

    // Database Constructor.
    private DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        cities = readCity();
    }

    public static DBHandler getInstance(Context context) {
        if (instance != null) return instance;
        return new DBHandler(context);
    }

    // Creating sql query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Setup column names with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CITY + " TEXT)";

        // execSQL method Execute above query
        db.execSQL(query);
    }

    // This method is use to add cities to database.
    public boolean addCity(String city) {
        if (!isExists(city)) {
            // open connection for writing data in our Database.
            SQLiteDatabase db = this.getWritableDatabase();

            // creating variable for content values.
            ContentValues citiesData = new ContentValues();

            // put data into ContentValues.
            citiesData.put(CITY, city);

            // insert content values to table.
            db.insert(TABLE_NAME, null, citiesData);

            // close database connection
            db.close();

            //Add that city in arraylist as well
            cities.add(new DBCitiesRecord(city));
            return true;
        }
        return false;
    }

    //This method check is city already exists in Database or not
    private boolean isExists(String city) {
        for (int i = 0; i < cities.size(); i++)
            if (cities.get(i).getCities().equalsIgnoreCase(city))
                return true;
        return false;
    }

    // This method is use to Read cities from database.
    public ArrayList<DBCitiesRecord> readCity() {
        // open connection for read data from Database.
        SQLiteDatabase db = this.getReadableDatabase();

        // creating cursor with query to read data from database.
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // Creating a new ArrayList to store cursor data
        ArrayList<DBCitiesRecord> citiesModelList = new ArrayList<>();

        // Moving cursor to first position.
        if (cursor != null)
            // Moving cursor until cursor is not empty.
            while (cursor.moveToNext())
                // adding data from cursor to array list.
                citiesModelList.add(new DBCitiesRecord(cursor.getString(1)));

        // Closing cursor
        if (cursor != null)
            cursor.close();

        // Returning cities List
        return citiesModelList;
    }

    public void removeCity(String city) {
        // open connection for writing data in our Database.
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete Row from table.
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE name = '" + city + "'");

        // close database connection
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If the table exists already this method called automatically.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
