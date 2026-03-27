PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS _test_results;
CREATE TEMP TABLE _test_results (
    test_name TEXT PRIMARY KEY,
    passed INTEGER NOT NULL,
    details TEXT NOT NULL
);

INSERT INTO _test_results (test_name, passed, details)
SELECT
    'table_count',
    CASE WHEN (
        SELECT COUNT(*)
        FROM sqlite_master
        WHERE type = 'table' AND name NOT LIKE 'sqlite_%'
    ) = 8 THEN 1 ELSE 0 END,
    'Expected exactly 8 application tables';

INSERT INTO _test_results (test_name, passed, details)
SELECT
    'seed_users_count',
    CASE WHEN (SELECT COUNT(*) FROM users) = 1 THEN 1 ELSE 0 END,
    'Expected exactly 1 seeded user';

INSERT INTO _test_results (test_name, passed, details)
SELECT
    'seed_patients_count',
    CASE WHEN (SELECT COUNT(*) FROM patients) = 1 THEN 1 ELSE 0 END,
    'Expected exactly 1 seeded patient';

INSERT INTO _test_results (test_name, passed, details)
SELECT
    'integrity_check',
    CASE WHEN (
        SELECT group_concat(integrity_check, ',')
        FROM pragma_integrity_check
    ) = 'ok' THEN 1 ELSE 0 END,
    'PRAGMA integrity_check must return ok';

INSERT INTO _test_results (test_name, passed, details)
SELECT
    'foreign_key_check',
    CASE WHEN (SELECT COUNT(*) FROM pragma_foreign_key_check) = 0 THEN 1 ELSE 0 END,
    'PRAGMA foreign_key_check must return no rows';

SELECT
    'FAIL | ' || test_name || ' | ' || details
FROM _test_results
WHERE passed = 0;

SELECT 'ALL_TESTS_PASSED'
WHERE NOT EXISTS (SELECT 1 FROM _test_results WHERE passed = 0);

