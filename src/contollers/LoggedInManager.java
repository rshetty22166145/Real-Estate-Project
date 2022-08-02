package contollers;

import entities.Message;
import entities.User;
import entities.containers.ListingContainer;
import entities.containers.MessageContainer;
import entities.containers.UserContainer;
import exceptions.UserCannotBeBannedException;
import exceptions.UserNotFoundException;
import useCases.userUseCases.AuthenticateUser;
import useCases.userUseCases.RestrictUser;
import useCases.userUseCases.UpdateUserHistory;
import useCases.CSVUseCases.UsernamePasswordFileEditor;
import entities.Listing;
import useCases.listingUseCases.CreateListing;
import useCases.listingUseCases.ListingProperties;
import useCases.messageUseCases.SendMessages;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggedInManager {
    private final InputHandler input;
    private final UserInterface ui;
    private final AuthenticateUser auth;
    private final UpdateUserHistory history;
    private final RestrictUser restrict;
    private final UserManager userManager;
    private final UsernamePasswordFileEditor file;
    private final SendMessages sendMessages;
    private final ListingProperties listingProperties;
    private final CreateListing createListing;

    private final FavAndUnFavManager favAndUnFavManager;


    /**
     * Constructor for LoggedInManager
     * @param users is the container that stores users of the program
     * @param messages is the container that stores messages between users
     * @param listings is the container that stores listings
     */
    public LoggedInManager(UserContainer<String, User> users, MessageContainer<Integer, Message> messages,
                           ListingContainer<Integer, Listing> listings) {

        auth = new AuthenticateUser(users);
        history = new UpdateUserHistory(users);
        restrict = new RestrictUser(users);
        file = new UsernamePasswordFileEditor(auth, users);
        sendMessages = new SendMessages(messages, users);
        listingProperties = new ListingProperties(listings);
        createListing = new CreateListing(listings, users);
        favAndUnFavManager = new FavAndUnFavManager(users, listings);
        userManager = new UserManager(users);

        ui = new UserInterface();
        input = new InputHandler(ui);
    }

    /**
     * Calls the appropriate screen depending on what type of user is logged in
     *
     * @param username the logged-in user's username
     * @return true if the correct user screen successfully appears
     */
    public boolean userScreen(String username) {
        if (!auth.checkUserAdmin(username)) {
            if (!auth.checkUserSeller(username)) {
                return buyerScreen(username);
            } else {
                return sellerScreen(username);
            }
        } else {
            return adminScreen(username);
        }
    }

    /**
     * Presents buyer with options to navigate through program
     *
     * @param username the logged-in user's username
     * @return true if the buyer screen successfully appears
     */
    public boolean buyerScreen(String username) {
        ui.printBuyerLoginMenu();

        ArrayList<Integer> allowedInputs = new ArrayList<>();
        Collections.addAll(allowedInputs, 1, 2, 3, 4, 5, 6, 7);

        int select = input.intInput(allowedInputs);
        switch (select) {
            case 1:
                // View listings
                searchProperties(username);
                break;
            case 2:
                // View favourite listings
                viewFavouriteListings(username);
                checkDeleteFavouriteListing(username);
                break;
            case 3:
                // Send message
                messageUser(username);
                break;
            case 4:
                // View inbox
                viewInbox(username);
                break;
            case 5:
                // View outbox
                viewOutbox(username);
                break;
            case 6:
                // View login history
                ArrayList<String> userHistory = history.getLoginHistory(username);
                ui.printLoginHistory(userHistory);
                break;
            case 7:
                // Logout user
                auth.logoutUser(username);
                ui.printLogOutSuccess();
                return false;
        }

        return true;
    }

    /**
     * Presents seller with options to navigate through program
     *
     * @param username the logged-in user's username
     * @return true if the seller screen successfully appears
     */
    public boolean sellerScreen(String username) {
        ui.printSellerLoginMenu();

        ArrayList<Integer> allowedInputs = new ArrayList<>();
        Collections.addAll(allowedInputs, 1, 2, 3, 4, 5, 6, 7, 8);

        int select = input.intInput(allowedInputs);
        switch (select) {
            case 1:
                // View posted listings
                ui.printFilteredListings(createListing.getSellerListingsStrings(username));
                break;
            case 2:
                // Message user
                messageUser(username);
                break;
            case 3:
                // View inbox
                viewInbox(username);
                break;
            case 4:
                // View outbox
                viewOutbox(username);
                break;
            case 5:
                // Create listing
                createNewListing(username);
                break;
            case 6:
                // Delete listing
                deleteListing(username);
                break;
            case 7:
                // View login history
                ArrayList<String> userHistory = history.getLoginHistory(username);
                ui.printLoginHistory(userHistory);
                break;
            case 8:
                // Logout user
                auth.logoutUser(username);
                ui.printLogOutSuccess();
                return false;
        }

        return true;
    }

    /**
     * Bans specified user
     */
    private void updateUserBanStatus() {
        ui.printRestrictUsernameInput();
        String restrictUser = input.strInput();

        boolean isBanned;
        try {
            isBanned = restrict.isUserBanned(restrictUser);
        } catch (UserCannotBeBannedException | UserNotFoundException e) {
            ui.printArbitraryException(e);
            return;
        }

        ui.printRestrictUserConfirmation(restrictUser, isBanned);

        ArrayList<String> allowedStrings = new ArrayList<>();
        Collections.addAll(allowedStrings, "Y", "N");

        String choice = input.strInput(allowedStrings);
        if (choice.equals("Y")) {
            if (!isBanned) {
                restrict.banNonAdminUser(restrictUser);
            } else {
                restrict.unbanNonAdminUser(restrictUser);
            }
        }
    }

    /**
     * Deletes specified user
     */
    private void deleteUser() {
        ui.printDeleteUsernameInput();
        String deleteUser = input.strInput();
        boolean deleted = restrict.deleteNonAdminUser(deleteUser);
        if (deleted) {
            try {
                file.createUsernamePasswordFile();
            } catch (IOException e) {
                ui.printArbitraryException(e);
            }

            history.overwriteUserHistories();
            ui.printDeleteUserSuccess(deleteUser);
        } else {
            ui.printDeleteUserFail(deleteUser);
        }
    }

    /**
     * Sends a message from the logged-in user to a specified recipient
     *
     * @param username the username of the logged-in user
     */
    private void messageUser(String username) {
        ui.printMessageUsernameInput();
        String recipient = input.strInput();

        boolean userExists = auth.checkUserNonAdminExists(recipient);

        if (userExists) {
            ui.printMessageInput();
            String content = input.strInput();

            sendMessages.sendMessage(username, recipient, content);

            ui.printMessageSentSuccess();

        } else {
            ui.printMessageUserFail(recipient);
        }
    }

    /**
     * Creates a new listing with the specified properties
     *
     * @param username the username of the logged-in user
     */
    private void createNewListing(String username) {
        ui.printListingHasUnitNumber();
        ArrayList<String> allowedStrings = new ArrayList<>();
        Collections.addAll(allowedStrings, "Y", "N");
        String choice = input.strInput(allowedStrings);

        int unitNumber = 0;
        if (choice.equals("Y")) {
            ui.printEnterType("unit number");
            unitNumber = input.intInput();
        }

        ui.printEnterType("civic address number");
        int number = input.intInput();

        ui.printEnterType("street name");
        String street = input.strInput();

        ui.printEnterType("city");
        String city = input.strInput();

        ui.printEnterType("price");
        BigDecimal price = input.bigDecimalInput();

        ui.printEnterType("listing type (apartment, house, townhouse)");
        String[] inputsAllowed = {"apartment", "house", "townhouse"};
        String type = input.strInput(List.of(inputsAllowed), true);

        ui.printEnterType("number of floors");
        int floors = input.intInput();

        ui.printEnterType("number of bedrooms");
        int bedrooms = input.intInput();

        ui.printEnterType("number of bathrooms");
        int bathrooms = input.intInput();

        Listing listing;
        if (choice.equals("Y")) {
            listing = createListing.addListing(unitNumber, number, street, city, type, bedrooms, bathrooms, floors, price);
        } else {
            listing = createListing.addListing(number, street, city, type, bedrooms, bathrooms, price);
        }

        createListing.addListingToSeller(username, listing);
        createListing.addListingToCreatedListings(username, listing);

        ui.printListingCreatedSuccess();
    }

    /**
     * Deletes a specified listing posted by the logged-in user
     *
     * @param username the username of the logged-in user
     */
    private void deleteListing(String username) {
        ArrayList<String> listingStrings = createListing.getSellerListingsStrings(username);
        ui.printNumberedListings(listingStrings);
        ui.printEnterType("number of property to delete");
        int number = input.intInput(1, listingStrings.size());
        Listing listingToRemove = createListing.getSellerListings(username).get(number - 1);
        createListing.deleteListing(username, listingToRemove);
        createListing.removeFromCreatedListings(username, listingToRemove);

        ui.printDeleteListingSuccess();
    }

    private void checkAddListingToFavourites(String username, ArrayList<Listing> listings){
        ArrayList<Integer> listingID = listingProperties.getListingsID(listings);

        if(listingID.size() > 0) {
            ui.printAddListingToFavourites();
            ArrayList<String> allowedStrings = new ArrayList<>();
            Collections.addAll(allowedStrings, "Y", "N");
            String choice = input.strInput(allowedStrings);

            if (choice.equals("Y")) {
                ui.printEnterType("number of listing to add");
                int number = input.intInput(1, listings.size());
                favAndUnFavManager.addToBuyerFavorites(username, listingID.get(number - 1));
                ui.printListingFavouriteSuccess();
            }
        }
    }

    private void checkDeleteFavouriteListing(String username){
        ArrayList<Integer> listingIDs = favAndUnFavManager.getBuyerFavouritesID(username);

        if(listingIDs.size() > 0){
            ui.printDeleteListingFromFavourites();
            ArrayList<String> allowedStrings = new ArrayList<>();
            Collections.addAll(allowedStrings, "Y", "N");
            String choice = input.strInput(allowedStrings);

            if (choice.equals("Y")) {
                ui.printEnterType("number of listing you want to remove");
                int number = input.intInput(1, listingIDs.size());
                favAndUnFavManager.removeFromBuyerFavorites(username, number - 1);
                ui.printFavouriteRemovedSuccess();
            }
        }
        else{
            ui.printNoFavourites();
        }
    }

    /**
     * Displays available listings based on specific properties
     */
    private void searchProperties(String username) {
        ui.printListingOptionsMenu();

        ArrayList<Integer> allowedInputs = new ArrayList<>();
        Collections.addAll(allowedInputs, 1, 2, 3, 4, 5, 6, 7, 8);

        int select = input.intInput(allowedInputs);
        switch (select) {
            case 1: {
                // Search by civic address
                ui.printEnterType("civic address number");
                int number = input.intInput();
                ArrayList<Listing> listings = listingProperties.searchByCivicAddress(number);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 2: {
                // Search by street name
                ui.printEnterType("street name");
                String street = input.strInput();
                ArrayList<Listing> listings = listingProperties.searchByStreetName(street);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 3: {
                // Search by city
                ui.printEnterType("city");
                String city = input.strInput();
                ArrayList<Listing> listings = listingProperties.searchByCity(city);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 4: {
                // Search by bedroom number
                ui.printEnterType("number of bedrooms wanted");
                int number = input.intInput();
                ArrayList<Listing> listings = listingProperties.searchByBedrooms(number);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 5: {
                // Search by bathroom number
                ui.printEnterType("number of bathrooms wanted");
                int number = input.intInput();
                ArrayList<Listing> listings = listingProperties.searchByBathrooms(number);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                break;
            }

            case 6: {
                // Search by floor number
                ui.printEnterType("number of floors wanted");
                int number = input.intInput();
                ArrayList<Listing> listings = listingProperties.searchByFloors(number);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 7: {
                // Search by price
                ui.printEnterType("minimum price");
                BigDecimal min = input.bigDecimalInput();
                ui.printEnterType("maximum price");
                BigDecimal max = input.bigDecimalInput();
                ArrayList<Listing> listings = listingProperties.searchByPrice(max, min);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }

            case 8: {
                // Search by listing type
                ui.printEnterType("listing type (apartment, house, townhouse) wanted");
                String[] inputsAllowed = {"apartment", "house", "townhouse"};
                String type = input.strInput(List.of(inputsAllowed));
                ArrayList<Listing> listings = listingProperties.searchByListingType(type);
                ui.printNumberedListings(listingProperties.getListingsStrings(listings));
                checkAddListingToFavourites(username, listings);
                break;
            }
        }
    }

    /**
     * Displays the messages inbox of the logged-in user
     * @param username the username of the logged-in user
     */
    public void viewInbox(String username) {
        ui.printMessages(sendMessages.getMessageInbox(username));
    }

    /**
     * Displays the messages outbox of the logged-in user
     * @param username the username of the logged-in user
     */
    public void viewOutbox(String username) {
        ui.printMessages(sendMessages.getMessageOutbox(username));
    }

    public void viewFavouriteListings(String username){
        ui.printNumberedListings(favAndUnFavManager.getBuyerFavouritesString(username));
    }
    /**
     * Presents admin with options to navigate through program
     *
     * @param username the logged-in user's username
     * @return true if the admin screen successfully appears
     */
    public boolean adminScreen(String username) {
        ui.printAdminLoginMenu();

        ArrayList<Integer> allowedInputs = new ArrayList<>();
        Collections.addAll(allowedInputs, 1, 2, 3, 4, 5);

        int select = input.intInput(allowedInputs);
        switch (select) {
            case 1: {
                // View login history
                ArrayList<String> userHistory = history.getLoginHistory(username);
                ui.printLoginHistory(userHistory);
                break;
            }

            case 2: {
                // Create new admin user
                userManager.createNewUser("ADMIN");
                break;
            }

            case 3: {
                // Ban or unban user
                updateUserBanStatus();
                break;
            }

            case 4: {
                // Delete user
                deleteUser();
                break;
            }

            case 5: {
                // Logout user
                auth.logoutUser(username);
                ui.printLogOutSuccess();
                return false;
            }
        }

        return true;
    }
}
