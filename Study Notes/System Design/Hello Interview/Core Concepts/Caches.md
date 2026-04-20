- Redis or Memcache are not the only caches, we also have *browser cache, CDN cache, app cache, DBs also have caches*.

## Type of caches

**CDN** - use CDN when the app has to server static data at scale.

**Client-Side** - app does not have much control, invalidation is hard and data can go stale, data is cached in browser (HTTP cache, localStorage) or mobile app using local memory or on-device storage.

**In-Process** - cache on the application itself, like using Maps or something, cache common configs, feature flags etc, *in distributed system if one server invalidates a key other server won't know*, in interview use this as optimization once you have added external cache.

## Cache Architectures

**Cache-Aside** - *default in iw*, app checks the cache, if data is there -> return it, else fetch from DB, store in cache and return it; caches only data we need, keeps the cache lean; cache miss causes extra latency

**Write-Through** - app write only to cache, cache synchronously write data to the DB; if transaction is not complete until both cache and DB are updated; *slower writes*; 

> If the cache update succeeds but the database write fails, or vice versa, the systems can end up inconsistent. You need retry logic, error handling, or eventually accept that perfect consistency is difficult without distributed transactions.

Use this when **reads must always return fresh data** and your system can tolerate slightly slower writes

<img src="Study Notes/System Design/Hello Interview/resource/Images/Example2.png" width=600 height=200/>  
**Write-Back / Write-Behind** - app writes everything to the *cache*, cache then asynchronously writes data to DB; we can have data loss if cache crashed be writing data to DB; used where slight data loss and eventual consistency is acceptable and app need high write speeds

**Read-Through** - if there is a cache miss cache itself reads data from DB -> stores it -> returns the data; CDN is a kind of read-through cache


## Cache Eviction policies

**Least Recently Used (LRU)** - removes data which was not access for the longest time
**Least Frequently Used (LRU)** - remove data which has been *accessed least number of times*
**FIFO** - removes oldest item add; may remove a hot item, not used in system
**TTL** - remove item after a decided time; used together with LRU and LFU; TTL is a must have when data must eventually refresh, like API responses or session tokens

## Caching Problems

### Cache Stampede

When a hot key is evicted and lots of request miss the cache at the same time and go to the database. Also called *Thundering Heard*. 

#### Solution
**Request coalescing** - rather than all the failed requests that have missed the cache only one request goes to the DB to fetch data and other requests wait; *most effective solution*

**Cache Warming** - refresh the hot keys before they expire; works for TTL eviction policies not for invalidation on writes; more here  [[Cache Warming]]

### Cache Consistency

Happens when cache and DB return different values because app reads from cache write into DB. 

#### Solution
**Invalidate cache on update/write** - Invalidate cache entry when corresponding database entry is modified so it gets cache with fresh data.

**Short TTLs** - If TTL is short cache entries will be remove frequently thus fresh data is cached frequently.

**Accept eventual consistency** - Updates propagate asynchronously from the database to cache and replicas, so during that time some users see the latest data while others still see stale data. *So users should just accept it* 😛.

### Hot Keys
A single hot key can overload a cache node in a distributed system.

#### Solution
 **Replicate hot keys:** Store the same value on multiple cache nodes and load balance reads across them.
 
**Add a local fallback cache:** Keep extremely hot values in-process to avoid pounding Redis.

**Apply rate limiting:** Slow down abusive traffic patterns on specific keys.



#
> **When to bring up cache?**
> 
> - **Lots of reads:** Use cache to avoid hitting the database again and again.
> - **Slow/complex queries:** Cache results so you don’t recompute every time.
> - **High DB load:** Cache repeated queries to reduce database work.
> - **Need fast responses:** Use cache to return data quickly instead of waiting on DB.
> 
> **How to introduce caching**
> 
> - **Identify the bottleneck:** Clearly point out _what exactly is slow and why_ (e.g., “user profile API is hitting DB 500 req/sec, each taking ~30ms → DB becomes bottleneck”).
> - **Decide what to cache:** Cache only **high-read, low-update, expensive data** (e.g., user profiles, trending feeds). Also define **clear cache keys** like `user:123:profile` or `trending:posts:global` for easy lookup.
> - **Choose cache strategy/architecture:** Pick a pattern based on consistency needs — most common is **cache-aside** (check cache → fallback to DB → store in cache). Mention alternatives like **write-through / write-behind**, and layers like **Redis (distributed), in-process cache (for hot keys), CDN (for static content)**.
> - **Set eviction & freshness rules:** Use **TTL (to avoid stale data)** + **LRU (to control size)**. Also handle updates explicitly (e.g., delete or refresh cache when user updates profile).
> - **Handle real-world challenges:**
>     - **Cache invalidation:** Keep data fresh (invalidate on writes or rely on TTL).
>     - **Cache failures:** Fallback to DB + use circuit breakers to avoid overload.
>     - **Thundering herd:** Prevent many requests hitting DB at once (use request coalescing or early expiration).
> 
