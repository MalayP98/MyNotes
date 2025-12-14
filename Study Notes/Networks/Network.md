# [Computer Networks](https://vonage-qa.udemy.com/course/ccna-complete/learn/lecture/9538164#overview)

---------------------

* PDU : Protocol Data Units, block of data shared between different layers of OSI or TCP/IP models.
* Here is what its called at each layer,

<img src="resource/Example1.png" width="600" height="300"/>

* Upper OSI Layer:
    1. Includes -> Application, Presentation, Session.
    2. Application layer is the app itself, the UI that we see. HTTP/S is a app layer protocol.
    3. Presentation layer is responsible for translation and encryption. SSL is presentation layer protocol.
    4. Session layer manager, establishes session b/w 2 hosts.

* Lower layer OSI Layers:
    1. Transport layer : Ensure data delivery, breaks data into segments and handle port numbers.
       TCP/UDP are used here. Like a delivery service ensuring your packages to the right destination intact.
    2. Network layer : Encapsulate segment into packet and IP address to them, finds path(route) to send
       these packets. Router works on this layer. Sending from India -> USA, packets travel across multiple
       routers. Like choosing best road to a destination.
    3. Data link layer : Frames packets, add MAC address (src/dest). Ethernet, WiFi work on this layer.
       Laptop send frames to WiFi router, frames contain WiFi's MAC address. Like street names and
       house number used for delivery
    4. Physical layer : Converts frames into signals. Responsible for data rate. Optical fibers, Hubs.
       Like roads and wires.

  ## Transport Layer (4):

    1. Responsible to data transfer between hosts
    2. Responsible for flow control.
        * Flow control : If receiver or send are too fast or too slow this layer controls the flow of data
          so that none of the side get overwhelmed
    3. Responsible for session multiplexing.

       <img src="resource/Example2.png" width="700" height="300"/>

        * Session Multiplexing : using a single network connection (or session) to handle multiple communication
          streams simultaneously between two devices or applications.
        * Analogy : Highway - One connection | Lane - Multiple communication stream
        * Without multiplexing every application needs to open a new connection, example, new connection for each tab
          , new connection for chat app etc
        * Multiplexing offers :
            - Reduced connection overhead
            - Optimized resource usage
            - Improved performance
        * Sender sends message with destination and soruce port, example, `DST:80 SRC:1500` when the receiver sends
          the traffice back it flips destination and soruce.
        * TCP:
            - Once a connection is established data can be send bidirectionally.
            - Carries sequencing to ensure packets are received in correct order
            - Lost segment are resnet after an acknowledgement is received.
            - Performs flow control.
        * TCP 3-way handshake:

        <img src="resource/Example3.png" width="700" height="300"/>

        * TCP headers:

        <img src="resource/Example4.png" width="700" height="400"/>

        * UDP:
            - It is not connection oriented(does not performs handshake).
            - Doesnt carry seq number, doesnt care is if data is received or lost.
            - Not reliable.
            - Does not perform flow control.
            - If error detection and recovery is required upper layers should provide it.

        * UDP Header:

        <img src="resource/Example5.png" width="700" height="300"/>

        * Real time traffic uses UDP.
        * DNS uses TCP and UDP.

  ## Network Layer (3) :

    * Route packets to their destination.
    * No connection protocal, i.e. no handshake/ack is done.
    * Protocol on this layer : IP, ICMP, IPSec.
    * IP Header :

    <img src="resource/Example6.png" width="700" height="400"/>

    * Types of traffic :
        * Unicast : One-to-One communication. If multiple request comes then multiple copies of same will have to
          send to each receiver. Good for private communication.
        * Broadcast : One-to-All communication. One message is send to everyone on the subnet. Routers do not
          forward boradcast messages. Comes in use with DHCP server (google this).
        * Multicast : If we have to send message to multiple receiver using Unicast it will lot of bandwidth
          , directly propotional to number to receiver. But with multicast, sender just have to send one which
          gets distributed to all the interested receiver.

    * If a packet is going from sender to receiver on same subnet it can go directly(via switch). If sender and receiver
      are
      on different subnets then it has to go through router.
    * IP address is 32 bit long. Written in 4 section of octets. Example, 127.34.56.102
    * IP is set manually on servers, routers and other network devices. For devices like PC and laptop the IP is
      automatically assigned using DHCP.

  ## Subnet:

    * Subnet is always a series of `1`s followed by a series on `0`s.
    * This `1` portion defines Newtwork section and the `0` portion defines host portion.
    * Example :

    <img src="resource/Example7.jpg" width="1100" height="400"/>

    * In the above example network is defined as 192.168.10.0 and host address is 0.0.0.15 or .15
    * Data from 10.10.10.12 to 10.10.10.14 can go without router as they are on same subnet but data from 10.10.10.12 to
      10.10.20.12 cannot go without
      a router as they are on different subnets.
    * `Network Address`.0 is only kept to define Network Address and is not a valid IP address, Example 192.168.10.0 is
      not a valid IP address.
    * `Network Address`.255 is only **used for broadcast communication** and is not a valid IP address, Example
      192.168.10.255 is not a valid IP address.
    * Only valid IP address are in range `Network Address`.1 - `Network Address`.254
    * As subnet is consecutive ones followed by consecutive zero we can represent it by just the number of ones.
      Example, 198.168.10.23/18 means first 18 bits are ones and rest are zeros.

  ## Class A IPs:

    * Used where large number of host are needed.
    * Subnet of 8.
    * First bit is always zero. Example, *0*0011011.11011010.11001010.01011010 is a Class A IP.
    * If the first bit is zero the network range reduces to (0-127).0.0.0
    * 0 cannot be start of a IP and 127 is reserved for local, so now the address ranges reduces to (1-126)
    * 127.0.0.1 - 127.255.255.255 are all reserved for loopback testing (local).

  ## Class B IPs :

    * Used for mid to large size networks
    * First 2 bits are 10.
    * Subnet 16
    * Range 128.0.0.0 - 191.255.0.0

  ## Class C IPs :

    * Used for small size networks.
    * Starting bits are 110.
    * Subnet 32
    * 192.0.0.0 - 223.255.255.0

  ## Class D IPs :

    * Reseved for multicast address.
    * First 4 bits are 1110
    * These address are not allocated to host and there is no subnet mask
    * Range 224.0.0.0 - 239.255.255.255

  ## Class E IPs :

    * First 4 bits are 1111.
    * Kept for experimental and future use.
    * Range 240.0.0.0 - 255.255.255.255
    * 255.255.255.255 is a broadcast address.


* Private IP address :
    - Class A: 10.0.0.0 to 10.255.255.255
    - Class B: 172.16.0.0 to 172.31.255.255
    - Class C: 192.168.0.0 to 192.168.255.255
