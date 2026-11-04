CREATE TABLE required_learning_reminder_organisation_unit_actions
(
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    job_execution_id      INT          NOT NULL,
    organisation_id       INT          NOT NULL,
    course_id             VARCHAR(50)  NOT NULL,
    course_title          VARCHAR(255) NOT NULL,
    learning_period_start DATETIME     NOT NULL,
    learning_period_end   DATETIME     NOT NULL,
    processed_timestamp   DATETIME,

    CONSTRAINT fk_rl_job_execution FOREIGN KEY (job_execution_id) REFERENCES job_executions (id)
);

CREATE TABLE required_learning_reminder_notifications
(
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    job_notification_id INT         NOT NULL,
    course_id           VARCHAR(50) NOT NULL,

    CONSTRAINT fk_rl_job_notification FOREIGN KEY (job_notification_id) REFERENCES job_notifications (id),
    CONSTRAINT uk_notification_course UNIQUE (job_notification_id, course_id)
);

INSERT INTO jobs (job_name)
VALUES ('REQUIRED_LEARNING_NOTIFICATION_PRODUCER');
