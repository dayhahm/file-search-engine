# file-search-engine

### Structure
The src folder contains all of our class files. The driver class file is the ThreadRunner.java file.

### Instructions/ How to run and install
Our project uses Java 11 and has no outside dependencies (I think tempest uses Java 8 and so it doesn't work there). 
`git clone` the repository, compile with `javac ThreadRunner.java`, and then run with `java ThreadRunner`. If you would
like to run the engine with verbose mode on (prints out additional information, like number of files parsed, number of 
index keys, etc), run with `-v`.

The program will prompt the user to enter a root directory to traverse. Please enter the absolute path to the directory.

### Known bugs and limitations
The largest limitation of this project is the memory usage. In our evaluation sections, we talk more about the memory
limits of our system.

### Problem

For most users having to manually search through their file system is not feasible due to the volume of files. So, 
instead there should exist a tool where a user can enter keywords and the tool sifts through the files, returning the 
most relevant options. An example of such a tool is MacOS's Spotlight. Spotlight can perform several different tasks,
but its core job is to maintain an index of the metadata of all files in the file system. The user can type in keywords, 
commonly a word that exists in the file they're looking for, but users can also filter by other file metadata like 
creation date and so on. 

Our project aims to build a simple version of a file search engine. Our minimum goal is to build a tool that can
1) Traverse a file system and gather all (readable) files
2) Parse those files
3) Rank files based on a user-submitted keyword
    1) Preliminarily, this ranking method will be very simple. We will rank files by the number of times the keyword
    appears.
    
The most important aspects to consider during this project are
1) Memory efficiency
    1) We need to maintain an index of all files and the number of times a certain word appears in that file. This can 
    be extremely memory-consumptive, so we need to devise a way to ensure efficiency.
2) Indexing latency
    1) Similar to memory efficiency, because we must parse every single file in the FS, this may take a long time. For
    small file systems, the latency may not be noticeable, but using only one thread to parse is not a scalable 
    solution. 
3) Search/Ranking latency
    1) When a user enters a keyword, there should not be a long delay between then and when the engine returns their 
    results. It should feel nearly instantaneous.
4) Dynamism
    1) After the initial indexing, the FSE must be able to update its index with any changes that may occur. Files that 
        are newly created, updated, or deleted must be reflected in the index even after the initial indexing.
5) Persistency
    1) Just as how Spotlight maintains an indexing, instead of reindexing every time it starts up, our file system 
    should have a way to maintain its indexing between sessions.


Note that some of these priorities conflict with each other. In particular, a significant problem we faced was the 
conflict between memory efficiency vs. time efficiency, which we will talk about in more detail later.

### Design

Our file search engine has 4 basic parts. 
1) Traverser
    1) The traverser traverses the file system starting from a root directory. It gathers the paths of all the files 
    that need to be parsed and puts them into a bounded buffer.
2) Parser and the Index
    1) The parser takes file paths out of the bounded buffer and parses the file space. Currently, the parsing delimeter 
    is any whitespace.
    2) For each unique word in the file, the parser counts the number of times it is uses and stores that count and the
    file together in our index.
    3) Our index indexes by word. The corresponding value for each word is a collection of file and respective word 
    count pairs.
3) Ranker
    1) The ranker takes the user inputted keyword and then creates a ranking of the files in the index for that word.
4) Watcher
    1) The watcher watches for any file creation, update, deletion notices from the file system. On creation notices, 
    the watcher puts the newly created file into the bounded buffer. On deletion notices, the watcher deletes any 
    instances of the given file from the index. On update notices, the watcher performs the deletion process and then
    puts the file into the bounded buffer so that the new contents are added.
    
We use concurrency to solve some of the time performance issues mentioned above. We use a bounded buffer for the 
Traverser, Watcher, and Parser to communicate. The Traverser and the Watcher are the producers and the Parser is the 
consumer. For the Traverser and the Watcher, we have one thread of each; having multiple threads can cause repeat files 
or notices being added to the bounded buffer. However, as parsing is the largest source of latency, we have multiple 
threads that parse. Each thread, takes a file from the bounded buffer and parses it, avoiding any 
issues of overlap.

Regarding the index, we had a few option to consider. 
1. A single index/map
    - In terms of memory, this would be the better option. The keys to the map would be the set of words that appeared 
    in all the parsed files. The value for each key would be a collection of file path names of all the files that 
    contained that word. 
    - However, certain operations like changing the index for files that have been modified or deleted since indexing, 
    would be very expensive as there is no place where we also keep records of word counts for each file. Probably the 
    best solution for this issue would be to batch multiple of these operations together to decrease the number of times
    we would have to scan through the entire index and every place the file appears. Batching the operations would mean
    that these operations would by asynchronous, so some search results may be incorrect, but that tradeoff would be
    well worth it.
2. Multiple indices/maps
    - In terms of time, this would be the better option. We could maintain the same index from the single index 
    approach, but in addition keep a map of files to their word counts. This would mean that instead of scanning the 
    entire key set of the index (i.e. every single word that has appeared in any parsed file), we could just scan the
    index for the words that appear in the file. This is still a costly operation, but an individual file's word set 
    will almost always be significantly less than the index's word set. 
    - However, keeping two different versions of the parsed files is not memory efficient at all. Even keeping a single
    index can be very memory intensive for large file systems, so keeping two different indices in the engine's heap 
    memory would be problematic. Indeed, as we learned during our implementation stage, keeping even one index caused 
    memory issues.
    
Ultimately, we decided to keep only one index, sacrificing the time efficiency of some operations in favor of 
maintaining memory to a certain degree. 

However, we also had an alternative idea that had the memory benefits of the first approach and the time benefits of the
second approach. This idea involved persistency, in that the way we would persist the index would be to create a JSON 
file or some other structured file, that stored the file name as a key and then the word count for each word as a nested
value. Essentially the second index in the second approach would become the persistent index and that would be stored as 
a file instead of in the program's heap memory. Thus, with the persistent index on an update or delete we could retrieve
the file's word set from the file and then scan the first index with that word set. Of course this approach has its own
cons, mainly in IO costs and bottle-necking when there are lots of updates or deletes, but again we could use batching
to decrease those costs. Reading the file itself could be an issue as well, considering that this file could become 
quite large, so care would be needed when retrieving data from the file. In the end, we were not able to implement this
approach due to time constraints, but it would be interesting to see if this approach performs better than our current
approach, or if the IO costs would outweigh the other benefits.

### Implementation

Our index is the most important piece of our design, as every part of our design is dictated by how to maintain the 
index. For our index, we used a ConcurrentHashMap (a normal HashMap is not thread safe). The keys of the index are 
of type String, and as mentioned before are the set of words that appear in the parsed files). The values for each key
is a PriorityQueue. We chose to use a PriorityQueue as it is a sorted data structure and thus while insertion is not
constant time like in an ArrayList, retrieving the top element is, so user's would get their results back almost 
immediately. We created a simple class called FileCount which stores a String, the file name, and an int called count,
the number of times a certain word appeared in that file. The PriorityQueue sorts FileCount objects by the count, 
descending.

As mentioned in the design portion, the Traverser thread and the Parser threads communicate through a BoundedBuffer that
we implemented. The capacity of the BoundedBuffer is currently set to 20 (an arbitrary number), but it can easily be 
changed if experimentation shows another more optimal number. The Traverser thread is the first offshoot thread to start
and begins adding to the buffer. For now, we have five Parser threads that start after the Traverser and begin 
consuming from the buffer and adding to the index. 

The Watcher thread is then started and starts to watch for any file creations, modifications, and deletions in the 
root directory space.

As soon as the Traverser thread has completed its traversal, the user may start searching for key terms. Note that this 
means that the engine has not necessarily finished parsing. So, if a user searches a term twice, it may be possible that
they get two different results as more parsing may have been done in the in between time. We decided on this flow 
because parsing most likely takes up more time than the traversal, so while the results may not be perfectly accurate
the user is able to at least get approximate results in return for shorter waiting times. Also, it is difficult to tell
when the initial parsing has ended as the Watcher thread may be adding files to be parsed based on file creations or 
updates. The Parser threads are never completely done, and thus can't be joined back to the main thread to signal that 
they have finished. 


The main flaw to this implementation is again the delete and update operations. In addition to the issues mentioned in
the design section, while the retrieval methods for the PriorityQueue are constant, we pay from them in the remove 
method, which takes linear time. In a large system, which many deletes and updates, these costs stack up.

### Evaluation

Parsing is the main time sink of the engine, so we ran it on three different directories and timed the initial parsing.

The first contains a set of tweets. It contains 603 files and is 119 mbs big. It took about 33111 ms to parse 602 files
(not including traversal), which is about 55 ms per file, and each file is about 200 kbs big.

The second contains a set of Marvel character wikipedia pages. It contains 1632 files and is 28 mbs big. It took about 
10366 ms to parse 1610 files, which is about 6 ms per file, and each file is about 15 kbs big.

The last contains books from Project Gutenberg. It contains 10 files and is about 9.7 mbs big. It took about 5544 ms to
parse 10 files, which is about 554 ms per file, and each file is about 1 mb big.

Based on this data, it looks like the parsing is roughly linear, 

When we tested the engine with 1gb of Java heap memory, it is able to index about over 3.1 million unique words and 
about 215,493 characters of unique file path names. When tested with 1024mb of space, it is able to index about 2.6 
million unique words and 178,886 characters of unique file path names. We suspect that the memory issues is due to the 
fact that index is just too large, thus in a next iteration, it would be good to explore non-Map implementations of our
index. A possible solution could be to simulate a virtual memory space.

We had also tried a Trie data structure instead of the map (checkout the Trie branch to see that implementation). We 
thought that it was possible that many of the keys would have the same prefixes, so the Trie structure could save space
as it doesn't create a new String object for every index, but instead creates a tree of character nodes instead, and 
words that share the same prefix, share the same path down, until their prefix ends. However, perhaps due to the fact
that our implementation uses a HashMap to store the children of the TrieNode and the associated overhead needed to 
maintain the HashMap, it was only about to process a tenth of the files of our current implementation. Furthermore, for
files that have relatively long words (meaning bigger avg key lengths), the Trie structure we need to create that many 
more HashMaps (e.g. if the average length of a key is about 20 characters, and there needs to be that many HashMaps 
to insert that key into the Trie). We also tried using fixed size arrays instead of HashMap's but that also had the same
results.

### Conclusion

Currently as of 12/18/20, our project reaches MVP in that it is able to traverse, index, and return ranked results.
Beyond our MVP requirements, we have implemented concurrency in our parsing threads and a partial file system watcher 
that can add files that have been newly created or modified since the initial indexing. The watcher does not yet deal 
with deleted files and for updated files, it does not delete the previous references to the file in the index. 
Persistency would be the last of our stretch goals that we would implement.

Our file search engine scratches just the surface of what modern day file search engine can do, but even with our 
limited scope, it exposes the challenges of designing such a system. A repeated theme in systems is that there is not 
perfect solution to a problem, only slightly less worse solutions than the others. 

### References
-BoundedBuffer reading