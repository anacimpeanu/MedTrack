import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MIGRATION_PATH = ROOT / "db" / "migrations" / "001_init.sql"
SEED_PATH = ROOT / "db" / "seeds" / "001_seed_minimal.sql"
DB_PATH = ROOT / "medtrack_dev.db"


def apply_sql_file(connection: sqlite3.Connection, path: Path) -> None:
    sql = path.read_text(encoding="utf-8")
    connection.executescript(sql)


def main() -> None:
    if DB_PATH.exists():
        DB_PATH.unlink()

    with sqlite3.connect(DB_PATH) as conn:
        conn.execute("PRAGMA foreign_keys = ON;")
        apply_sql_file(conn, MIGRATION_PATH)
        apply_sql_file(conn, SEED_PATH)

        table_count = conn.execute(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
        ).fetchone()[0]

        user_count = conn.execute("SELECT COUNT(*) FROM users;").fetchone()[0]
        patient_count = conn.execute("SELECT COUNT(*) FROM patients;").fetchone()[0]

    print(f"OK: created {DB_PATH.name}")
    print(f"OK: tables={table_count}, users={user_count}, patients={patient_count}")


if __name__ == "__main__":
    main()

