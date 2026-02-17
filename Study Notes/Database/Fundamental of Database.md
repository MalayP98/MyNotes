
# Transaction
- Transaction: Collection of query that is treated as one unit of work.
- Every transaction start with *BEGIN* and we can *COMMIT* or *ROLLBACK* it.
- There can be multiple strategies to commit the queries, lets say we have 1000 queries in a txn -
  1. Write each query to the disk
  2. Write each query to memory and once all queries are done then update the disk
- We can also have to rollback the queries of DB crashes, so when DB comes up it must remember to rollback the data

# ACID

## Atomicity
A txn happens completely or not at all. Even if just one query fails we need to rollback all the previous successful queries.

## Isolation
Isolation controls how and when the changes made by one txn can be visible to another txn. It kinda say how much will/can you isolate multiple txn from each other.

### Read Phenomenons

#### Dirty Reads
When a txn read the uncommitted change made by another txn and this txn decides to rollback its changes. In this we have some thing which is not in the DB.
Example,

Txn A begins, set balance to 500. Txn B begin reads balance as 500 and then Txn A rollbacks. So now the balance is 1000 but Txn B has read 500.

| id  | balance |
| --- | ------- |
| 1   | 1000    |

**Transaction A**: 

```sql
UPDATE accounts SET balance = 500 WHERE id = 1; -- Not committed yet
```

Now balance (internally) is 500 but not committed.

**Transaction B**

```sql
SELECT balance FROM accounts WHERE id = 1;
```

B sees **500**.

Now A does:

`ROLLBACK;`

#### Non Repeatable Reads
If a txn reads a data multiple times and get different results because some other txn has committed their changes, this is called non-repeatable reads.
Example, 
TxnA checks balance which comes out to be 1000, TxnB starts and commits the balance to 2000. TxnA checks the balance again and finds out it 2000. But first it read the balance as 1000.

|id|balance|
|---|---|
|1|1000|

##### Transaction A

```sql
BEGIN; SELECT balance FROM accounts WHERE id = 1; -- Returns 1000
```

##### Transaction B

```sql
UPDATE accounts SET balance = 2000 WHERE id = 1; COMMIT;
```

##### Back to Transaction A

```sql
SELECT balance FROM accounts WHERE id = 1; -- Returns 2000
```


#### Phantom Reads
When a txn reads a table multiple time and the numbers of rows in the result varies because some other txn has added or removed the rows.
Example, 
Txn A check the employee table and get a row as a result. Txn B adds a new row and commits the txn. Txn A reads the employee table again and this time get 2 rows as result.

|id|salary|
|---|---|
|1|4000|
|2|6000|

##### Transaction A

```sql
BEGIN; SELECT * FROM employees WHERE salary > 5000;
```

Returns:

`id=2, salary=6000`

##### Transaction B

```sql
INSERT INTO employees VALUES (3, 7000); COMMIT;
```

##### Back to Transaction A

```sql
SELECT * FROM employees WHERE salary > 5000;
```

Now returns:

`id=2, salary=6000 id=3, salary=7000`

#### Lost Updates
When 2 txns overwrite each others changes
Example, 
Initial balance is 1000 in a DB. Txn A wants to add 500 and Txn B want to add 300. Correct result should be the balance increasing by 800. But as both the txn read the same value of 1000, they both add extra on top of 1000. So the result will be either 1500 or 1300 depending on which txn commits first. One of the txn's update will be lost.

##### Transaction A

```sql
SELECT balance FROM accounts WHERE id = 1; -- 1000
```

#####  Transaction B

```sql
SELECT balance FROM accounts WHERE id = 1; -- 1000
```

Both think balance = 1000.

#####  Transaction A

Adds 500:

```sql
UPDATE accounts SET balance = 1500 WHERE id = 1; COMMIT;
```

#####  Transaction B

Adds 300 (based on old value 1000):

```sql
UPDATE accounts SET balance = 1300 WHERE id = 1; COMMIT;
```

Final balance = **1300**

But correct balance should be **1800**

Transaction A's update is lost.

[[Study Notes/Database/ChatGPT Responses/Response 1|Response 1]] - check this for more.

### Isolation Levels
Isolation levels tell how much can multiple txn can see each others update.

#### Read Uncommitted
Means that a txn can read uncommitted changes from other txn. This the weakest isolation level as it allows every read phenomenon.

#### Read Committed
Means that a txn can read committed changes from other txn. Prevents *dirty reads* as they happen because of reading uncommitted changes.

#### Repeatable Reads
Makes sure that if a txn reads a row it will see that same row throughout its txn *even if other txn modify that data*. This possible by taking snapshot. Prevents dirty and non-repeatable reads. May also prevent phantom reads but that depends on the database. [[Response 2]]

#### Serializable
It make concurrent txn behave as synchronised. It prevents all the read phenomenon. It is achieved by either heavy locking or using snapshots with conflict detection.
Cons: 
1. Low concurrency(Blocking)
2. Deadlocks

#### Complete Comparison Table

| Isolation Level  | Dirty Read | Non-Repeatable Read | Phantom Read | Write Skew  |     |
| ---------------- | ---------- | ------------------- | ------------ | ----------- | --- |
| Read Uncommitted | ✅ Yes      | ✅ Yes               | ✅ Yes        | ✅ Yes       |     |
| Read Committed   | ❌ No       | ✅ Yes               | ✅ Yes        | ✅ Yes       |     |
| Repeatable Read  | ❌ No       | ❌ No                | ⚠️ Depends   | ⚠️ Possible |     |
| Serializable     | ❌ No       | ❌ No                | ❌ No         | ❌ No        |     |

#### Real World Mapping

| Use Case                               | Recommended Isolation |
| -------------------------------------- | --------------------- |
| Banking transfers                      | Serializable          |
| E-commerce checkout                    | Repeatable Read       |
| Reporting systems                      | Read Committed        |
| Analytics where minor inconsistency ok | Read Committed        |

## Consistency
Consistency ensures that after a committed transaction, the database satisfies all defined constraints. *The database transitions from one valid state to another valid state*
These constrains/rules can be any of,
- Primary key constraints
- Foreign key constraints
- Unique constraints
- Check constraints
- Triggers
- Application-level invariants
- Business rules