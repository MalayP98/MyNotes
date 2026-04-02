
# Network

## General Points

- When to use UDP over TCP:
  1. Low latency is very important
  2. Some packet loss is acceptable
  3. Handling high volume telemetry log where occasional loss is fine
  4. You don't need support of web browser ==(cause browser don't have widespread support for UDP)==
- Use SSE when you want the client to have an update as soon as the event occurs. Example, Bidding applications.
- When we send data via SSE some old proxies might start buffering the data until all the data is received rather than sending the data to the client in chunks. Read [[Problem with SSE]]
- NAT device is basically router.

## TCP vs UDP

| Feature            | UDP                     | TCP                    |
| ------------------ | ----------------------- | ---------------------- |
| Connection         | Connectionless          | Connection-oriented    |
| Reliability        | Best-effort delivery    | Guaranteed delivery    |
| Ordering           | No ordering guarantees  | Maintains order        |
| Flow Control       | No                      | Yes                    |
| Congestion Control | No                      | Yes                    |
| Header Size        | 8 bytes                 | 20-60 bytes            |
| Speed              | Faster                  | Slower due to overhead |
| Use Cases          | Streaming, gaming, VoIP | Everything Else        |

==QUIC - Consider it a better version of TCP==

## Common Status

- Success (2xx)
    - 200 OK: The request was successful
    - 201 Created: The request was successful and a new resource was created
- Moved (3xx)
    - 302 Found: The requested resource has been moved temporarily
    - 301 Moved Permanently: The requested resource has been moved permanently
- Client Error (4xx)
    - 404 Not Found: The requested resource was not found
    - 401 Unauthorized: The request requires authentication
    - 403 Forbidden: The server understood the request but refuses to authorize it
    - 429 Too Many Requests: The client has sent too many requests in a given amount of time
- Server Error (5xx)
    - 500 Server Error: The server encountered an error
    - 502 Bad Gateway: The server received an invalid response from the upstream server


## What is TLS?

==Complete this note!!!!!!!==

## GraphQL

- Under-fetch: Fetching lesser data than required
- Over-fetch: Fetching more data than required.

If we under-fetch then the API calls will increase get the same amount of data. This will lead to latency, backend load.
If we over-fetch then the API will bring lots of data and also increase latency.

This can be solved using *GraphQL*. GraphQL basically is just a query we send to the backed to get the exact data we want (like querying a DB). Server interprets these queries and responds accordingly.

## gRPC

RPC -> calling a method on different server like its on local server.
It is faster than REST and uses ==Protocol Buffer (ProtoBuff)== instead of JSON.

## Layers v/s Protocols

|Layer (Top → Bottom)|Responsibility|Common Protocols / Examples|
|---|---|---|
|Application|User-facing communication, APIs|HTTP/HTTPS, DNS, FTP, SMTP, IMAP, gRPC, WebSocket|
|Transport|End-to-end delivery, reliability, ports|TCP, UDP, QUIC|
|Internet|Addressing & routing across networks|IP (IPv4/IPv6), ICMP, IPsec|
|Network Access (Link)|Local network communication (MAC layer)|Ethernet (802.3), Wi-Fi (802.11), ARP, PPP|
|Physical|Transmission of raw bits (signals)|Cables, Fiber optics, Radio signals|


"## TCP vs UDP
| ProductID | UserName |
| --- | --- |
| 1 | testuser |
| 2 | testuser |
| 3 | testuser |