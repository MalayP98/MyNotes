- **Consistency (C):** All nodes see the same data at the same time
- **Availability (A):** Every request gets a response (even if data is stale)
- **Partition Tolerance (P):** System keeps working despite network failures

In reality, **Partition Tolerance is mandatory**, so the real tradeoff is **Consistency vs Availability during network failures**

If stale data is catastrophic → choose **Consistency**, Ticket booking,  banks, inventories
If not → choose **Availability**, Social media, review sites

In real work usually both *Consistency* and *Availability* is used for different type of services.

## Types of Consistency

- **Strong:** Always latest data
- **Causal:** Correct order of events
- **Read-your-own-writes:** You see your updates immediately
- **Eventual:** Data becomes consistent over time


