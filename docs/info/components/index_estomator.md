# Index Estimator
The purpose of this document is to define a new feature in the lucene index structure which will allow the index to use approximation based queries.
The index creation will be enhanced with this capability to allow specific fields to be approximated using a pre-defined data sketches with a selected precision.

The approximation of the field will be constantly updated during the life of the index and will be able to answer questions using
a dedicated API.

Such API can be used both internally for query execution planning and both for external usage.

## Approximation Theory

## Approximation Queries in other products

## Component Architecture

## API & Example Query 

