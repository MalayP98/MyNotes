
## Functional Requirement
1. Able to upload files like image, document etc.
2. Able to download files.
3. Users should be able to share file with other users

## Non-Functional Requirement
1. Fast uploads and download
2. Availability > Consistency
3. Should support file as large as 50 GB 
4. Should be able to recover file if corrupted.

## Setup

### Entities
- User
- Item (metadata for file that is to be uploaded)
- File to be uploaded
### APIs
1. `POST /item` -> binary file
2. `GET /item/{itemId}`
3. `GET /allItems/{userId}` -> all the items for a users

## HLD

