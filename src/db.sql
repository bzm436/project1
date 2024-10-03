DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS AccessLogs;
DROP TABLE IF EXISTS UserTypes;

CREATE TABLE UserTypes (
    user_type_id INT PRIMARY KEY,
    user_type_name VARCHAR(50) NOT NULL
);

CREATE TABLE Users (
    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    user_type_id INT,
    status VARCHAR(20),
    FOREIGN KEY (user_type_id) REFERENCES UserTypes(user_type_id)
);



CREATE TABLE AccessLogs (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT,
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES Users(student_id)
);


INSERT INTO UserTypes (user_type_id, user_type_name) VALUES (1, 'Student');
INSERT INTO UserTypes (user_type_id, user_type_name) VALUES (2, 'Faculty Member');
INSERT INTO UserTypes (user_type_id, user_type_name) VALUES (3, 'Staff Member');
INSERT INTO UserTypes (user_type_id, user_type_name) VALUES (4, 'Janitor');

INSERT INTO Users (name, email, user_type_id, status) VALUES ('Alice Johnson', 'alice.johnson@example.com', 1, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Bob Smith', 'bob.smith@example.com', 1, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Charlie Brown', 'charlie.brown@example.com', 1, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Dr. Emily White', 'emily.white@example.com', 2, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Prof. John Doe', 'john.doe@example.com', 2, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Mary Johnson', 'mary.johnson@example.com', 3, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('James Williams', 'james.williams@example.com', 3, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Janet Miller', 'janet.miller@example.com', 4, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Patrick Wilson', 'patrick.wilson@example.com', 4, 'active');
INSERT INTO Users (name, email, user_type_id, status) VALUES ('Daisy Evans', 'daisy.evans@example.com', 1, 'suspended');
