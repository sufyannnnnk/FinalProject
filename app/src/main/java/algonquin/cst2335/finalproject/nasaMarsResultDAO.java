package algonquin.cst2335.finalproject;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface nasaMarsResultDAO {

    @Insert
    long insertFav(nasaMarsResult r);


    @Query("Select * from nasaMarsResult")
    List<nasaMarsResult> getAllFavs();


    @Query("Select * from nasaMarsResult where imgID = :id")
    List<nasaMarsResult> searchByID(String id);


    @Delete
    void deleteFavourite(nasaMarsResult r);
}
