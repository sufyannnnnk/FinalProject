package algonquin.cst2335.finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import algonquin.cst2335.finalproject.databinding.MarsFragmentBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The nasaMarsDetailsFragment class is a fragment that displays detailed information about a selected Mars photo result,
 */
public class nasaMarsDetailsFragment extends Fragment {

    private nasaMarsResult selected;

    private nasaMarsResultDAO marsResultDAO;

    /**
     *  The constructor for nasaMarsDetailsFragment class
     * @param mars
     * @param db
     */
    public nasaMarsDetailsFragment(nasaMarsResult mars, nasaMarsDatabase db){
        selected = mars;
        marsResultDAO = db.marsResultDAO();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        MarsFragmentBinding binding = MarsFragmentBinding.inflate(inflater);

        binding.marsFragmentImage.setImageBitmap(selected.getBitmap());
        binding.cameraFragmentText.setText(selected.getCamName());
        binding.urlFragmentText.setText(selected.getImgSrc());


        List<nasaMarsResult> search = new ArrayList<>();
        Executor thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            search.addAll(marsResultDAO.searchByID(selected.getImgID()));
            getActivity().runOnUiThread(() -> {
                if (search.size() == 0) {
                    binding.saveButton.setVisibility(View.VISIBLE);
                    binding.saveButton.setEnabled(true);
                    binding.delButton.setVisibility(View.INVISIBLE);
                    binding.delButton.setEnabled(false);
                } else {
                    binding.saveButton.setVisibility(View.INVISIBLE);
                    binding.saveButton.setEnabled(false);
                    binding.delButton.setVisibility(View.VISIBLE);
                    binding.delButton.setEnabled(true);
                }
            });
        });


        binding.saveButton.setOnClickListener(saveClk -> {
            binding.saveButton.setVisibility(View.INVISIBLE);
            binding.saveButton.setEnabled(false);
            binding.delButton.setVisibility(View.VISIBLE);
            binding.delButton.setEnabled(true);

            FileOutputStream fOut = null;
            String filename = selected.getImgID() + ".png";
            try { fOut = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                selected.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            nasaMarsResult fav = new nasaMarsResult(
                    selected.getImgID(),
                    selected.getImgSrc(),
                    selected.getCamName(),
                    selected.getRoverName(),
                    filename
            );
            thread.execute(() -> {
                marsResultDAO.insertFav(fav);
            });
        });


        binding.delButton.setOnClickListener(delClk -> {
            binding.saveButton.setVisibility(View.INVISIBLE);
            binding.saveButton.setEnabled(false);
            binding.delButton.setVisibility(View.INVISIBLE);
            binding.delButton.setEnabled(false);

            thread.execute(() -> {
                marsResultDAO.deleteFavourite(selected);
            });


            Snackbar.make(binding.getRoot(), "Deleted from saveds", Snackbar.LENGTH_LONG)
                    .setAction("Undo", snackClk ->{
                        thread.execute(() -> {
                            marsResultDAO.insertFav(selected);
                        });
                        binding.saveButton.setVisibility(View.INVISIBLE);
                        binding.saveButton.setEnabled(false);
                        binding.delButton.setVisibility(View.VISIBLE);
                        binding.delButton.setEnabled(true);
                    })
                    .show();
        });

        return binding.getRoot();
    }
}
