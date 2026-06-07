**Partitioning** - when the data is divided on a single machine
**Sharding** - when data is divided across multiple machines/servers

## Choosing shard key
**High cardinality** - key should have many different unique values, like `user_id`
**Even Distribution** - keys should be evenly distributed; if we shard by country and most of our user are in same country then that shard be become hot
**Match sharding with your queries** - Split data in a way that most queries only need one shard.

## Sharding Strategies
**Range-Based** - we define a range of shard key to each shard; good for multi tenant systems
```
Shard 1 → User IDs 1–1M
Shard 2 → User IDs 1M–2M
Shard 3 → User IDs 2M–3M
```

**Hash-Based** - we give shard key to a hash method and this method then returns the shard; problems when adding or removing a shard as it leads to a lot of data migration across shards; *defaults*; **==use consistent hashing to optimize==**

**Directory-Based** - uses lookup table to check in which shard the data is present; slow as every query requires look; **==directory service becomes SPF==**

## Sharding Challenges

**Hot Spot** - *celebrity problem* can make a specific shard bear all the load; time-based sharding create a write-load to the most recent shard;
> Time-based sharding creates a different kind of hot spot. If you shard by creation date, all new writes go to the most recent shard. That shard handles all the write traffic while older shards sit mostly idle handling only reads of historical data.

detect hot spots by monitoring shard metrics like query latency, CPU usage, and request volume;

**Handling Hot-Spots**

- **Isolate hot keys:** Move high-traffic users/data (e.g., celebrity accounts) to a *separate shard* to reduce load on others.

- **Use compound shard keys:** Combine fields (e.g., `user_id + date`) to *spread heavy traffic across multiple shards* instead of one. This spreads a single user's data across multiple shards over time, which helps if the hot spot is both high volume and spans time periods

- **Split shards dynamically:** When a shard becomes too big or busy, *split and rebalance it* (automatically in some DBs like MongoDB, or manually in systems like [Vitess](https://vitess.io)).


**Cross-Shard Operation** - operation which need to query all the shards; example, get top 10 posts, for this application have to go to all the shards to get the data and aggregate the result.

**Other problems -** ***==Joins, Maintaining Transaction, Rollbacks==***
``
**Solutions** -

- **Cache** - cache the result of cross-shard queries; first query -> expensive, next queries -> fast; if the result is stored in cache and a DB update happens then user might not see the updated result immediately, so this solution is good for eventually consistent systems
  
- **De-normalize** - keep related data together; if sharding is done on `user_id` then keep user related data like post, comments on user's shard so that app does not have to query different shards to get user info; **==duplicates data and updates are complicated update but is worth the trade-off==**

- **Acceptance** - if a query has to move across shard for data but the get rarely executed it fine 🙂‍↕️ 🙃

**Maintaining Consistency** - if user data is on one shard and transaction data is on other shard and there a method which wants to update both the table it cannot happen in a single *transaction*

**Solution** - 

**2 Phase Commit (2PC)** - a coordinator makes sure all the transaction are ready in all the shards, when ready it asks everyone to commit; *not used it production as coordinator becomes SPF*

**Design** - design in such a way that all related data is on same shard; *same as de-normalization*

**SAGA** - rather than a single transaction, the process is broken into different transactions and if a transaction fails, a compensating  action is performed; example, if the order fails, first a method called `removeOrderFromUser` might next on payment service a method called `refundToUser` might run and so on.

> **SAGA = split + execute + rollback in reverse using compensations**


## When to use sharding?

- When the storage increase, writes increase and even read replicas are not able to handle reads; if any one these happens introduce sharding.
- What shard key to choose -> Sharding strategy and why -> List Trade-Offs -> How to handle growth3  