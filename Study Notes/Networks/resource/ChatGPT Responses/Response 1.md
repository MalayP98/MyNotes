# Real Packet Flow (Step-by-Step)

Example:

Laptop --- Switch1 --- Switch2

### Step 1

Switch1 sends LLDP frame:

Dest MAC = 01:80:C2:00:00:0E  
Src MAC = Switch1 MAC  
EtherType = 0x88CC  
Payload = LLDP TLVs

---

### Step 2

Switch2 receives frame.

Because destination MAC is **LLDP multicast**, it:

- Processes packet
    
- Updates **LLDP neighbor table**
    

---

### Step 3

Switch2 stores:

Neighbor: Switch1  
Port: Gi0/1  
Capabilities: Switch

---

### Step 4

Administrator runs:

show lldp neighbors

Output:

Device ID     Local Port    Neighbor Port  
Switch1       Gi0/1         Gi0/24