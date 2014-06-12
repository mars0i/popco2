Notes on random sampling, etc.
=======

## Base Clojure functions

In Clojure 1.6.0 (and probably all previous versions since 1.0), `rand` does:
````clojure
(. Math (random))
````
which means the same thing as
````clojure
(. Math random)
````
which in Java is
````java
java.lang.Math.random()
````
which creates a new random number generator (RNG) by calling
`java.util.Random()`, if necessary, and then calls its `nextDouble()`
method.  Note that this takes a new seed by default, (from the system time, I think),
so you'll get different results from `rand` in every session.

If `rand` is giving an integer argument, it simply multiplies that
integer by the value of an argument-less call to `rand`.

Clojure's `rand-int` just calls `(rand n)` and passes the result to
`int`, which *truncates* the result to an integer.

`rand-nth` just calls `nth` using `(rand-int (count coll)` as the index.

## data.generators

data.generators' `*rnd*` is a `java.util.Random` that's always seeded
with 42 by default, so if you don't set `*rnd*` yourself, you'll always
get the same results from data.generators functions.

`long` (= `uniform`) is the closest thing to `rand-int`.  You can call it
with no arguments, in which case it takes random values from the full range
of a long, or you can give it a high and low value as arguments.  In the latter case,
`long` or `uniform` simply adds the low value to the normal double produced by
`java.util.Random` with `.nextDouble`, multiplies it by the difference between
low and high, and truncates the result into a long.

data.generator's `rand-nth` is essentially the same as Clojure's
built-in `rand-nth`, but using `(uniform 0 (count coll))` instead of
`(rand-int (count coll))`.  Conceivably the Clojure `rand-nth` could be
slightly faster, after the first call requiring a random generator?
It's probably not a measurable difference.

## MersenneTwister*.java


### On MersenneTwisterFast vs. MersenneTwister

I emailed this question to the author of these classes, Sean Luke:

> Is the reason that MersenneTwisterFast doesn't subclass java.util.Random
> simply that it's not syncrhonized?  Is there any reason why I can't use
> a version with "extends java.util.Random" added, as long as I'm careful
> not to access a single instance from different threads?  (Seems to
> work.)

Sean responded: 

> There are several reasons MTF doesn't subclass java.util.Random,
> including synchronization and the inability of Java VMs to inline
> methods in subclasses.  Don't do that.  There's a perfectly good
> subclass of java.util.Random which does Mersenne Twister already: it's
> MTF's sibling, MersenneTwister.java.

### notes on methods

clone() creates a new rng with the same state.

stateEquals() determines whether two rngs have the same state.  Note that
if they started with the same state, but they haven't been next'ed the
same number of times, they'll have different states.

writeState() and readState() look useful.
