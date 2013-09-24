Meemplex
========

A system for asynchronous, distributed development.

Is is modelled around the concept of a Meem.  A Meem has inputs and outputs called Facets.
Meems may depend on each other.

Meemplex has been applied in the realm of Smart Home technology, bridging disperate automtion bus protocols with A/V, calendars and weather.

Meem
----
A Meem is the base components.  A Meem communicates with others Meems via Facets.

Facets
------
Facets are the points for input and output of a Meem.

Lifecycle
---------
Meems may be in one of various lifecycle states.

Dependencies
------------
A Dependency defines how one Meem depends on another via one or more facet pairs.
Dependencies may be Strong or Weak.  A Strong dependency means that the dependent Meem follows the Lifecycle states of the
Dependee Meem.

Hyperspace
----------
A hierarchical tree of Categories for organising Meems.
