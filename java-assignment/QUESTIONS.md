# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. 
If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes — if I were maintaining this code base, I would very likely refactor the database access layer if multiple strategies are currently mixed.
I will do that because: Because inconsistent persistence strategy causes confusion, Data integrity risk, testing, maintenance, optimization and knowledge gap issues.
```

2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, 
but for the other endpoints - `Product` and `Store` - we just coded directly everything. 
What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
API First:
PROS:
Strong contract governance
Clear separation of concerns
Better for long term stability
Enables early collaboration
Consistency & documentation
CONS:
Slower initial development
Extra generation layer
Risk of spec drift

Code-First:
PROS:
Faster for small teams 
Easier refactoring early
Simpler for internal APIs
CONS:
Weaker contract governance
Harder cross-team collaboration
Less structured versioning
```
----
3. Given the need to balance thorough testing with time and resource constraints, 
how would you prioritize and implement tests for this project? 
Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
For this kind of backend system, I would structure testing like this:
1. Unit Tests
2. Service-Level Tests
3. Integration Tests
4. End-to-End / Scenario Tests


What I would NOT over invest in: 
Testing trivial getters/setters
Testing framework behavior
Excessive mocking of repositories
100% code coverage obsession
```