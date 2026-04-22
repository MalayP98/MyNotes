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

- **Split shards dynamically:** When a shard becomes too big or busy, *split and rebalance it* (automatically in some DBs like MongoDB, or manually in systems like Vitess).





