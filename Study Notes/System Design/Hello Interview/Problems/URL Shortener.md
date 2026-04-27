Example -> [Bit.ly](https://www.hellointerview.com/learn/system-design/problem-breakdowns/bitly)

## Functional Requirements
1. Convert from Long URL -> Short URL
2. Convert from Short. URL -> Long URL
3. Optional - Expiration date

## Non-Functional Requirements
1. **Uniqueness** of the short codes
2. **Redirection** should be fast (able to fetch long URL from DB as fast as possible)
3. System should be available (We are choosing **Availability** over Consistency). We are choosing availability because this is high read system. [[How consistency matter in a URL Shortener?]]
4. System should support 1 B short urls and 100 DAU *(Daily active users)*

#
**Core Entities** - Original URL, Shortened URL, User
**API** - 
	{ `POST /urls`; Payload -> `long URL`; Return `shortened URL` };  
	{ `GET /<SHORTENED_URL>`; Return `long/original URL` }


> In case of `GET` request application will **redirects** the user to the long URL by return code `301 Permanent Redirect` or `302 Found`.
> 
> **301** - the browser caches the result and redirects user to the same long URL everytime; future request won't go through the system 
> **302** - the browser does not cache result as this status code indicate that the resource has be temporarily located on a different URL; every request goes though the system;
> 
> **Why is 302 a better option** - 
> 1. if a URL is updated/deleted user will get it immediately as the request goes through the system 
> 2. good for analytics as all the requests comes to the system ^redirect


## HLD

### Basics

**Submitting a URL** - 
1. validate the URL; if this URL has already been shortened return the existing shortened URL
2. once validated, shorten the original URL
3. insert into DB
4. return the shorten URL

**Fetching a URL** - 
1. looks up the short code in the DB
2. check if the short code has not expired *(if expiration is used)*
3. if not found in DB or expired, return `4xx` status
4. if found, [[#^redirect]] the user to that short URL 


### Shortening Strategies

**Random** - can use random number generator; as collisions are possible we need to verify it uniqueness everytime, hence, slow; not for scale

**Hash** - very low collision *(different strings can have same hash code)*;  this is **predictable** because anyone can guess the which URL has been shortened; example, if `hash(www.google.com) = jnwoin12`, then some can use hit and trial to check which URL's hash gives `jnwoin12` as a result; **==same string has same hash==** ^hash

**Hash + Salt** - `hash(www.google.com + <SOME_SALT>)`; this give us randomness so the output is no more predictable; we lose same string same code property *(if that was required)* ^saltedhash

Once [[#^hash]] or [[#^saltedhash]] is used we need to *Base62* encode it and then pick the first `N` characters as the short code.

> Problem with [[#^hash]] or [[#^saltedhash]] approach - collision might still happen; 
> Lets say we want to store `1B` short URLs. Each shorted URL is 6 chars long and every character have 62 options (a-z; A-z; 0-9) as we `Base62` encoding it. So different URL that can be generated will be `62^6 ~ 56B`  -> collision possibility `1B/56B` -> `1/56`, which is high in a large system. **==Not sure how is this high but that what I found==**


**Counter** - a counter give the app a incremented value for every URL; the value to then `base62` encoded and used;
**Pros** - 
- No collision as we get new counter value everytime
- Fast as we do not hash
- encoded strings are very short
**Cons** - 
- Predictability increases
- Single Counter -> SPF

To address predictability we can modify the count by - 
-  taking XOR with a `SECRET KEY`
- bit rotation
- hashing
- encrypt counter and encode

Use distributed ID generators -> (timestamp + machineID)
Assign each server/machine a set of IDs like *Server 1 -> (0-1M); Server 2 -> (1M-2M)*