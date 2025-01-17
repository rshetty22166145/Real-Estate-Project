package gateways;

import entities.User;
import entities.containers.UserContainer;

import java.io.*;

public non-sealed class HistoriesCSVController extends CSVController {
    private final UserContainer<String, User> users;

    public HistoriesCSVController(UserContainer<String, User> users) {
        super("Histories.csv");

        this.users = users;
    }

    @Override
    public void write() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath.getFilePath(), false));

        for (String username : users.keySet()) {
            for (String loginHistory : users.get(username).getLoginHistory()) {
                bw.append(username);
                bw.append(",");
                bw.append(loginHistory);
                bw.append("\n");
            }
        }

        bw.flush();
        bw.close();
    }
}
