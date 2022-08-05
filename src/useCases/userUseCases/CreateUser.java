package useCases.userUseCases;

import entities.AdminUser;
import entities.User;
import entities.containers.UserContainer;
import useCases.DataInterface;

import java.io.IOException;
import java.util.HashMap;

public class CreateUser {
    private final UserContainer<String, User> interfaceUsers;
    public DataInterface i;

    private final HashMap<String, AdminUser> createdAdmins;

    public CreateUser(UserContainer<String, User> interface_users, DataInterface i) {
        this.interfaceUsers = interface_users;
        createdAdmins = new UserContainer<>();
        this.i = i;
    }

    public void read() throws IOException {
        i.read();
    }

    public void write() throws IOException {
        i.write();
    }

    public void createAdminUser(String username, String password) {
        AdminUser user = new AdminUser(username, password);
        interfaceUsers.put(username, user);
        createdAdmins.put(username, user);
    }

    public HashMap<String, AdminUser> getCreatedAdmins() {
        return createdAdmins;
    }
}
