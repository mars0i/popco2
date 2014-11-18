popco2
=======

**Pop**ulation **Co**herence: Cultural transmission with
analogy-influenced biases, using Holyoak and Thagard's (1989) ACME model
of analogy recognition.   This is a rewrite of
[popco](https://github.com/mars0i/popco) in Clojure, using different
data structures, algorithms, etc., but with the same functionality
except for some peripheral details.

popco2 is a framework for agent-based simulations in which agents'
communication of their simulated beliefs depends how those beliefs do
or do not fit into analogies.  It involves "coherence" in two senses:
(a) it often tends to generate agreement between agents, and (b) the
ACME model of analogy processing can be viewed as based on ideas about
coherence (see references below). For the motivation of this project,
an illustration of its use, and the primary documentation of how the
software works, see the open access article described below.

This software is copyright 2013, 2014, 2015 by [Marshall
Abrams](http://members.logical.net/~marshall/), and is distributed
under the [Gnu General Public License version
3.0](http://www.gnu.org/copyleft/gpl.html) as specified in the file
LICENSE, except where noted.  (For example, there is source code in
src/java that was written by other authors, which is released under
different licenses.)

Please feel free to contact me with questions, suggestions, interest
in help developing popco simulations, etc. at:

	mabrams ([at]) uab [(dot)] edu
	marshall ([at]) logical [(dot)] net  

In some documents here I refer to the original popco as "popco1".  I often
refer to the current version as "popco" rather than "popco2" when context
makes the intended sense clear.  This repository was previously called
"popco-x".


-------

#### Article on the original (Common Lisp) version of popco:

#### Marshall Abrams, ["A moderate role for cognitive models in agent-based modeling of cultural change"](http://www.casmodeling.com/content/1/1/16), *Complex Adaptive Systems Modeling* 2013, 1(16):1-33.  (Also see this [correction](http://www.casmodeling.com/content/2/1/1).)

#### Abstract:

##### Purpose

Agent-based models are typically "simple-agent" models, in which agents
behave according to simple rules, or "complex-agent" models which
incorporate complex models of cognitive processes. I argue that there is
also an important role for agent-based computer models in which agents
incorporate cognitive models of moderate complexity. In particular, I
argue that such models have the potential to bring insights from the
humanistic study of culture into population-level modeling of cultural
change.

##### Methods

I motivate my proposal in part by describing an agent-based modeling
framework, POPCO, in which agents' communication of their simulated
beliefs depends on a model of analogy processing implemented by
artificial neural networks within each agent. I use POPCO to model a
hypothesis about causal relations between cultural patterns proposed by
Peggy Sanday.

##### Results

In model 1, empirical patterns like those reported by Sanday emerge from
the influence of analogies on agents' communication with each other.
Model 2 extends model 1 by allowing the components of a new analogy to
diffuse through the population for reasons unrelated to later effects of
the analogy. This illustrates a process by which novel cultural features
might arise.

##### Conclusions

The inclusion of relatively simple cognitive models in agents allows
modeling population-level effects of inferential and cultural coherence
relations, including symbolic cultural relationships. I argue that such
models of moderate complexity can illuminate various causal
relationships involving cultural patterns and cognitive processes.

Keywords: Simulation; Culture; Cognition; Analogy; Metaphor;
Hermeneutics

-------

#### Other sources:

Keith Holyoak and Paul Thagard (1989), "Analogical mapping by constraint
satisfaction". *Cognitive Science* 13:295–355.

Keith Holyoak and Paul Thagard (1995), *Mental Leaps: Analogy in
Creative Thought*. Cambridge, Massachusetts: MIT Press.

Paul Thagard (2000), *Coherence in Thought and Action*. Cambridge,
Massachusetts: MIT Press.

<br/>

For additional relevant sources, see the article in *Complex Adaptive
Systems Modeling* that's described above.
