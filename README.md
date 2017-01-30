# Vote4CAP

(name subject to change)

## Introduction

This is a coding exercise meant to learn about the [IntelliJ](https://www.jetbrains.com/idea/) Java IDE.

This project is a script that scrapes ballots off web pages and processes them through a selected voting algorithm, designed for use with [Smogon's Create-a-Pokemon Project](http://www.smogon.com/cap/).

By default, the script uses a scraper designed for [XenForo](https://xenforo.com/) forums, but it can be extended and recompiled. Given enough interest, the scraper can be parametrized. (Oh my, a voting *framework*?)

This script uses [jsoup](https://jsoup.org/) to load and parse HTML.

### Reasoning

I originally wrote this script using the inspirations of [QxC4eva](https://www.smogon.com/forums/members/221662/), as the existing CAP voting script had bugs and relied on awkward hacks. Some of the problems resolved with this script include:

* Preferential block voting gives correct results.
* Script does not strip out non-alphabetical characters. E.g., in the existing script, `R2-D2` and `R 3 D 5` would be reported as the same option.
* `<br>` tags are used instead of relying on `<b>` tags to locate votes. Relying on `<b>` tags was error-prone, as split bold tags can break votes.
* * **NOTE**: Hence, this script requires a different ballot format than the previous voting script.

In addition, the script offers several other advantages:

* Low coupling: changes to the script are localized 
	(i.e., they do not require the rest of the script to be refactored).
* Extensible: Easy to design another ballot scraper or voting method and plugging it in.
* Ballots can be checked to verify that they are legal.
* Capitalization of entries is preserved.

This script does *not* automatically generate cutoffs for multiple-winner voting methods.

## How to Run

Windows:

1. Install the [Java JRE (and/or JDK)](http://www.oracle.com/technetwork/java/javase/overview/index.html) if Java is not already present on the system.
2. Open the command-line prompt.
3. Navigate to the directory of `Vote4CAP.jar` (`.\out\artifacts\Vote4CAP_jar\` by default).
4. Type in the following and press "enter":

```
java -jar Vote4CAP.jar <poll URL> <poll type> [-v]
```

E.g.:

```
java -jar Vote4CAP.jar http://www.smogon.com/forums/threads/3579745/ PBV
```

More information about the parameters can be found by running the script.

The intended user is expected to be familiar with the names of the listed poll types as the second parameter. Further elaboration of this parameter may be added depending on the future of this project.

# Warning

This script may be changed and reworked at any time, subject to the whims of the developers.
