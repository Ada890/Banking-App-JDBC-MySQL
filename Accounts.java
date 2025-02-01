package BankManagementSystem;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Accounts {
    private final Connection connection;
    private final Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public long openAccount(String email) {
        if (!accountExists(connection, email)) {
            String openAccountQuery = "INSERT INTO accounts (accNum, name, email, balance, secPin, secPinSalt) VALUES (?, ?, ?, ?, ?, ?)";
            scanner.nextLine();
            System.out.print("Enter Name:\t");
            String name = scanner.nextLine();
            System.out.print("Enter Initial Amount:\t");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter Security Pin (4-digit):\t");
            String secPin = scanner.nextLine();
            if (secPin.length() != 4)
                throw new RuntimeException("Security PIN should be 4 digits");

            try {
                long accNum = generateAccountNumber();
                String secPinSalt = DBUtils.getSalt();
                String hashedSecPin = DBUtils.hashSecurityPin(secPin, secPinSalt);

                PreparedStatement preparedStatement = connection.prepareStatement(openAccountQuery);
                preparedStatement.setLong(1, accNum);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, email);
                preparedStatement.setDouble(4, balance);
                preparedStatement.setString(5, hashedSecPin);
                preparedStatement.setString(6, secPinSalt);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Account Created Successfully");
                    return accNum;
                } else {
                    throw new RuntimeException("Account Creation Failed!!");
                }
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());

                if (e.getMessage().contains("Duplicate entry")) {
                    throw new RuntimeException("Account Already Exists");
                } else {
                    throw new RuntimeException("Account Creation Failed: " + e.getMessage());
                }
            }
        } else {
            throw new RuntimeException("Account Already Exists");
        }
    }

    public long getAccountNumber(String email) {
        String query = "SELECT accNum FROM accounts WHERE email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getLong("accNum");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        throw new RuntimeException("Account Number Doesn't Exist");
    }

    private long generateAccountNumber() {
        Random random = new Random();
        long accountNumber;

        while (true) {
            accountNumber = 1000000000L + Math.abs(random.nextLong() % 9000000000L); // 10-digit number

            try {
                String query = "SELECT COUNT(*) FROM accounts WHERE accNum = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setLong(1, accountNumber);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) == 0)
                    break;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return 10000100; // Default fallback account number in case of errors
            }
        }
        return accountNumber;
    }

    public boolean accountExists(Connection connection, String email) {
        return DBUtils.recordExists(connection, "accounts", "email", email);
    }

    public boolean hasAccount(String email) {
        String query = "SELECT COUNT(*) FROM accounts WHERE email = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returns true if the user has at least one account
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}