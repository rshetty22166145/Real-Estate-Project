package entities;
import java.util.ArrayList;

public abstract sealed class User
permits AdminUser, NonAdminUser
{
    private final String username;
    private final String password;
    private final ArrayList<String> loginHistory;

    protected Boolean isAdmin = false;

    private boolean isLoggedIn = false;

    /**
     * @param username of the User
     * @param password of the User
     */
    protected User(String username, String password) {
        this.username = username;
        this.password = password;
        loginHistory = new ArrayList<>();
    }

    /**
     * @param username     of the User
     * @param password     of the User
     * @param loginHistory of the User
     */
    protected User(String username, String password, ArrayList<String> loginHistory) {
        this.username = username;
        this.password = password;
        this.loginHistory = loginHistory;
    }

    /**
     * @return the username of the User
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password of the User
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the login history of the User
     */
    public ArrayList<String> getLoginHistory() {
        return loginHistory;
    }

    /**
     * @return admin status of the User
     */
    public Boolean getAdmin() {
        return isAdmin;
    }

    /**
     * @return whether the User is logged in
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * @param newLogin to be added to the login history of the User
     */
    public void addToLoginHistory(String newLogin) {
        loginHistory.add(newLogin);
    }

    /**
     * @param bool to set login status to
     */
    public void setIsLoggedIn(boolean bool) {
        isLoggedIn = bool;
    }
}
