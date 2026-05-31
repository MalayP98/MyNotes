```sql
-- Create table
CREATE TABLE student (
    id INT PRIMARY KEY,
    grade INT,
    name VARCHAR(100)
);

-- Insert 1 million rows
INSERT INTO student (id, grade, name)
SELECT
    gs AS id,
    FLOOR(RANDOM() * 10 + 1)::INT AS grade,
    (
        ARRAY[
            'John', 'Alice', 'Bob', 'Charlie', 'David',
            'Emma', 'Sophia', 'Liam', 'Noah', 'Olivia',
            'Ava', 'Mason', 'Lucas', 'Mia', 'Ethan'
        ]
    )[FLOOR(RANDOM() * 15 + 1)] AS name
FROM generate_series(1, 1000000) gs;
```