package algonquin.cst2335.finalproject;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * This class represents a single image from the NASA Mars Rover Photos API.
 */
@Entity
public class nasaMarsResult {
     @PrimaryKey
    @NonNull
    @ColumnInfo(name = "imgID")
    protected String imgID;
      @ColumnInfo(name = "imgSrc")
    protected String imgSrc;
      @ColumnInfo(name = "camName")
    protected String camName;
      @ColumnInfo(name = "roverName")
    protected String roverName;
     @ColumnInfo(name = "imgPath")
    protected String imgPath;
     @Ignore
    protected Bitmap bitmap;

    /**
     * Constructor for nasaMarsResult object with basic image details such as ID, source URL, camera name, and rover name.
     * @param imgID
     * @param imgSrc
     * @param camName
     * @param roverName
     */
    @Ignore
    public nasaMarsResult(String imgID, String imgSrc, String camName, String roverName){
        this.imgID = imgID;
        this.imgSrc = imgSrc;
        this.camName = camName;
        this.roverName = roverName;
    }

    /**
     * Constructor for nasaMarsResult object with full image details including image path.
     * @param imgID
     * @param imgSrc
     * @param camName
     * @param roverName
     * @param imgPath
     */
    public nasaMarsResult(String imgID, String imgSrc, String camName, String roverName, String imgPath){
        this.imgID = imgID;
        this.imgSrc = imgSrc;
        this.camName = camName;
        this.roverName = roverName;
        this.imgPath = imgPath;
    }



    public String getImgID() {
        return imgID;}


    public String getImgSrc() {
        return imgSrc;}


    public String getCamName() {
        return camName;}


    public String getRoverName() {
        return roverName;}


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap; }


    public Bitmap getBitmap() {

        return bitmap;
    }


    public String getImgPath() {
        return imgPath; }

}
