
- While making changes to config file do make copies.
- Kernel is part of OS, OS comprises of Kernel, Shell, Device Drivers, File System etc.
 


```
                Operating System
+---------------------------------------------+
| GUI                                         |
| Shell                                       |
| File System                                 |
| Applications                                |
| System Libraries                            |
| Device Drivers                              |
|                                             |
|      +---------------------------+          |
|      |         Kernel            |          |
|      +---------------------------+          |
+---------------------------------------------+
                 │
                 ▼
      CPU • RAM • Disk • Keyboard • Network
```


## Secure Shell
A shell is a command-line interpreter that acts as an interface between the user and the Linux kernel. It accepts user commands, interprets them, and requests the kernel to perform the required operations. Secure Shell deamon config location `/etc/ssh/sshd_config`

### Configure Idle Timeout Interval:

Automatically disconnects an SSH session after a period of inactivity. This prevents unauthorized access if a user leaves their terminal unattended.

### Disable Root Login:

Prevents the `root` user from logging in directly via SSH. Administrators must log in using a normal user account and then use `sudo` to gain administrative privileges. This reduces the risk of brute-force attacks and improves auditing.

### Disable Empty Passwords:

Prevents users with blank passwords from logging in via SSH, ensuring that every account has a valid password.


### Limit User SSH Access:

Restricts SSH access to only specific users or groups. This reduces the attack surface by preventing unnecessary accounts from logging in remotely.

**Example:**

```
AllowUsers malay admin
```

Only the users `malay` and `admin` can log in via SSH.

### Use a Different SSH Port:

Changes the default SSH port from **22** to another port. This helps reduce automated scans and brute-force attacks targeting the default SSH port, although it should not be relied upon as the primary security measure.

```
                    Computer
                       │
                       ▼
                 UEFI / BIOS
                 "Start the boot"
                       │
                       ▼
                     GRUB
                 "Load the kernel"
                       │
                       ▼
                Linux Kernel
              "Manage hardware"
                       │
                       ▼
                   systemd
              "Start system services"
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
        sshd         cron         network
       daemon       daemon        services
                       │
                       ▼
                   User Space
          ┌────────────┼────────────┐
          ▼            ▼            ▼
         Bash         ls           cat
        (Shell)    (GNU tool)   (GNU tool)
```