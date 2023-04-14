package algonquin.cst2335.finalproject;

/**
 *  An interface defining click events for RecyclerView items.
 */
public interface RecyclerClickInterface {
    /**
     * @param position The position of the clicked item in the RecyclerView.
     */
    void itemClick(int position);
    void itemLongClick(int position);
}
