# 5️⃣ Write Skew (Advanced / MVCC systems)

This happens mostly in snapshot isolation systems.

Two transactions:

- Read same data
    
- Make decisions based on it
    
- Update different rows
    
- Violate constraint together
    

Example:

- Rule: At least 1 doctor must be on call.
    
- Two doctors check if someone else is on call → both see yes.
    
- Both mark themselves off-call.
    
- Now zero doctors on call.
    

Constraint broken 😬

---

# Summary Table

|Phenomenon|What Changes?|Happens Due To|
|---|---|---|
|Dirty Read|Uncommitted data read|Weak isolation|
|Non-Repeatable Read|Same row value changes|Concurrent update|
|Phantom Read|New rows appear/disappear|Concurrent insert/delete|
|Lost Update|One update overwrites another|Missing locking|
|Write Skew|Constraint violated logically|Snapshot isolation|