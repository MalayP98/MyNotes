
- Use path param when the item represented the param is alway required. Example, `/events/{id}/ticket` shows that we will always require event ID to get the ticket
- Use query param when we have to filter and its optional. Example, `/tickets?eventId=1`, this shows that you can also remove the query param to get all the tickets or specify eventId to just get the ticket for a specific event.
- REST resources should represent _things_ in your system, not _actions_. Instead of thinking about what users can do (like "book" or "purchase"), think about what exists in your system (events, venues, tickets, bookings)
- **PATCH** updates *part* of a resource. Example,
  ```json
  // current entity
  {
  "name" : ...,
  "dob" : ...,
  "email" : ...
  }
  ```
  Now patch will just be used when we have to update `email` and not the entire resource.


## N+1 Problem

When it seems we just need *1* query to retrieve the data but that one query uses *N* more queries to form the whole response, making total number of queries used as *N+1*. Example, you want user data, the backend first fetches used data and then all the orders placed by this user(lets say N orders) making *N+1* queries.

**Where it occurs:**
- ORMs (like Hibernate)
- Lazy loading relationships
- GraphQL resolvers (nested queries)

**Resolutions:** 
- Join
- Eager Loading
- Batch fetching (not sure what this is)
- Use Data Loader in GraphQL(need to read more on this)

## API Patterns

### Pagination

**Problem with Offset Pagination** - 
If pagination is done on some sorted order, and a new entry is added then this pagination method might return duplicate entries.
Lets say we are retrieving rows sorted by latest `create_date`.

Current table -

| User  | Timestamp |
| ----- | --------- |
| User1 | 10        |
| User2 | 9         |
| User3 | 8         |
| User5 | 7         |
| User6 | 6         |
As per this table if we query `select * from user order desc timestamp limit 3 offset 0` we will get user `User1, User2 and User3`
Now a new user is create `User4`

New table - 

| User  | Timestamp |
| ----- | --------- |
| User4 | 11        |
| User1 | 10        |
| User2 | 9         |
| User3 | 8         |
| User5 | 7         |
| User6 | 6         |
Now if we query a new page, `select * from user order desc timestamp limit 3 offset 3`
we will get `User4, User1 and User2`, 2 users are duplicate in this result.

**How Cursor Pagination help here?**
After the first result we store a *cursor*.  Cursor will point to the timestamp of the last result *i. e* `User3`. For the next page query will be like so, `select * from user where timestamp < cursor_timestamp order desc timestamp limit 3`
`cursor_timestamp` will be 8. So this query says give me next 3 row after timestamp value 8, so we will get `User5 and User6` as a result.

## Security

**Authentication** - Is the use who he say he is?
**Authorization** - Does the user has permission/access to this resource/action?

**API Key** - For internal communication or for external application
**JWT** - For users on web app or mobile app
**RBAC** - Role Based Access Control, Roles have permissions associated to them and roles are assigned to users. Example, 

> Roles:
> - customer: can book tickets, view own bookings
> - venue_manager: can create events, view sales for their venues
> - admin: can access everything
> 
> User: john@example.com → Role: customer
> User: manager@venue.com → Role: venue_manager

## Rate limiting and Throttling

> Common strategies include:
> - **Per-user limits**: 1000 requests per hour per authenticated user
> - **Per-IP limits**: 100 requests per hour for unauthenticated requests
> - **Endpoint-specific limits**: 10 booking attempts per minute to prevent ticket scalping

