## Elastic Search Schema Mapping
This document explains the low level mapping interface that allows generating the index mapping for the different 
nodes and edges that consist the ontology.

This low level schema mapping is consisted of the following mapping types that are available under elasticsearch index structure.

###Types of mapping:

    //static index
    STATIC,
    //common general index - unifies entities / relations under the same physical index
    UNIFIED,
    //time partitioned index
    TIME,
    //internal document which will be flattened to a dot separated key pathe
    NESTED
As show above we can match the existing index structure to our desired physical index structure or create one that fits
our needs for speed and space features.

