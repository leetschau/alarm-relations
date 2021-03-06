import Math.ceil
import scala.io

val MIN_SUP = 0.004
val MIN_CONF = 0.1
val MAX_RELATION_ORDER = 3
val DATA_FILE = "inp.txt"
val RES_FILE = "res_rule.txt"
val SEP = "\001"
val WEI_IDX = 5
val MIN_INT_ID_LEN = 5
val BMIN_CNT_WEI = 0.3

val rawData = sc.textFile(DATA_FILE).distinct
val data = rawData.filter(x => x.split(SEP)(1).split("_")(0).size > MIN_INT_ID_LEN)   
val item_count = data.map(_.split(SEP)(1)).map(w => (w,1)).reduceByKey(_+_)
val wids = data.map(_.split(SEP)(0))
val MAX_ITEM = wids.map(w => (w,1)).reduceByKey(_+_).map(_._2).max
val T = wids.distinct.count

val weight = data.map(x => (x.split(SEP)(1), x.split(SEP)(WEI_IDX).toFloat)).distinct
val id_cnt_wei = item_count.join(weight).map(x => (x._1, x._2._1, x._2._2))

val flt_sup_cnt = id_cnt_wei.map(x => (x._2 * x._3 , x)).top((id_cnt_wei.count * BMIN_CNT_WEI).toInt).map(_._2)

val almid_alm = data.map(x => (x.split(SEP)(1), x))
val alm_cnt = almid_alm.join(item_count)

val min_cnt = flt_sup_cnt.map(_._2).min
val flt_alm_cnt = alm_cnt.filter(x => x._2._2 >= min_cnt)

val flt_alms = flt_alm_cnt.map(_._2)
val devide_to_window = flt_alms.groupBy(x => x._1.split(SEP)(0))

val sort_in_window = devide_to_window.map(x => x._2.toList.sortBy(_._2).reverse)

val id_in_window = sort_in_window.map(x => x.map(y => y._1.split(SEP)(1)))

def buildBranch(inp: List[String]): IndexedSeq[String] = {
  val baseArr = (1 to inp.size).map(inp.take(_))
  baseArr.map(x => x.reduce(_ + "," + _))
}
val str_tree = id_in_window.flatMap(buildBranch)
val small_tree = str_tree.distinct.filter(_.split(",").size <= MAX_RELATION_ORDER)
val branch_cnt = str_tree.filter(_.split(",").size <= MAX_RELATION_ORDER).map(w => (w,1)).reduceByKey(_+_)

val node_branch = small_tree.flatMap(x => x.split(",").map(y => (y, x)))
val branch_wei_sum = node_branch.join(weight).map(x => (x._2._1, x._2._2)).reduceByKey(_+_)
val branch_sup = branch_wei_sum.join(branch_cnt).map(x => (x._1, x._2._1 * x._2._2))
val flt_tree = branch_sup.filter(_._2 / T > MIN_SUP).map(_._1)

val branch_sets = flt_tree.map(x => (x, x.split(",").toSet.subsets.toSet - Set() - x.split(",").toSet))
val branch_subsets = branch_sets.map(x => (x._1, x._2.map(_.reduce(_ + "," + _))))
val branch_subbran = branch_subsets.flatMap(x => x._2.map(y => (x._1, y)))

val window_sets = id_in_window.map(_.toSet)

val all_branch_sets = branch_sets.flatMap(x => Set(x._1.split(",").toSet) ++ x._2)
val all_bran_sets_merge = all_branch_sets.map(_.reduce(_ + "," + _)).distinct.map(_.split(",").toSet)
val subset_winset = all_bran_sets_merge.cartesian(window_sets).filter(x => x._1 subsetOf x._2)
val subsets_cnt = subset_winset.groupByKey().map(x => (x._1, x._2.size))

val branch_join_subset_cnt = branch_subbran.map(x => (x._1.split(",").toSet, x)).join(subsets_cnt).map(x => x._2)
val subbran_join_subset_cnt = branch_subbran.map(x => (x._2.split(",").toSet, x)).join(subsets_cnt).map(x => x._2)
val branch_ratio = branch_join_subset_cnt.join(subbran_join_subset_cnt).map(x => (x._1, x._2._1.toFloat / x._2._2.toFloat, x._2._1.toFloat/T))

val set_prefix = branch_ratio.filter(_._2 > MIN_CONF)

val conf_sup_prefix_suffix = set_prefix.map(x => (x._2, x._3, x._1._2, (x._1._1.split(",").toSet -- x._1._2.split(",").toSet).toArray.reduce(_ + "," + _)))

// filter by resourse relation
val res_data = sc.textFile(RES_FILE).map(x => x.split(",").reduce(_ + "," + _))
val combine_pre_suf = conf_sup_prefix_suffix.flatMap(x => (x._3.split(",").flatMap(y => (x._4.split(",").map(z => (y+","+z, x))))))
val suf_pre = combine_pre_suf.map(x => (x._1.split(",")(1) + "," + x._1.split(",")(0), x._2))
val double_pre_suf_rule = suf_pre ++ combine_pre_suf
val res_join_double = res_data.map(x => (x,1)).join(double_pre_suf_rule)
val rule_in_res_cnt = res_join_double.map(x => (x._2._2, x._2._1)).reduceByKey(_+_)
val flt_res_rules = rule_in_res_cnt.filter(x => x._1._3.split(",").size * x._1._4.split(",").size == x._2)


val rule_sets = conf_sup_prefix_suffix.toArray.toList.sortBy(_._1)(Ordering[Float].reverse)
scala.tools.nsc.io.File("rule_sets.txt").writeAll(rule_sets.map(_.toString).reduce(_ + "\n" + _))

