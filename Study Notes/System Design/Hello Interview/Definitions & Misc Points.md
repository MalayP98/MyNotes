- WebSocket and SSE(Server Side Events) are considers stateful connections because the connection remains open and the server **maintains context about the client**
- SSE is unidirectional, client make initial request to open connection and then server pushes data to the client. 
- If data is structured used SQL, if data is dynamic used NoSQL
- Normalization introduced joins which can be slow. 
  De-normalization is expensive if you want to update data. For example, 
  There is a denormalized table like so,
  
  | ProductID | UserName |
  |---|---|
  | 1 | testuser |
  | 2 | testuser |
  | 3 | testuser |
  
  Now if the `testuser` changes their name to `testuser1` then we will have to update rows.
  
  If this table was normalized like this,
  
  | ProductID | UserID |
  |---|---|
  | 1 | 10 |
  | 2 | 10 |
  | 3 | 10 |
  
  | UserID | Username |
  |---|---|
  | 10 | testuser |
  
  In this case we just have to change the *Username* in the User table once.
- A safe approach in interview is to start with Normalized DB and then Denormalize it if required.
- In **NoSQL databases**, a **partition key ** is the field used to ** determine where data is stored across different nodes or partitions in a distributed database**.
- If you are using NoSQL DB then *you should know all the queries around the data that can happen*. You design the DB based on these expected queries. For example, you are designing Tweeter, your most common query is `get all post from user_id=X` so you will design you partition keys based on user ID. If somebody know says they want all the posts with `hashtag #Y` then in this case you will have to traverse all the posts.
- Cache Stampede: When the cache server goes down and all the traffic comes to DB.
- Use NoSQL when,
  -> data model is evolving and you need to store different type of data structures
  -> when you need to do horizontal scaling(not sure on this)
  -> you app is dealing large volume of unstructured data
- **Cassandra** is good choice for *write-heavy* apps
- When you have to store big files like images, video or large text file use **Blob storage** like ==S3, GCS==. You store data in blob storage and reference to this data in your database.
- Search Optimized Database -> Elasticsearch, Pgres's GIN indexes. Tech involved in SODs,
  - Inverted Index - word to doc kind of indexes, example
    ```json
    {
	  "word1": [doc1, doc2, doc3],
	  "word2": [doc2, doc3, doc4],
	  "word3": [doc1, doc3, doc4]
	}
    ```
  - Tokenization - process of breaking text to individual words
  - Stemming - `running` and `runs` both become `run`
  - Fuzzy Search - tolerating slight misspellings, uses Edit Distance.
-  If you are using websockets the use L 4 ,Load balancer else L 7 Load balancer
- **Backpressure** - a mechanism used when producer is producing at a faster rate than consumer is consuming. 
  Some backpressure strategies: 
  - Blocking producer until queue gets empty
  - Rejecting new messages
  - Dropping latest, oldest or random messages
  - Consumer request new message only when it done processing existing messages
- *Backpressure* is ==reaction based==, if consumer becomes slow it asks producer to slow down. Where as *throttling* is proactive, it puts in a limit by default, example, the server has a rate limit of 10 req/sec. ==Throttling and rate limiting are same can be used interchangeably==.
- Use Messaging Queues when,
  - Burst of traffic -> in ride sharing app when may ride request come at once
  - To distribute work load -> if `n` operations are to be performed they can be put into a queue and can be taken up one at a time or by diff threads.
- Distributed Lock - locking single or a group of resource across the system. For example, when buying any item online if the user is in middle of making payment for the item it should not be sold to anyone else. So we lock this item for a certain period of time like 10 mins. Can be achieved using Redis. Be ware of ==deadlocks==.
 
#### Diff between Webhook and SSE

**Webhook** - it is an ==event (an HTTP request)== that get triggers when something happens. 
For example, you app is some payment gateway. That payment app/gateway is responsible to collect the amount from the user/customer. Lets say the payment window is active for 5 min, how will your app know that the payment is successful? 
*First way* - keep calling the payment gets API to check if the payment is credit to your bank. This will be polling and will be inefficient.
*Second way* - When the payment is successful the payment gateway/app should your apps API telling your app that the customer has made the payment.

**SSE** - client send a request to open a connection. The server open a connection and start send the data. This is a unidirectional communication. Only server sends the data. 
This like a radio, user starts a radio, tune in to a channel and consume the information from the radio.

Check this -> [[Webhook & SSE Analogy]]

#### Cache Invalidation

Cache invalidation ensures **cached data stays consistent with the database** when the data is updated or removed.

| Policy                        | How It Works                                          | Read Speed                        | Write Speed | Notes                                        |
| ----------------------------- | ----------------------------------------------------- | --------------------------------- | ----------- | -------------------------------------------- |
| **TTL (Time-To-Live)**        | Cache entry expires after a fixed time                | Fast                              | Fast        | Simple but may serve stale data until expiry |
| **Write-through**             | Write updates cache and DB together                   | Fast                              | Slower      | Strong consistency                           |
| **Write-around**              | Write goes directly to DB, cache updated on next read | First read slow, later reads fast | Fast        | Avoids caching unnecessary data              |
| **Write-back (Write-behind)** | Write goes to cache first, DB updated asynchronously  | Fast                              | Very fast   | Risk of data loss if cache crashes           |
| **Explicit Invalidation**     | On DB update, cache entry is deleted/updated          | Fast after refresh                | Normal      | Most common approach in real systems         |