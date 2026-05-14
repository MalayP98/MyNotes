
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

### Upload file

**Via API**: User can upload file to our backend and out backend can upload file to S3 or any other blob storage. Once the object is stored successfully the status in metadata can be changed to `uploaded` or something. Problem, upload happens twice, first user -> backend second backend -> blob storage.

**Pre-signed URL**: Our backend will give user pre-signed URL using which they can upload the file, once uploaded S3 can send us event. We can confirm if the file was upload successfully by consuming this event, if upload we can update the status in file metadata.

### Download File

**Download from backend**: Double Download

**Pre-Signed URL**: Get the URL from from AWS *(or any other cloud provider)*, and use to download the file. Problem, if a user is far from the server region the downloads will be slow.

**CDN**: We can use CDN so that the user can download the file from their closest region. Problem, need to remove the file from CDN if it is update or deleted, cost is higher.


### Share

**Save user in file metadata**: For a user `U1`, if we need to know the all the file shared with them, we will have do a full scan on file DB to see which file metadata contain that user. ^sol1
```json
{
	"id" : ...,
	"name" : "testfile",
	"extension" : "txt"
	.
	.
	.
	"sharelist" : ["U1", "U2"......]
}
```

**Save file id in user**: In addition to the above solution, for a user `U1` we can add a `sharelist` key in the user json as well. ^sol2
```json
{
	"id" : ...,
	"name" : "Ux",
	"email" : "ux@example.com",
	.
	.
	.
	"sharelist" : ["F1"....]
}
```


Problem, we will have to keep both the list in sync. *Why do we need 2 list?*, First solution [[#^sol1]] list will be used for query like `Which user have access to file F1?` and second solution [[#^sol2]] list will be used to query list `Which file does user U1 has access to?`

**Create a separate table**:  This table keep track of files shared between users. `U1 -> F1` means that fileId `F1` was shared with user `U1`

| userId (Partition Key) | fileId (Sort Key) |
|-------------|-------------|
| user1       | fileId1     |
| user1       | fileId2     |
| user2       | fileId3     |

### Sync

*We need to have a pre-configured sync directory on our system so that these sync feature work. No need to tell this is an interview, just keep in mind.*

#### Local to Remote 
When a file is modified in any way the application used OS event *(like `inotify` in Linux or `FSEvent` in macOS)* to monitor these changes. If changes are detected, it uploads the new file with updated metadata. If there are conflicts, we can use different strategies to resolve them one of them being **last write win**. *(I feel there can be a lots of questions on this strategy, such as, what if 2 offline changes are made at the exact same time.)*

#### Remote to Local

##### Polling
Pinging the server periodically *(lets say p)* to check anything is there to be synced. Fetch all the files who's `lastUpdated` time was `currentTime - p`. 

Even better, keep a `lastSynced` timestamp, after every sync we can store this timestamp locally and in the next sync call we can pass this and ask for changes where `lastSynced` > `passed_timestamp`.

##### SSE or Websocket
Server maintains a open connection with the client, when ever new update happens it syncs the file. Its expensive but the sync are in real time.

Best approach is to mix the above to strategies,
As Websocket connection might drop temporarily *(because of any reason)*, so meanwhile application can rely on polling. This will maintain eventual consistency if websocket connection drops.

## Deep Dive

### Why can we upload big files

1. **Server Timeouts**: If we calculate using internet speed how much time a 50 GB file take to upload it will be somewhere around 1 hr with 100 Mbps internet. Server timeouts are way less than this so the download will fail.
2. **Browser/Server limitation**: Browsers and server don't allow large files to be uploaded in a single post request.
3. **Network Interruption**: As a bigger file will take more time the chances of network failure or power failure increase because of which file upload might fail.

#### Chunking
To upload big files we will divide these files in to chunks of size 5-10 MBs and upload these chunks one by one or parallelly. To track which chunks have been upload add `chunks` key to the JSON file metadata.
```json
{
	"id" : ...,
	"name" : "testfile",
	"extension" : "txt"
	.
	.
	.
	"sharelist" : ["U1", "U2"......],
	"chunks" : [
		{
			"chunkId" : "C1",
			"status" : "uploading"
		},
		{
			"chunkId" : "C2",
			"status" : "uploaded"
		},
		{
			"chunkId" : "C3",
			"status" : "failed"
		}
	]
}
```

##### How to keep this `chunks` field in sync with the actual chunks uploaded?

1. **PATCH request**: Client upload the chunk. S3 sends success response. Hit a PATCH request with updated metadata. Problem, client is responsible to keeping the `chunks` field in sync. Any user with a valid token can come and mark all the chunks as uploaded which can lead to a inconsistent state.
2. **ETags**: S3 don't send notification for each chunk uploaded, but it gives each chunk a `ETag` when it is upload. Once uploaded, S3 sends this `ETag` in response also. Client can send this `ETag` in the PATCH request can the server can then verify the tag value with S 3 ^etags

#### Fingerprinting and Resumable Uploads

When implementing resumable uploads, the very first thing we need to answer is:

1. Has this file already been uploaded before?
2. If upload was interrupted, which chunks were uploaded successfully and which still remain?

We cannot rely on filename for this because multiple users can upload files with the same name, or even the same user can upload different files with identical names. Instead, we generate a fingerprint from the file content itself.

A fingerprint is basically a hash generated using algorithms like `SHA-256`. Since the hash is derived from the file content, identical files will produce the same fingerprint regardless of filename. This helps in:
- deduplication
- resumable uploads
- integrity verification

One important thing to note is that fingerprint identifies the content, not the file record itself. Two users uploading the same movie will produce the same fingerprint, but they should still have different file records in our metadata DB. So ideally:
- `fileId` should be a UUID
- fingerprint should be a separate field

For resumable uploads, fingerprinting the whole file is not enough. We also fingerprint individual chunks. This helps us identify exactly which chunks were already uploaded before interruption happened.

#### Multipart Upload Flow

When a user uploads a large file, the client first breaks the file into chunks of around `5-10 MB`. It then calculates:
- fingerprint for the whole file
- fingerprint for every chunk

The whole-file fingerprint is used for:
- duplicate detection
- resumable upload checks

The client then asks backend if a file with the same fingerprint already exists for that user.

If backend finds a matching file whose status is still `uploading`, then upload can be resumed. Backend returns already uploaded chunk information so client only uploads remaining chunks.

If file does not exist, backend initiates a multipart upload.

#### Multipart Upload Initialization

Backend calls S3 `CreateMultipartUpload` API which returns an `uploadId`.

Backend then:
- generates pre-signed URLs for every chunk
- stores metadata in `FileMetadata` table
- marks file status as `uploading`

Finally backend returns:
- uploadId
- presigned URLs
- chunk metadata

to the client.

#### Uploading Chunks
Same as [[#^etags]]

#### Completing Upload

Once all chunks are marked as uploaded, backend calls S 3 `CompleteMultipartUpload` API along with part numbers and ETags S3 then assembles all chunks into a single object. Only after successful assembly backend updates file status to `uploaded`. This ensures metadata always reflects actual S3 state.

#### Upload Progress Tracking

Throughout the upload process, client is responsible for tracking upload progress and updating UI accordingly. This includes:
- upload percentage
- uploaded chunks
- estimated remaining time

#### Important S3 Multipart Nuance
S3 only sends event notifications after `CompleteMultipartUpload` is called, not for individual chunk uploads. To track upload progress for individual chunks, backend must use `ListParts API`, which returns uploaded part numbers and their `ETags`.

#### Chunked Uploads vs Chunked Downloads

Multipart uploads do not mean downloads also happen in chunks. Once upload is completed, S3 assembles all chunks into a single object and downloads work like normal file downloads using pre-signed URLs or CDN URLs.

For large files, HTTP/S3 support `Range Requests` which allow resumable downloads, parallel downloads and downloading specific byte ranges without needing knowledge of original upload chunk boundaries.