package BankManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class AccountManager {
    private final Connection connection;
    private final Scanner scanner;

    AccountManager(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void creditMoney(long accNum) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter amount to credit: \t");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin:\t");
        String securityPin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (accNum != 0) {
                String findQuery = "SELECT * FROM accounts WHERE AccNum = ?";
                PreparedStatement findPreparedStatement = connection.prepareStatement(findQuery);
                findPreparedStatement.setLong(1, accNum);
                ResultSet resultSet = findPreparedStatement.executeQuery();

                if (resultSet.next()) {
                    String storedHashedSecPin = resultSet.getString("secPin");
                    String storedSalt = resultSet.getString("secPinSalt");
                    String hashedInputSecPin = DBUtils.hashPassword(securityPin, storedSalt);

                    if (storedHashedSecPin.equals(hashedInputSecPin)) {
                        String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE AccNum = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(creditQuery);
                        updateStatement.setDouble(1, amount);
                        updateStatement.setLong(2, accNum);

                        int rowsAffected = updateStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("₹" + amount + " created successfully.");
                            connection.commit();
                        } else {
                            System.out.println("Transaction failed!!!");
                            connection.rollback();
                        }
                    } else {
                        System.out.println("Invalid Security Pin!");
                    }
                } else {
                    System.out.println("Account does not exist.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void debitMoney(long accNum) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter amount to debit: \t");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin:\t");
        String secPin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (accNum != 0) {
                String findQuery = "SELECT * FROM accounts WHERE AccNum = ?";
                PreparedStatement findPreparedStatement = connection.prepareStatement(findQuery);
                findPreparedStatement.setLong(1, accNum);
                ResultSet resultSet = findPreparedStatement.executeQuery();

                if (resultSet.next()) {
                    String storedHashedSecPin = resultSet.getString("secPin");
                    String storedSalt = resultSet.getString("secPinSalt");
                    String hashedInputSecPin = DBUtils.hashPassword(secPin, storedSalt);

                    if (storedHashedSecPin.equals(hashedInputSecPin)) {
                        double currentBalance = resultSet.getDouble("balance");
                        if (amount <= currentBalance) {
                            String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE AccNum = ?";
                            PreparedStatement updateStatement = connection.prepareStatement(debitQuery);
                            updateStatement.setDouble(1, amount);
                            updateStatement.setLong(2, accNum);

                            int rowsAffected = updateStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("₹" + amount + " debited successfully.");
                                connection.commit();
                            } else {
                                System.out.println("Transaction failed!!!");
                                connection.rollback();
                            }
                        } else {
                            System.out.println("Insufficient balance");
                        }
                    } else {
                        System.out.println("Invalid Security Pin!");
                    }
                } else {
                    System.out.println("Invalid Account Number or Pin!!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void transferMoney(long fromAccNum) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter Receiver account number: \t");
        long toAccNum = scanner.nextLong();
        System.out.println("Enter Amount: \t");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin:\t");
        String secPin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (fromAccNum != 0 && toAccNum != 0) {
                String findSenderQuery = "SELECT * FROM accounts WHERE AccNum = ?";
                PreparedStatement findPreparedStatement = connection.prepareStatement(findSenderQuery);
                findPreparedStatement.setLong(1, fromAccNum);
                ResultSet senderResultSet = findPreparedStatement.executeQuery();

                if (senderResultSet.next()) {
                    String storedHashedSecPin = senderResultSet.getString("secPin");
                    String storedSalt = senderResultSet.getString("secPinSalt");
                    String hashedInputSecPin = DBUtils.hashPassword(secPin, storedSalt);

                    if (storedHashedSecPin.equals(hashedInputSecPin)) {
                        double currentBalance = senderResultSet.getDouble("balance");
                        if (amount <= currentBalance) {

                            String findReceiverQuery = "SELECT * FROM accounts WHERE AccNum = ?";
                            PreparedStatement findReceiverPreparedStatement = connection.prepareStatement(findReceiverQuery);
                            findReceiverPreparedStatement.setLong(1, toAccNum);
                            ResultSet receiverResultSet = findReceiverPreparedStatement.executeQuery();

                            if (receiverResultSet.next()) {

                                String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE accNum = ?";
                                String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE accNum = ?";

                                PreparedStatement creditPreparedStatement = connection.prepareStatement(creditQuery);
                                PreparedStatement debitPreparedStatement = connection.prepareStatement(debitQuery);

                                debitPreparedStatement.setDouble(1, amount);
                                debitPreparedStatement.setLong(2, fromAccNum);
                                creditPreparedStatement.setDouble(1, amount);
                                creditPreparedStatement.setLong(2, toAccNum);

                                int rowsAffected1 = debitPreparedStatement.executeUpdate();
                                int rowsAffected2 = creditPreparedStatement.executeUpdate();

                                if (rowsAffected1 > 0 && rowsAffected2 > 0) {
                                    System.out.println("₹" + amount + " transferred successfully.");
                                    connection.commit();
                                } else {
                                    System.out.println("Transaction failed!!!");
                                    connection.rollback();
                                }
                            } else {
                                System.out.println("Receiver account does not exist!!!!");
                            }
                        } else {
                            System.out.println("Insufficient balance!!!");
                        }
                    } else {
                        System.out.println("Invalid Security Pin!");
                    }
                } else {
                    System.out.println("Invalid Account Number or Pin!!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }


    public void getBalance(long accNum) throws SQLException {
        scanner.nextLine();
        System.out.println("Enter Security Pin: \t");
        String secPin = scanner.nextLine();

        try {
            String findQuery = "SELECT * FROM accounts WHERE accNum = ?";
            PreparedStatement findPreparedStatement = connection.prepareStatement(findQuery);
            findPreparedStatement.setLong(1, accNum);
            ResultSet resultSet = findPreparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedHashedSecPin = resultSet.getString("secPin");
                String storedSalt = resultSet.getString("secPinSalt");
                String hashedInputSecPin = DBUtils.hashPassword(secPin, storedSalt);

                if (storedHashedSecPin.equals(hashedInputSecPin)) {
                    double currentBalance = resultSet.getDouble("balance");
                    if (accNum != 0)
                        System.out.println("Balance: " + currentBalance);
                } else {
                    System.out.println("Invalid Security Pin!");
                }
            } else {
                System.out.println("Invalid Account Number");
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
