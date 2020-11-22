##(L)EPB  - Logical Execution Plan Builder
![](https://media.licdn.com/dms/image/C4E12AQE1OLxgsJvdew/article-inline_image-shrink_1500_2232/0?e=1547683200&v=beta&t=IvcsBixXfakKoY8r3BnvQcr5srbLrYVckW0Veqw1Wjc)

The responsibility of this phase is to transform the logical query into a plan which is cost optimized according to set of predefined statistical parameters collected over the dataset.

###Strategies
The Logical Plan executor is responsible for building a valid (logical) execution plan that will later be translated into physical traversal execution plan.

The Plan Execution Builder comes with 3 strategies:

* DFS – A simple (DFS based) strategy that uses regular extenders to build the plan without any cost estimation.
  * Extenders will try depth first (descendants) strategy for plan building.

* Rule Based – A simple strategy that uses regular extenders to build the plan without any cost estimation.
  * Extenders will attempt to select ‘low cost’ steps according to some predefined simple rules.

* Cost Based – Cost based plan builder will use regular extenders with an additional cost estimation step.
  * Extenders are more exhaustive then in the former plans to cover as many plans as possible. 
  * Cost estimation will be used to prune costly plans.


The EPB process essentially is constantly growing a logical execution plan using the next components
* Extenders – add steps to existing plan in a variety of manners
* Pruners – remove plans either not valid or not efficient or duplicate
* Selector – selects best plans to continue to groom
* Statistics – provides the statistical cost access above the dataset
* Estimator – calculation the additional step cost according to the statistics

The extending phase is somewhat exhaustive in the way it adds steps to the plan.

This is useful in overcoming local minimums, the downside is the size of the plans search tree, we employ the pruner (for the purpose of trimming the tree size).
The pruner acts according to size & cost minimization – for example we only allow plan with 3 joins at most (this is configurable).

> Only the cost based strategy will employ cost based selectors & pruners.

### Process Of Execution Plan Building 
![](https://codeopinion.com/wp-content/uploads/2015/02/query-150x150.png)

####Initial Extenders

Initially we begin with empty plan and spawn initials (single step) plans, all plans start from ETyped node.
We continue the process of plan building with the next steps...

While we have available search options (unvisited query elements) do:

* Extend plan with a single step (apply a series of extenders)
   * Each (next) step is taken from the query unvisited elements.
* Validate the extended plan (apply a series of validators)
* Estimate cost for each plan (add the cost of the latest step)
* Select (Prune) best plan according selector policy (lowest cost / size limitation and so on… )

Once no more new steps available – we should have a list of valid best prices plans of which we will take the first (or any) for execution.

####On-Going Extender Types
An extender attempts to extend an existing plan with the next patterns:

> (entity)—[relation]—(entity) 
> (entity):{constraints}—[ relation] :{constraints}—(entity) :{constraints}
> (entity)—[relation]—(entity)—(goTo:visited_entity) 

From any step in the execution plan, we try to extend the plan either left or right (with elements from the query)
Left is considered ancestor, right is considered descendant.

####Example 1

Let’s assume we have the next query:
```javascript
Start [0]: ETyped [1]: Quant1 [2]:{3|4}: EProp [3]: Rel [4]: ETyped [5]
```

First we will initially create seeds plans (starting from Entity):
First Phase: Initial Plans 
------
```javascript
•	ETyped [1] – Plan 1
•	ETyped [5] – Plan 2
```
Next, we need to add additional steps (from the query) which are not part of the plan 

Second Phase:
------

 * Extending each plan with all possible extenders (all directions)

```javascript
StepAncestorAdjacentStrategy – extender that attempts extending left (ancestor)
•	ETyped [1] (no left extension possible) 	     – Plan 1
•	ETyped [5] --> Rel[4] --> ETyped [1]:EProp[3]    – Plan (2)-3

StepDescendantsAdjacentStrategy –extender that attempts extending right(Descendant)
•	ETyped [5] (no right extension possible) 	     – Plan 2
•	ETyped [1]: EProp [3] --> Rel [4] --> ETyped [5] – Plan (1)-4
```

The final plans are 3 & 4, there are similar but in reverse order.

####Example 2
Additional available Extender strategies...
Let’s assume we have the next query:
```javascript
Start [0]: ETyped [1]: Quant1 [2]:{3|4|6}: EProp [3]: Rel [4]: ETyped [5] : Rel [6]: EConcrete[7]
```

#####First Phase: 
```javascript
•	ETyped [1] – Plan 1
•	ETyped [5] – Plan 2
•	ETyped [7] – Plan 3
```

#####Second Phase:
Extending each plan with all possible extenders (all directions)

```javascript
StepAncestorAdjacentStrategy – extender that attempts extending left (ancestor)
•	ETyped [1] (no left extension possible) 	     – Plan 1
•	ETyped [5] --> Rel[4] --> ETyped [1]:EProp[3]    – Plan (2)-4
•	ETyped [7] --> Rel[6] --> ETyped [5]	     - Plan (3)-5


StepDescendantsAdjacentStrategy –extender that attempts extending right(Descendant)
•	ETyped [7] (no right extension possible) 	     – Plan 3
•	ETyped [5] --> Rel[6] --> ETyped [7] 	     – Plan (2)-5
•	ETyped [1]:EProp[3] --> Rel [4]-->ETyped [5] – Plan (1)-6

GotoExtensionStrategy - extender that jumps to already visited element 
•	ETyped [5]-->Rel[4]--> ETyped [1]:EProp[3]-> GoTo[5] – Plan (4)-7
•	ETyped [1]:EProp[3]--> Rel[4]--> ETyped[5]-> GoTo[1] – Plan (6)-8
•	ETyped [5] --> Rel[6] --> ETyped [7]-> GoTo[5]	- plan (5)-9
```

#####Third Phase:

```javascript
StepAncestorAdjacentStrategy – extender that attempts extending left (ancestor)
•	ETyped[7]-->Rel[6]-->ETyped[5]-->Rel[4]-->ETyped[1]:EProp[3]	 - Plan (5)-10
•	ETyped[5]-->Rel[6]-->ETyped[7]->GoTo[5]-->Rel[4]-->ETyped[1]:EProp[3] – plan (9)-11

StepDescendantsAdjacentStrategy –extender that attempts extending right(Descendant)
•	ETyped[1]:EProp[3]-->Rel[4]-->ETyped[5]-->Rel[6]->EConcrete[7] – Plan (5)-12
•	ETyped[5]->Rel[4]->ETyped[1]:EProp[3]->GoTo[5]-->Rel[6]->EConcrete[7] – Plan (8)-13
```

Plans 10-13 are the final plans, while additional non-valid plans had been removed during the build process. 
The missing step here is the cost estimation.
 
In the next section we will apply the same plan building process but with the additional cost estimators.

