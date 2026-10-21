import sqlite3
from pathlib import Path
db = Path(__file__).resolve().parents[1] / 'database' / 'chess_tests.db'
with sqlite3.connect(db) as con:
    con.execute('UPDATE test_cases SET expected_any = 1 - expected_any WHERE id = 1')
    print('Test #1 expected_any:', con.execute('SELECT expected_any FROM test_cases WHERE id = 1').fetchone()[0])
