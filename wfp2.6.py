from __future__ import division
import operator, math
from itertools import chain, combinations
import codecs

def read_input(datafile):
    wids = set()
    windows = []
    weis = {}
    old_win_timespan = ''
    for line in codecs.open(datafile, 'r', encoding='utf-8').readlines():
        ele = line.split(',')
        win_timespan = ele[0]
        wid = ele[0] + ele[1]
        if not wid in wids:
            weis[line] = float(ele[5])
            wids.add(wid)
            if not win_timespan == old_win_timespan:
                windows.append([])
                old_win_timespan = win_timespan
            windows[-1].append(line)
    return windows, weis

def remain_max(y, k):
    remains = dict((k,v) for k,v in weight.items() if k!=y)
    sort_weight = sorted(remains.items(), key=operator.itemgetter(1), reverse=True)
    first_part = sort_weight[:k]
    return sum([float(x[1]) for x in first_part])

def B(y,k):
    bup = MIN_SUP*T
    W = float(weight[y]) + remain_max(y,k)
    return math.ceil(bup/W)
    
def Bmin(item_name):
    return min(B(item_name, k) for k in range(2,MAX_ITEM+1))

class WfpTree:
    def __init__(self, k, p):
        self.key = k
        self.count = 1
        self.parent = p
        self.children = []
        self.current = self
        self.level = 0 if self.parent is None else self.parent.level + 1

    def add_node(self, k):
        for child in self.current.children:
            if k == child.key:
                child.count += 1
                self.current = child
                return
        node = WfpTree(k, self.current)
        self.current.children.append(node)
        self.current = node

    def reset(self):
        self.current = self

    def __repr__(self):
        indent = "  " * self.level
        rep = indent + str(self.key) + ": " + str(self.count)
        rep +=  "\n" if self.parent is None else (" -> " + str(self.parent.key) + "\n")
        for child in self.children:
            rep += str(child)
        return rep

    def weight_sup(self):
        sum_weight = float(weight[self.key]) + (0 if self.parent.key == "root" else self.parent.weight_sup())
        return sum_weight * self.count / T

    def filter_tree(self, freq_set):
        if self.weight_sup() > MIN_SUP:
            freq_set.append(self)
        if len(self.children) > 0:
            for child in self.children:
                child.filter_tree(freq_set)

    def path(self):
        if self.parent.key == "root":
            return [self.key]
        parent_path = self.parent.path()
        parent_path.append(self.key)
        return parent_path

    @staticmethod
    def build_tree(data):
        root = WfpTree("root", None)
        for line in data:
            for val in line:
                root.add_node(val)
            root.reset()
        return root

    @staticmethod
    def weight_filter(tree):
        freq_ptn = []
        for item in tree.children:
            for child in item.children:
                child.filter_tree(freq_ptn)
        return [node.path() for node in freq_ptn]

def powerset(iterable):
    s = list(iterable)
    return chain.from_iterable(combinations(s, r) for r in range(len(s)+1))

def sup_cnt(ptn):
    cnt = 0
    for line in data:
        if ptn <= set(line):
            cnt += 1
    return cnt

def build_wfp_rules(ptns):
    res = []
    for ptn in ptns:
        if len(ptn) > MAX_RELATION_ORDER:
            continue
        pset = set(ptn)
        all_subsets = set(powerset(pset))
        all_subsets.remove(tuple(pset))
        all_subsets.remove(tuple())
        for aset in all_subsets:
            elem = set(aset)
            s_sup_count = sup_cnt(elem)
            l_sup_count = sup_cnt(pset)
            conf_ratio = (l_sup_count / s_sup_count)
            pre_wei_sum = sum([weight[warning] for warning in elem])
            pre_sup_ratio = s_sup_count * pre_wei_sum / T
            rule_wei_sum = sum([weight[warning] for warning in pset])
            rule_sup_ratio = l_sup_count * rule_wei_sum / T
            if conf_ratio >= MIN_CONF:
                res.append([elem, pset- elem, conf_ratio, rule_sup_ratio, pre_sup_ratio])
    return res

def calculate(data):
    item_sc = {}
    for line in data:
        for item in line:
            if not item in item_sc:
                item_sc[item] = 1
            else:
                item_sc[item] += 1
    filter_sup = dict((key, val) for key, val in item_sc.items() if Bmin(key) <= val)
    sort_sup = sorted(filter_sup.items(), key=operator.itemgetter(1), reverse=True)

    sort_data = []
    sk = [k for (k,v) in sort_sup]
    for item in data:
        sort_item = []
        for val in sk:
            if val in item:
                sort_item.append(val)
        sort_data.append(sort_item)

    sort_tree = WfpTree.build_tree(sort_data)

    # mapper: freq_patterns -> wfp_rules
    # reducer: wfp_rules union
    freq_patterns = WfpTree.weight_filter(sort_tree)

    return build_wfp_rules(freq_patterns)

def write_result(res):
    #print(result[-1][0].pop())
    print(result)

MIN_SUP = 0.0003
MIN_CONF = 0
MAX_RELATION_ORDER = 3
DATA_FILE = 'first1000'

data, weight = read_input(DATA_FILE)
T = len(data)
MAX_ITEM = max([len(line) for line in data])
result = calculate(data)
write_result(result)

