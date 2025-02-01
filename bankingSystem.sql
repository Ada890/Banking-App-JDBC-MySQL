CREATE DATABASE bankingSystem;
USE bankingSystem;

CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100),
                       email VARCHAR(100) UNIQUE,
                       password VARCHAR(256),
                       salt VARCHAR(256)
);

CREATE TABLE accounts (
                          accNum BIGINT PRIMARY KEY,
                          name VARCHAR(100),
                          email VARCHAR(100),
                          balance DOUBLE DEFAULT 0,
                          secPin VARCHAR(256),
                          secPinSalt VARCHAR(256),
                          FOREIGN KEY (email) REFERENCES users(email)
);
