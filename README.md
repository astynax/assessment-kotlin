### IPv4 Address Counter

Here is my solution for [this problem](https://github.com/Ecwid/new-job/blob/c3189567685c3f63604f5c34d8f8270656b832fc/IP-Addr-Counter.md).

#### Rationale

Since the task didn't specify that I can only *roughly* count unique addresses, I decided to count each one precisely instead of using something like Bloom filter or HyperLogLog.

I thought that if I'll

1. use bit sets to store the last two bytes of the address
2. keep these sets in a `Map` using the first pair of bytes as a key

then I'll get a pretty decent overall storage density (~1 bit per unique address) and possibly will save some space because of the `Map`'s sparseness if some address ranges won't appear.

#### Results

I have my code tested with the mentioned inf the task sample file (120 GB). The program counted exactly 1 billion unique addresses using ~1.2 GB of heap on average.
