package algonquin.cst2335.finalproject;

/**
 *  A class representing a record of a city in the database
 */
public class DBCitiesRecord {
    private String cities;
    private int id;

    /**
     * Creates a new instance of DBCitiesRecord with the given city name
     * @param cities the name of the city
     */
    public DBCitiesRecord(String cities) {
        this.cities = cities;
    }

    public String getCities() {
        return cities;
    }

    public void setCities(String cities) {
        this.cities = cities;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
