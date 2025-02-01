package BankManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;


public class BankingApp {
    private static final String url = "jdbc:mysql://localhost:3306/bankingSystem";
    private static final String user = "root";
    private static final String password = "l8a9l0a@AD";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            Scanner scanner = new Scanner(System.in);
            User user = new User(connection, scanner);
            Accounts accounts = new Accounts(connection, scanner);
            AccountManager accountManager = new AccountManager(connection, scanner);

            String email;
            long accNum;

            while (true) {
                System.out.println("*** Welcome to Bank Management System ***");
                System.out.println();
                System.out.println("1.Register\n2.Login\n3.Exit");
                System.out.println("Enter your choice: \t");
                int choice1 = scanner.nextInt();

                switch (choice1) {
                    case 1: user.register();
                    break;

                    case 2:
                        email = user.login();
                        if (email != null) {
                            if (accounts.hasAccount(email)) {
                                // User has an existing account
                                accNum = accounts.getAccountNumber(email);
                                performTransactions(accountManager, accNum, scanner);
                            } else {
                                // User does not have an account
                                System.out.println();
                                System.out.println("1. Open a new Bank Account");
                                System.out.println("2. Exit");
                                if (scanner.nextInt() == 1) {
                                    accNum = accounts.openAccount(email);
                                    System.out.println("Account Created Successfully");
                                    System.out.println("Your Account Number is: " + accNum);

                                    // After creating the account, allow the user to perform transactions
                                    performTransactions(accountManager, accNum, scanner);
                                }
                            }
                        } else {
                            System.out.println("Incorrect Email or Password");
                        }
                        break;
                    case 3:
                        System.out.println("Thank you for using Bank Management System");
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Enter Valid Choice");
                        break;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void performTransactions(AccountManager accountManager, long accNum, Scanner scanner) throws SQLException {
        int choice2 = 0;
        while (choice2 != 5) {
            System.out.println();
            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Log out");
            System.out.println("Enter your choice: \t");
            choice2 = scanner.nextInt();

            switch (choice2) {
                case 1:
                    accountManager.debitMoney(accNum);
                    break;
                case 2:
                    accountManager.creditMoney(accNum);
                    break;
                case 3:
                    accountManager.transferMoney(accNum);
                    break;
                case 4:
                    accountManager.getBalance(accNum);
                    break;
                case 5:
                    break;
                default:
                    System.out.println("Enter Valid Choice");
                    break;
            }
        }
    }
}
