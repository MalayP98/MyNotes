In MySQL InnoDB:  
Repeatable Read also prevents phantom reads using **next-key locking**.

In PostgreSQL:  
Repeatable Read uses snapshot isolation and can still allow write skew.

Interview gold line:

Repeatable Read ensures row-level stability, not full predicate stability.