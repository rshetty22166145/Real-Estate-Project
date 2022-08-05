package useCases.userUseCases;

import entities.User;
import entities.containers.UserContainer;
import useCases.CSVUseCases.csvFilePath;
import useCases.DataInterface;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


public class UpdateUserHistory {
    private final UserContainer<String, User> interfaceUsers;
    public DataInterface i;

    public UpdateUserHistory(UserContainer<String, User> interfaceUsers, DataInterface i) {
        this.interfaceUsers = interfaceUsers;
        this.i = i;
    }

    public void read() throws IOException {
        i.read();
    }

    public void write() throws IOException {
        i.write();
    }

    /**
     * Read this user's login history from csv file.
     *
     * @param username this user's username.
     * @return an arraylist of timestamps corresponding to this user's logins.
     */
    public ArrayList<String> readUserHistory(String username) {
        ArrayList<String> userHistory = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath.getDatabasePath() + "UserHistories.csv"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] contents = line.split(",");

                if (username.equals(contents[0])) {
                    userHistory.add(contents[1]);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userHistory;
    }


    public void readUserHistories() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath.getDatabasePath() + "UserHistories.csv"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] contents = line.split(",");

                User u = interfaceUsers.get(contents[0]);
                u.addToLoginHistory(contents[1]);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Overwrite csv file with new login history for each user.
     */
    public void overwriteUserHistories() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath.getDatabasePath() + "UserHistories.csv", false));

            for (String username : interfaceUsers.keySet()) {
                for (String loginHistory : getLoginHistory(username)) {
                    bw.append(username);
                    bw.append(",");
                    bw.append(loginHistory);
                    bw.append("\n");
                }
            }

            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a new login for this user to csv file.
     *
     * @param username this user's username.
     * @param append   signals whether to append to csv file.
     */
    public void writeUserHistory(String username, boolean append) {
        User user = interfaceUsers.get(username);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath.getDatabasePath() + "UserHistories.csv", append));
            Scanner sc = new Scanner(csvFilePath.getDatabasePath() +"UserHistories.csv");
            Date newLogin = new Date();

            while (sc.hasNext()) {
                sc.next();
            }

            bw.append(username);
            bw.append(",");
            bw.append(newLogin.toString());
            bw.append("\n");

            bw.flush();
            bw.close();

            user.addToLoginHistory(newLogin.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get this user's login history locally (without reading from csv file).
     *
     * @param username this user's username.
     */
    public ArrayList<String> getLoginHistory(String username) {
        User user = interfaceUsers.get(username);
        return user.getLoginHistory();
    }
}
