# Linux Directory Structure and Filesystems

## 1. Linux Filesystem Hierarchy

Linux follows a hierarchical directory structure starting from `/` (root). All files, directories and mounted filesystems exist under this hierarchy.

|Data Type|Description|Examples|
|---|---|---|
|Static|Data that rarely changes unless explicitly modified|Commands, libraries, configuration files|
|Dynamic|Data that changes during system operation|Logs, runtime data, temporary files|

## 2. Filesystem Categories

|Type|Description|Examples|
|---|---|---|
|Disk-based|Stores data persistently on storage devices|ext4, XFS, Btrfs|
|Network-based|Provides access to files stored on remote systems|NFS, SMB|
|Memory-based / Virtual|Exposes kernel information or stores temporary runtime data|procfs, sysfs, tmpfs, devtmpfs|

**Note:** Virtual filesystems do not necessarily store ordinary files in RAM. For example, `/proc` and `/sys` expose kernel-maintained information dynamically.

## 3. Important Linux Directories

|Directory|Purpose|
|---|---|
|`/`|Root of the entire Linux directory hierarchy|
|`/boot`|Linux kernel, initramfs and bootloader files|
|`/etc`|System-wide configuration files|
|`/root`|Home directory of the root user|
|`/home`|Home directories of regular users|
|`/mnt`|Temporary mount point for filesystems|
|`/opt`|Optional or third-party application software|
|`/usr`|User-space programs, libraries and documentation|
|`/var`|Frequently changing data such as logs, caches and spool files|
|`/tmp`|Temporary files; cleanup depends on system configuration|
|`/dev`|Device nodes for physical and virtual devices|
|`/proc`|Information about processes and the kernel|
|`/run`|Runtime data for processes and services|
|`/sys`|Information about hardware, drivers and kernel subsystems|

### Important `/usr` Subdirectories

|Directory|Purpose|
|---|---|
|`/usr/bin`|User commands and executable programs|
|`/usr/sbin`|System administration commands|
|`/usr/lib`, `/usr/lib64`|Shared libraries and supporting program files|
|`/usr/include`|C/C++ header files|
|`/usr/local`|Locally installed software|
|`/usr/share`|Architecture-independent data, documentation and manual pages|
|`/usr/src`|Source code, including kernel source when installed|

### Important `/var` Subdirectories

|Directory|Purpose|
|---|---|
|`/var/log`|System and application logs|
|`/var/spool`|Queued jobs such as print and mail jobs|
|`/var/tmp`|Temporary files generally preserved across reboots|
|`/var/cache`|Cached application data|
|`/var/lib`|Persistent application state|
|`/var/opt`|Variable data for applications installed under `/opt`|

**Note:** `/tmp` may be cleared during reboot or periodic cleanup, whereas `/var/tmp` is intended to preserve temporary files across reboots.

## 4. Virtual Filesystems

|Directory|Filesystem|Purpose|
|---|---|---|
|`/dev`|devtmpfs|Provides device nodes for interacting with devices|
|`/proc`|procfs|Exposes process information and kernel state|
|`/sys`|sysfs|Exposes hardware devices, drivers and kernel attributes|
|`/run`|tmpfs (typically)|Stores temporary runtime information|

### `/dev` — Device Files

- Contains **device nodes**, not actual device drivers.
    
- Device drivers reside in the Linux kernel (built-in or loadable modules).
    
- Device nodes are managed dynamically through the kernel and `udev`.
    

|Device Type|Description|Examples|
|---|---|---|
|Character (`c`)|Transfers data as a byte stream|Terminals, serial ports, `/dev/null`|
|Block (`b`)|Supports block-oriented, random-access I/O|HDDs, SSDs, USB storage|

Device nodes have two identifying numbers:

- **Major number:** Identifies the kernel device-driver mapping.
    
- **Minor number:** Identifies a particular device or instance handled by that mapping.
    

### `/proc` — Process Filesystem

Contains dynamically generated information about running processes and kernel state.

|Path|Information|
|---|---|
|`/proc/cpuinfo`|CPU information|
|`/proc/meminfo`|Memory information|
|`/proc/PID/`|Information about a specific process|
|`/proc/mounts`|Mounted filesystems|
|`/proc/uptime`|System uptime|

### `/sys` — System Filesystem

Exposes the Linux kernel's device model, including hardware, drivers and kernel subsystems.

|Path|Information|
|---|---|
|`/sys/class`|Devices organized by class|
|`/sys/block`|Block devices|
|`/sys/bus`|Device buses|
|`/sys/devices`|Device hierarchy|
|`/sys/module`|Kernel module information|

### `/run` — Runtime Filesystem

- Stores runtime information such as process IDs, sockets and service state.
    
- Usually backed by RAM using `tmpfs`.
    
- Contents are removed during shutdown.
    

## 5. Useful Linux Commands

|Command|Purpose|
|---|---|
|`ls -l /`|List root directory contents|
|`df -Th`|Display mounted filesystems, types and disk usage|
|`lsblk -f`|Display block devices and filesystem information|
|`mount`|List mounted filesystems|
|`ls -l /dev`|Inspect device nodes and major/minor numbers|
|`cat /proc/cpuinfo`|Display CPU information|
|`cat /proc/meminfo`|Display memory information|

## Key Takeaways

- `/` is the root of the entire filesystem hierarchy.
    
- `/etc` stores configuration; `/usr` stores most software; `/var` stores variable data.
    
- `/dev` provides device interfaces, while actual drivers reside in the kernel.
    
- `/proc` exposes process and kernel information.
    
- `/sys` exposes device and driver information.
    
- `/run` stores temporary runtime state.
    
- Not every top-level directory is a separate filesystem; multiple directories can belong to the same filesystem.