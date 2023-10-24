## EmoSTL

A library providing EmoSTL DSL to formally specify PX requirements. Imagine an agent who is doing some activity in the game environment. We collect data from the agent in the form of a trace file that records the sampled states of the agent. The file is in csv-format. We possibly have multiple trac file, coming from multiple runs of the agent (or runs of multiple agents).

The DSL allows PX requirements to be written as specifications and with the  trace files, the satisfaction of those specifications can be verified.

In the context of Player Experience (PX) testing, these queries can express some emotional experience specifications that can be specified by time and area. This will be explained by some examples later on.

DSL feature:

   * Based on Linear Temporal Logic (LTL), with extensions below.
   * Spatial/area-coverage
   * Discrete-time bounding
   * First-derivative values
   * Sequential-patterns
   * Aggregation


## Replication instructions
to replicate the results, you need to run "Test_ZenopuslevelSpecifications" test file usgin ecliple, located at src/test/java/ux.pxtesting.ltl.offline
location of trace file....
origiaal speification pdf.file

EmoSTL is written in Java. It requires Java-11 or higher. The project is Maven-based.

To build just do `mvn compile` from the project's root. This will automatically download all its dependencies and then compile the project.

Run the testclass `Test_ZenopuslevelSpecifications`. It verifies all set specifications over the given game trace files:

```java
mvn test -Dtest="ux.pxtesting.ltl.offline.Test_ZenopuslevelSpecifications" -Dmaven.test.failure.ignore=true

```
In the results, you need to see 21 tests are run and only two tests failed which shows our specification successfully detected an issue in the game design. The rest (19 tests) are passed successfully.
## General information about the Library
### Area and area coverage
An "area" represents some 3D-space. It can be a "surface", but generally it is a space.

Suppose an agent works in the area, and we want to now how much of the area is covered by the agent. Suppose the information we have is a sequence of samples of its locations while working in the area. The sequence is finite. Unfortunately, we cannot simply use the number of different locations in the sequence as a measure of coverage, because there are infinitely many locations contained in any non-empty area.

To address the above problem, we can imagine the area to be divided into disjoint _voxels_. A voxel is a small 3D-cube, by default of size 1-unit. The entire space can be thought to be divided by infinite number of adjacent voxels, aligned to the origin (0,0,0). The latter means that we have one voxel vx0 with (0,0,0)
as its bottom corner, and (1,1,1) as it tops corner. Other voxels must be adjacent to vx0, or inductively adjacent to another voxel.

So, given an area A, we can calculate V(A) as the smallest set of voxels that subsumes A.

A visit at location p covers a voxel vx if p is inside vx. More precisely, if (x0,y0,z0) is vx's bottom, and (x1,y1,z1) is its top, then p covers vx if:

    x0≤p.x<x1 ∧ y0≤p.y<y1 ∧ z0≤p.z<z

Now, given a finite set S of locations, representing sampled locations of our agent, its coverage over A can be defined as the set C = { vx | vx ∈ V(A) ∧ (∃ p: p∈S : p∈A ∧ p covers vx)}. In terms of percentage, the coverage is then:

   100% * |C| / |V(A)|

**NOTE:** The current implementation explicitly constructs V(A) when A is created. It is therefore not suitable for representing vast areas.

#### Specifying areas

   * A _rectangle_ is specified by its bottom and top corner. The bottom corner is
   the one with lowest (x,y,z) position, and the top corner is its opposite.
   It is assumed to have 1-unit height (y-dimension).

   ```Java
   RectangleArea A1 = rect(new Vec3(-0.5f,0,0), new Vec3(2,0,1)) ;
	RectangleArea A2 = rect(new Vec3(-1,0,0), new Vec3(2,0,3)) ;
	RectangleArea A3 = rect(new Vec3(-0.5f,0,0), new Vec3(3,0,4)) ;

   ```

   * Areas can be combined to construct a more complicatedly shaped area:

   ```Java
   var B1 = A1.union(A2)
   var B2 = B1.intersect(A3) // intersection of two areas ... in this example this gives empty
   var B3 = A3.minus(A1) // A3 - A1
   ```

   * Checking if a location p is within an area:

   ```Java
   A1.contains(p)
   ```

#### Calculating area-coverage

Suppose S is a list of locations (instances of `Vec3`), below we obtain C: the voxels from V(A3) that are covered by S. The coverage percentage is given by c:

```Java
var C = A3.covered(S)
float c = 100f * A3.coveredPortion(S)
```


## Offline processing

Imagine an agent that is doing some work. We collect data from the agent in the form of a trace file that records sampled states of the agent. The file is in csv-format.

   * The first row of the trace-file specify the column-names. Each name represent a property of the agent state. E.g. it could be:

   `posx`, `posy`, `posz`, `time`, `health`, `joy`

   * Subsequent rows are sampled states. For example if r(k) is the k-th row, with k>0, in this example it will be a tuple of 6 nummeric values v0,v1,v2,v3,v4,v5 of the corresponding property-name. For example, v0 would be the value of posx in this state, and v5 the value of joy.

   * `posx`, `posy`, `posz` represents the agent position in a 3D space. **At least posx should be present**. If your data call these `x`,`y`,`z` instead (or other names), you can set their naming in `XStateTrace.posxName`, `XStateTrace.posyName` etc.

   * `time` is a time-stamp. It should be a whole number (a long, to be precise).

Imagine that the agent has had multiple runs. So, we have collected a bunch of trace-files (with the above format) from those runs.

#### Parsing trace-file

We can read these trace-files to be exposed to LTL-queries. Internally, it will then converted into an instance of `XStateTrace`, which is essentially a sequence of `XState`.

```Java

XStateTrace trace1 = XStateTrace.readFromCSV(file1) ;
XStateTrace trace2 = XStateTrace.readFromCSV(file2) ;
XStateTrace trace3 = XStateTrace.readFromCSV(file3) ;
```

#### Enriching trace

Each of this `XStateTrace` contains simply the series of data as in the original trace-file. LTL has however a number of shortcomings, e.g. in LTL we cannot express the difference between the value of some property x in the current state, and in the previous state. Therefore we can't express a requirement that says that "eventually the value of x would decrease". LTL cannot express aggregation either. E.g. we cannot then express "in average, the value of x will never be below 0". Note that area-coverage is also an aggregation property.

To solve the above problem, we **enrich** our `XStateTrace` so it is augmented with derived information:

   * For every property x, we augmented the trace with the value of its first derivative (x'), which is the difference between x at state i and x at state i-1.

   * For selected property y, we augment the trace, such that each state i contains the whole history of pairs (p,yval) from the beginning of the trace up to i. Here p is the agent position at some previous state j, and yval is the value of y at that state j.

For example, the following will enrich `trace1` with first-derivatives, and history-tracking for the property `joy`:

```Java
trace1.enrichTrace("joy");
```

## Examples of some requirments for emotion-PX testing.
* example1- There should be no increase of hope in rooms 1 and 2, that can only happen on rooms 3

  ```Java
	LTL<XState> hopereq=ltlAnd(
      always(now(in(room1).or(in(room2))).implies(nH())),
      eventually(in(room3).and(H_()))) ;
  ```

 * example2- There is at least one trace in which fear should start increasing exactly at time 4 and for duration of at least 3 time stamps.

  ```Java  
  LTL<XState> temporalreq= eventually_within(until_atLeast(F(),nF(),3),4,4);
  ```

  more examples can be found in Test_ListofXStateTrace file.
  To use the DSL you need to place your csv-trace file in ltl-pxevaluation/src/test/data/.

