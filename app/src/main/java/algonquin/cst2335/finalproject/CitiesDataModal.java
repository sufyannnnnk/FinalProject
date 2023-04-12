package algonquin.cst2335.finalproject;

/**
 * A class representing a City's data modal, containing information about the city's name,
 * temperature, time, and weather icon.
 */
public class CitiesDataModal {
    private String cityName, cityTemp, cityTime, cityWeatherIcon;

    /**
     *  Constructs a new CitiesDataModal object with the given city name, temperature, time and weather icon.
     *  @param cityName the name of the city
     *  @param cityTemp the temperature of the city
     *  @param cityTime the time of the city
     *  @param cityWeatherIcon the weather icon of the city
     */
    public CitiesDataModal(String cityName, String cityTemp, String cityTime, String cityWeatherIcon) {
        this.cityName = cityName;
        this.cityTemp = cityTemp;
        this.cityTime = cityTime;
        this.cityWeatherIcon = cityWeatherIcon;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityTemp() {
        return cityTemp;
    }

    public void setCityTemp(String cityTemp) {
        this.cityTemp = cityTemp;
    }

    public String getCityTime() {
        return cityTime;
    }

    public void setCityTime(String cityTime) {
        this.cityTime = cityTime;
    }

    public String getCityWeatherIcon() {
        return cityWeatherIcon;
    }

    public void setCityWeatherIcon(String cityWeatherIcon) {
        this.cityWeatherIcon = cityWeatherIcon;
    }
}
