package gateways;

import entities.Seller;
import useCases.CSVUseCases.DatabaseFilePath;
import useCases.userUseCases.UserFactory;

import java.io.*;
import java.util.HashMap;

public class SellersCSVController implements CsvInterface {
    private final UserFactory userFactory;

    private final static DatabaseFilePath filepath = new DatabaseFilePath("Sellers.csv");

    public SellersCSVController(UserFactory userFactory){
        this.userFactory = userFactory;
    }


    @Override
    public void read() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filepath.getFilePath()));
        String line;
        while((line = reader.readLine()) != null){
            String[] splitLine = line.split(",");
            String username = splitLine[0];
            String password = splitLine[1];
            userFactory.createUser("seller", username, password);
        }
        reader.close();
    }

    @Override
    public void write() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.getFilePath(), false));
        HashMap<String, Seller> createdSellers = userFactory.getCreatedSellers();
        for(String username : createdSellers.keySet()){
            writer.write(username + "," + createdSellers.get(username).getPassword() + "\n");
        }
        writer.close();
    }
}