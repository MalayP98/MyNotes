

> **Schedule out** = OS removes a thread from CPU (it stops running)  
> **Schedule back** = OS puts that thread back on CPU (it resumes running)

---

# 🔹 What is “scheduling” here?

The OS scheduler decides:

Which thread gets CPU time

At any moment:

- One thread is **running**
- Others are **waiting**

---

# 🔥 Scenario (your example)

Thread A → holds lock  
Thread B → tries to acquire lock

---

## 🧩 Step-by-step

### 1. Thread B tries to get lock

Thread B → "I want the lock"

But:

Thread A already holds it ❌

---

### 2. Thread B gets blocked

Now OS says:

"No point running Thread B right now"

👉 **Thread B is SCHEDULED OUT**

---

# 🔻 What does “schedule out” mean?

schedule out =  
    remove thread from CPU  
    save its state (registers, stack, etc.)  
    put it in waiting/block queue

👉 Thread is NOT running anymore

---

### 3. Thread A finishes and releases lock

Thread A → releases lock

Now:

Thread B becomes eligible again

---

### 4. Thread B is resumed

OS scheduler:

"Okay, now run Thread B"

👉 **Thread B is SCHEDULED BACK**

---

# 🔻 What does “schedule back” mean?

schedule back =  
    OS picks thread again  
    restores its state  
    gives it CPU

👉 Thread resumes execution

---

# 🔥 Why is this expensive?

Because of **context switching**

Each switch involves:

1. Save current thread state  
2. Load another thread state  
3. Switch CPU context

👉 This is NOT free

---

# 🔹 Timeline view

Time →  
-------------------------------------  
Thread A:  RUNNING ---- release lock  
Thread B:  TRY → BLOCK → (scheduled out) → WAIT → (scheduled back) → RUN

---