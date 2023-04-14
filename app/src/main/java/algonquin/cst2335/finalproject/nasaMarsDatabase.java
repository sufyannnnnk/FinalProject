package algonquin.cst2335.finalproject;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * This class represents the Room database for the NASA Mars results.
 */
@Database(entities = {nasaMarsResult.class}, version = 1)

public abstract class nasaMarsDatabase extends RoomDatabase {

    /**
     * Returns the DAO  for interacting with the database.
     * @return  DAO
     */
    public abstract nasaMarsResultDAO marsResultDAO();
}

