INSERT IGNORE INTO `user` (record_last_updated, username, password, first_name, last_name,
                           externally_managed, timezone, locale, email, enabled, deleted)
VALUES (CURRENT_TIMESTAMP, 'git_repo_user', '',
        'Git Repo', 'User', 0, 'EST5EDT', 'en/US', 'evalgit@example.com', 1, 0) ;

UPDATE _meta SET version = '3.2.0.4' ;