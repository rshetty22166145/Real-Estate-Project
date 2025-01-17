package entities;

import java.util.ArrayList;

public non-sealed class Buyer extends NonAdminUser{
    private final ArrayList<Integer> favorites;

    /**
     * Create a buyer class
     *
     * @param username Username of the buyer
     * @param password Password of the buyer
     */
    public Buyer(String username, String password) {
        super(username, password);
        this.favorites = new ArrayList<>();

    }

    // Getters and setters

    /**
     * @return Arraylist of favoirites
     */
    public ArrayList<Integer> getFavorites() {
        return favorites;
    }

    /**
     * @param id ID of a listing to add to the favorites
     */
    public void addFavouriteListing(int id) {
        favorites.add(id);
    }

    /**
     * @param id ID of a listing to remove from the favorites
     */
    public void removeFavouriteListing(int id) {
        favorites.remove(id);
    }

    /**
     * @return String representation of the Buyer class
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getUsername()).append("\n").append("Favorite Listings:\n");
        for (Integer integer : favorites) {
            builder.append(integer).append(" ");
        }
        return builder.toString();
    }
}
