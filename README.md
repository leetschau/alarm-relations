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
