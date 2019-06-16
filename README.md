# Open-Lowcode
### Open-source Low-code platform to build applications quick and fast.

published by [Open Lowcode SAS](https://openlowcode.com). You may also want to look at the [Community Page](https://openlowcode.org) and the [javadoc](https://openlowcode.org/javadoc/).

The project includes the main low-code platform (a full application server), and also some java tools you may find useful in other contexts. You are of course very welcome to use just the java tools if it makes sense for you.

You are welcome to get involved and contribute to the project by:
* suggesting new features or bug corrections. right here on github or on the [community forum](https://openlowcode.org/open-lowcode-forum/). Of course, the fixes will be best effort
* writing improvements to the framework to be included
* As a company using the framework, you may request additions to the framework for a fee to [Open Lowcode SAS](https://openlowcode.com). Depending on the complexity, commitments for deliveries can be as short as a few days, and it may cost as a little as a few hours. Additions to the framework will be integrated  in the open-source project, a great way for you to support the community.

This is the first open-source project of the author, so there may be technical mistakes done, do not hesitate to suggest improvements on the way the project is organized too.

# Core Values

Debate on the framework is highly welcome. The author of the framework fully acknowledges you may have stronger skills in a number of areas, and bring a lot of value and insights. 

Nonetheless, the framework is built on a number of strong hypothesis and choices, contributors are expected to accept, or, at least, tolerate them. Values are discussed in greater length in the [Open Lowcode blog](https://openlowcode.com/blog/). Please take this as a warning that this is, say, a bossa-nova concert, and if you are expecting heavy-metal (or the other way round), you will likely be disappointed.

Some core values include:
* keep complexity reasonnable. To make a parallel, if the tool was a rich-text format, the objective would be something like markdown (easy and good enough), not something like html (powerful but verbose). This is driven by the need to be usable in enterprise context with varying development skill levels. 
* prioritize performance and ease of debugging. This is one of the main drivers behind automatically generated code, compared to alternative approaches such as complex frameworks using reflection.
* limit the number of dependencies, within reasons. Open Lowcode relies on major technological frameworks and standards such as SQL, Java SDK (not Enterprise), JavaFX, and some outstanding Apache libraries around PDF and reading office documents. However, the core of the framework is all home-made. This allows end-to-end optimization, and better control of obsolescence. There may be discussions on this point though on the margin, as probably, the current version takes things slightly to the extreme.
* have reasonnable formal quality of code (but not more). The framework was developped under time constraints, and is likely to remain so for the foreseable future. It is fully acknowledge the code produced may not be used as example of good coding practices.
