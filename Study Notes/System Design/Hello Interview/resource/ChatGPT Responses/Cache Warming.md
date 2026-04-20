## 🔥 What is _cache warming_?

Cache warming means:

> **Refreshing frequently-used (hot) data in the cache _before_ it expires**, so users never hit a cold cache.

Example:

- A product page is cached with TTL = 5 minutes
- Instead of letting it expire at 5:00 → you refresh it at 4:50
- So users always get fast responses from cache

---

## ⏱️ Case 1: TTL-based expiration (where warming helps)

With TTL (Time-To-Live):

- Cache entry expires automatically after some time
- At expiry → many requests may hit DB at once ❌ (**cache stampede**)

### Without warming:

Time 5:00 → cache expires  
1000 users request → all go to DB 😬

### With warming:

Time 4:50 → background job refreshes cache  
Time 5:00 → cache still valid ✅  
Users → always hit cache

👉 So warming **prevents stampede** here.

---

## ❌ Case 2: Write-based invalidation (where warming doesn’t help)

Here, cache is not time-based. Instead:

- When data changes → you **invalidate (delete)** cache

Example:

User updates profile → cache entry deleted  
Next request → cache miss → DB hit → cache repopulated

### Problem:

- You **don’t know when invalidation will happen**
- It’s triggered by writes, not time

So:

- You cannot “pre-warm” before invalidation
- Stampede can still happen if many reads come right after invalidation

Cache invalidated at random time  
1000 users request → all hit DB 😬

👉 Cache warming is useless here because:

> There’s no predictable expiry to refresh ahead of time

---

## 💡 Key Insight

- **Cache warming only works when expiry is predictable (TTL)**
- It **does NOT work when invalidation is event-driven (writes)**

---

## 🧠 So how do you prevent stampede with write invalidation?

You use other techniques:

- **Request coalescing** (only 1 request hits DB, others wait)
- **Locks / mutex**
- **Stale-while-revalidate**
- **Read-through caching with deduplication**

---

## ✅ One-line takeaway

> Cache warming prevents stampedes only when cache expiry is predictable (TTL), but not when cache is invalidated unpredictably on writes.