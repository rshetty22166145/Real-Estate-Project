package entities;

import entities.containers.MessageContainer;

import java.util.ArrayList;

public abstract sealed class NonAdminUser
extends User
implements Bannable
permits Buyer, Seller
{
    private boolean isBanned = false;
    protected MessageContainer<Integer, Message> outbox;
    protected MessageContainer<Integer, Message> inbox;

    /**
     * Creates an instance of a non-admin user.
     *
     * @param username this user's username.
     * @param password this user's password.
     */
    public NonAdminUser(String username, String password) {
        super(username, password);

        outbox = new MessageContainer<>();
        inbox = new MessageContainer<>();
    }

    public NonAdminUser(String username, String password, ArrayList<String> loginHistory) {
        super(username, password, loginHistory);
        outbox = new MessageContainer<>();
        inbox = new MessageContainer<>();
    }

    /**
     * Returns whether the user is currently banned.
     * @return true if the user is banned and false if not.
     */
    public boolean checkUserBanStatus() {
        return isBanned;
    }

    /**
     * Bans user temporarily.
     * @return true to signal the user has been banned.
     */
    public boolean temporarilyBanUser() {
        isBanned = true;
        return true;
    }

    /**
     * Unbans user.
     * @return true to signal the user has been unbanned.
     */
    public boolean unbanUser() {
        isBanned = false;
        return true;
    }

    public MessageContainer<Integer, Message> getOutbox() {
        return outbox;
    }

    public MessageContainer<Integer, Message> getInbox() {
        return inbox;
    }

    public void sendMessage(NonAdminUser nonAdminUser, Message message) {
        nonAdminUser.inbox.put(message.getMessageID(), message);
        this.outbox.put(message.getMessageID(), message);
    }
}