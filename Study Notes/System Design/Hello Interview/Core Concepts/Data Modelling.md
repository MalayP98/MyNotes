**SQL** - is default

**Document DB** - store data in JSON format in documents, have flexible schemas, use when interview mention *frequent changing structure of data, or when data becomes so complicated that we require too many joins*.

**Key-Value Stores** - usually used with a *SQL* DB. Used as cache. Goof for high-write envs. *Need to duplicate data across multiple keys for different data fetching scenarios*

**Wide-Column Database** - every row can has different columns, Example,

| Row Key | personal_info:name | personal_info:age | contact_info:email | contact_info:phone |
| ------- | ------------------ | ----------------- | ------------------ | ------------------ |
| User1   | Malay              | 25                | malay@gmail.com    | 9999999999         |
| User2   | Rahul              |                   | rahul@gmail.com    |                    |
| User3   | Ankit              | 30                |                    | 8888888888         |
Used in *telemetry, event logging*

**Graph DB**: No need for this in interviews.


