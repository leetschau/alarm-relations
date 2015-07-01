# Introduction

This project aims for analyze relations between huge amount of alarms created by NE.

The file wfp.scala are implementation of weighted FP algorithms in Scala
running on [Apache Spark](https://spark.apache.org/) single host or cluster.
Python scripts are implementation running on a single host.

inp.txt is input data file.
res_rule.txt is the resource relation definition file.
See note [告警资源过滤算法](http://leetschau.github.io/blog/2014/10/24/150059/) for details.

Under the root directory of the project, run `spark-shell -i wfp.scala`,
the result will be saved in file rule_sets.txt.

The buildWin.scala is a window-locating algorithm.
It tells an event will be put in which time windows depends on its occuring time.
For example, start spark shell with `spark-shell -i buildWin.scala`.
After the script is loaded (in the spark shell),
run `buildWin("2014-09-12 00:18:34")`,
you can get something like 
`res5: Array[(String, String)] = Array((2014-09-12 00:15:00,2014-09-12 00:20:00), (2014-09-12 00:18:00,2014-09-12 00:23:00))`.

It means an event happen at 2014-09-12 00:08:34 will be put into two window:
(2014-09-12 00:15:00,2014-09-12 00:20:00) and
(2014-09-12 00:18:00,2014-09-12 00:23:00).
