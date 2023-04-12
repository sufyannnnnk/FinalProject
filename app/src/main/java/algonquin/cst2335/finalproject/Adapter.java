package algonquin.cst2335.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 *  Adapter class for the RecyclerView to display the list of cities.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    RecyclerClickInterface recyclerClickInterface;
    LayoutInflater layoutInflater;
    ArrayList<CitiesDataModal> citiesRecords;

    /**
     *  Constructor for the Adapter class
     *  @param context The context of the activity or fragment where the Adapter is used.
     *  @param citiesRecords An ArrayList of CitiesDataModal containing the list of cities to be displayed.
     *  @param recyclerClickInterface An instance of RecyclerClickInterface to handle click events.
     */
    public Adapter(Context context, ArrayList<CitiesDataModal> citiesRecords, RecyclerClickInterface recyclerClickInterface) {
        this.layoutInflater = LayoutInflater.from(context);
        this.citiesRecords = citiesRecords;
        this.recyclerClickInterface = recyclerClickInterface;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.city_card, parent, false);
        return new ViewHolder(view, recyclerClickInterface);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cityNameView.setText(citiesRecords.get(position).getCityName());
        holder.cityTempView.setText(citiesRecords.get(position).getCityTemp());
        holder.dateTimeView.setText(citiesRecords.get(position).getCityTime());
        Picasso.get().load(citiesRecords.get(position).getCityWeatherIcon()).into(holder.weatherIconView);
    }

    /**
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return citiesRecords.size();
    }
    /**
     *  ViewHolder class for the Adapter class.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityNameView, cityTempView, dateTimeView;
        ImageView weatherIconView;
        /**
         *  Constructor for the ViewHolder class
         *  @param itemView The view that the ViewHolder will be holding.
         *  @param recyclerClickInterface An instance of RecyclerClickInterface to handle click events.
         */
        public ViewHolder(@NonNull View itemView, RecyclerClickInterface recyclerClickInterface) {
            super(itemView);
            cityNameView = itemView.findViewById(R.id.cardCityName);
            cityTempView = itemView.findViewById(R.id.cardTemperature);
            dateTimeView = itemView.findViewById(R.id.cardDateTime);
            weatherIconView = itemView.findViewById(R.id.cardWeatherIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerClickInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            recyclerClickInterface.itemClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (recyclerClickInterface != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerClickInterface.itemLongClick(position);
                            citiesRecords.remove(position);
                            notifyDataSetChanged();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
