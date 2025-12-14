# [Concurrency and Performance Optimization](https://vonage-qa.udemy.com/course/java-multithreading-concurrency-performance-optimization)

---------------------

## Fundamentals

* Thread scheduling : OS schedules threads using epochs. The decision to execute hoe much of a thread (10% in this Epoch
  or
  40% in this epoch) is taken by `Dynamic Priority`.
* Epochs :

<img src="Resources/Images/Example1.png" width="700" height="300"/>

* `Dynamic Priority` = `Static Priority` + `Bonus`
* `Static Priority` : Set by developer before the application starts.
* `Bonus` : Given by operating system for each thread depending on which threads did not get enough time to complete in
  last epoch to prevent starvation. `Bonus` can be negative.

* [Uncaught exception in thread example](../../Code/src/com/cpo/ThreadException.java)

* Thread consumes resource in kernel even when it is not running.
* App will not stop if any thread is running even when main thread has stopped.
* We need to stop a thread when:
    * Thread is misbehaving.
    * When task is over and we have to cleanup by stopping all the thread.

* Deamon thread : When a thread needs to run in the background irrespective of if the main thread is stopped or not
  are called Deamon thread. They can be interrupted.
* [Examples on thread interruptions](../../Code/src/com/cpo/ThreadInterruption.java)

## Performance Optimization

* Latency : Time required to do a task. Measured in `time unit`
* Throughput : Number of tasks completed in a certain period of time. Measured in `tasks/time unit`
* Latency can be reduced by dividing the task into `N` subtask and using threads to perform each subtask.
* In normal computer number of threads should be equal to number of cores in the system.
* When to and not to use multi threading :

<img src="Resources/Images/Example2.jpg" width="700" height="400"/>

* As per the above graph, when single thread is used `Time taken by a task = Latency`,
  but in case of multi threading when the params are less (time taken by the task is less) the latency might be
  poorer than single thread *as we are adding extra overhead of creatingTr multiple threads* and when the number of
  params increase (time taken by the task increase) the latency reduces
  in comparison to single thread.
