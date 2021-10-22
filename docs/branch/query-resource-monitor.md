#Query Resource Monitor
The purpose of this feature is to evaluate and maintain a resource utilization monitor for each 
query->cursor execution process.

A query by itself has no resource allocated since it only represents a logical query, once a cursor was created 
it enters a state machine with the following stages:

 - Not Started - awaiting activation
    
 - Running     - Running and fetching data from storage
   
 - Suspended   - Suspended and not executing - but scroll resources are still available in store
 - Canceled    - Suspended and not executing - No scroll resources available in store

A cursor can also be deleted - it will no longer be present or contain any resources.


###Execution 
During the cursor execution phase (graph traversal) we will introduce the following capabilities:
 
 - Cancellation 
 - Resource monitoring

Since each cursor is executed inside the "page" scope - we will maintain these capabilities to be in this resolution.
Meaning we will be able to cancel / pause / resume the execution in the scope of a page size

If the requested pageSize was very large it will be internally partitioned into intermediate size so that we could enforce the
capabilities we presented - approximate max size will be configurable and default to 500



###Resources
A Cursor holds the next resources:

  - open cursors (store depended)
  - paged data (memory allocated) 
  - materialized documents (projection index)