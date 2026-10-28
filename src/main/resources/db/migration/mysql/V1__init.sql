CREATE TABLE jobs
(
    id       INT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(50) NOT NULL
);

CREATE TABLE job_statuses
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    job_status VARCHAR(50) NOT NULL
);

INSERT INTO job_statuses (job_status)
VALUES ('PENDING'),
       ('IN_PROGRESS'),
       ('COMPLETED'),
       ('FAILED');

CREATE TABLE job_executions
(
    id              INT PRIMARY KEY AUTO_INCREMENT,
    job_id          INT      NOT NULL,
    job_status_id   INT      NOT NULL,
    start_timestamp DATETIME NOT NULL DEFAULT NOW(),
    end_timestamp   DATETIME,

    CONSTRAINT fk_job_id FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_job_status FOREIGN KEY (job_status_id) REFERENCES job_statuses (id)
);

