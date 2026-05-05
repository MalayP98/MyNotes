**MIME** stands for **Multipurpose Internet Mail Extensions**.

At its core, MIME tells a system **what kind of data is being sent** so it knows how to handle it.

---

## Simple idea

When data is sent over the internet (HTTP, email, etc.), it’s just bytes.  
MIME adds a label like:

- “this is an image”
- “this is JSON”
- “this is HTML”

So the receiver knows what to do with it.

---

## Common MIME types

- `text/html` → web pages
- `application/json` → APIs
- `image/png` → images
- `text/plain` → plain text
- `application/pdf` → PDFs


## Why it matters (practical use)

- Browsers decide how to display content
- APIs specify response format
- Security checks (don’t treat executable as image)
- File handling on servers

---