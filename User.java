package BankManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {
    private final Connection connection;
    private final Scanner scanner;

    public User(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void register() {
        scanner.nextLine();
        System.out.print("Full Name:\t");
        String name = scanner.nextLine();
        System.out.print("Email:\t");
        String email = scanner.nextLine();
        System.out.print("Password:\t");
        String password = scanner.nextLine();

        if (userExists(connection,email)) {
            System.out.println("User Already Exists for the Email Address");
            return;
        }

        String salt = DBUtils.getSalt();
        String hashedPassword = DBUtils.hashPassword(password, salt);

        String registerQuery = "INSERT INTO users (name, email, password, salt) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(registerQuery);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, hashedPassword);
            preparedStatement.setString(4, salt);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0)
                System.out.println("Registered Successfully");
            else
                System.out.println("Registration Failed!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String login() {
        scanner.nextLine();
        System.out.print("Email:\t");
        String email = scanner.nextLine();
        System.out.print("Password:\t");
        String password = scanner.nextLine();
        String loginQuery = "SELECT * FROM users WHERE email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                String storedSalt = resultSet.getString("salt");

                String hashedInputPassword = DBUtils.hashPassword(password, storedSalt);

                if (storedHashedPassword.equals(hashedInputPassword)) {
                    System.out.println("Logged in successfully");
                    return email;
                } else {
                    System.out.println("Incorrect Email or Password");
                }
            } else {
                System.out.println("User Not Found");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean userExists(Connection connection, String email) {
        return DBUtils.recordExists(connection, "users", "email", email);
    }
}
