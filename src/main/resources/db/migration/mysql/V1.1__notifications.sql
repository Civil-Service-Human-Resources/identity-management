CREATE TABLE job_notification_types
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    notification_type VARCHAR(50) NOT NULL
);

INSERT INTO job_notification_types (notification_type)
VALUES ('DEACTIVATE'),
       ('DELETION_WARNING'),
       ('DELETE'),
       ('REQUIRED_LEARNING_DUE');

CREATE TABLE job_notifications
(
    id                   INT PRIMARY KEY AUTO_INCREMENT,
    job_execution_id     INT         NOT NULL,
    notification_type_id INT         NOT NULL,
    sent_timestamp       DATETIME    NOT NULL DEFAULT NOW(),
    user_id              VARCHAR(50) NOT NULL
);
