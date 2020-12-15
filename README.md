# file-search-engine
###Problem

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
4) Persistency
    1) Just as how Spotlight maintains an indexing, instead of reindexing everytime it starts up, our file system should
    have a way to maintain its indexing between sessions.
5) Dynamism
    1) After the initial indexing, the FSE must be able to update its index with any changes that may occur. Files that 
    are newly created, updated, or deleted must be reflected in the index even after the initial indexing.

###Design

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
threads that parse. Each thread, takes out individual files from the bounded buffer and parses them, avoiding any 
issues of overlap.

###Implementation

//talk about index using ConcurrentHashMap and PriorityQueue and pros and cons

###Evaluation

//need to implement evaluation measures

###Conclusion