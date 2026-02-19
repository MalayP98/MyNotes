
# 🔷 Example 2: Foreign Key Constraint

Tables:

`Orders(order_id, customer_id) Customers(customer_id)`

Rule:

- Every order must reference a valid customer.
    

If someone runs:

`INSERT INTO Orders VALUES (1, 999);`

If customer 999 doesn’t exist:

❌ Database rejects it.

Consistency is preserved.

---

# 🔷 Example 3: Unique Constraint

Table:

`Users(email UNIQUE)`

If two users try to insert same email:

`INSERT INTO Users VALUES ('abc@gmail.com'); INSERT INTO Users VALUES ('abc@gmail.com');`

Second insert fails.

Consistency preserved.

---

# 🔷 Example 4: Check Constraint

`salary > 0`

If someone tries:

`UPDATE employee SET salary = -5000;`

Database rejects it.

Still consistent.

---