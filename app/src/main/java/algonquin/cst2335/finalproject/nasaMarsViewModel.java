package algonquin.cst2335.finalproject;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * ViewModel for NASA Mars fragment
 */
public class nasaMarsViewModel extends ViewModel {

    /**
     * LiveData for Mars search results
     */
    public MutableLiveData<ArrayList<nasaMarsResult>> result = new MutableLiveData<>();


    /**
     * LiveData for favourite Mars search results
     */
    public MutableLiveData<ArrayList<nasaMarsResult>> favourites = new MutableLiveData<>();

    public boolean favouriteList;
}
