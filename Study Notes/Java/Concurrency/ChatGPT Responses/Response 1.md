## How **fairness** in a `ReentrantLock` impacts **throughput**

---

### 1️⃣ Short, interview-ready answer

> **Fair `ReentrantLock` reduces throughput compared to unfair locks** because it enforces strict first-come-first-served ordering, which increases context switching and reduces opportunistic lock acquisition.

---

## 2️⃣ What “fairness” means in `ReentrantLock`

`ReentrantLock fairLock   = new ReentrantLock(true);   // fair ReentrantLock unfairLock = new ReentrantLock(false);  // default`

- **Fair lock** → longest-waiting thread gets the lock next
    
- **Unfair lock** → any thread can acquire the lock immediately
    

---

## 3️⃣ Visual intuition

![https://miro.medium.com/1%2A76-3-w78IkOUK7ceb0F-Mg.png](https://miro.medium.com/1%2A76-3-w78IkOUK7ceb0F-Mg.png)

![https://flylib.com/books/2/558/1/html/2/images/13fig02.jpg](https://flylib.com/books/2/558/1/html/2/images/13fig02.jpg)

![https://ducmanhphan.github.io/img/Java/Multithreading/reentrant-lock/dead-lock-1.png](https://ducmanhphan.github.io/img/Java/Multithreading/reentrant-lock/dead-lock-1.png)

Think of:

- **Fair lock** → orderly queue
    
- **Unfair lock** → fast person cuts in line
    

---

## 4️⃣ Why fair locks reduce throughput

### 🚫 1. Prevents lock barging

- Even if lock is free
    
- A new thread **must wait** for queued threads
    

➡️ Missed opportunity for fast execution

---

### 🚫 2. More context switching

- Threads are parked & unparked strictly
    
- OS scheduling overhead increases
    

➡️ Less useful CPU work

---

### 🚫 3. Reduced cache locality

- Different threads acquire the lock each time
    
- CPU cache warms are lost
    

➡️ Slower execution

---

### 🚫 4. Strict ordering increases wait time

- Threads with short critical sections wait behind long ones
    

➡️ Lower lock utilization

---

## 5️⃣ Why unfair locks have higher throughput

### ✅ Opportunistic acquisition

- Thread already running on CPU grabs lock immediately
    

### ✅ Fewer context switches

- No forced queueing
    

### ✅ Better cache locality

- Same thread may re-acquire lock quickly
    

➡️ **Higher throughput**

---

## 6️⃣ Concrete example

### Scenario

- 10 threads
    
- Critical section = 1 ms
    
- High contention
    

|Lock type|Throughput|
|---|---|
|Unfair|~900 ops/sec|
|Fair|~500 ops/sec|

Latency fairness improves, throughput drops.

---

## 7️⃣ When fairness is useful (despite lower throughput)

Use **fair locks** when:

- Starvation must be avoided
    
- Predictable latency is required
    
- Real-time / scheduling-sensitive systems
    

Use **unfair locks** when:

- Maximum throughput is needed
    
- Short critical sections
    
- High contention systems
    

---

## 8️⃣ Interview-ready comparison table

|Aspect|Fair lock|Unfair lock|
|---|---|---|
|Starvation|❌ Prevented|⚠️ Possible|
|Throughput|❌ Lower|✅ Higher|
|Latency predictability|✅ High|❌ Low|
|Context switching|High|Low|

---

## 9️⃣ One perfect interview sentence

> Fairness in a `ReentrantLock` enforces first-come-first-served access, which improves predictability and prevents starvation but reduces throughput due to increased context switching and loss of opportunistic lock acquisition.

---

## 🔟 Golden rule to remember

> **Fairness improves correctness and predictability, not performance.**

---

## 1️⃣1️⃣ Bonus interview insight (advanced)

Even **fair locks are not perfectly fair**:

- JVM allows limited barging
    
- Fairness applies only to queued threads
    

Mentioning this earns bonus points.