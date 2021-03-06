type SourceWord : ...;
type TargetWord : ...;

//Source-Position x Source-Word
predicate source: Int x SourceWord;

//Source-Position x Group-ID
predicate group: Int x Int;

//Group-Id x Target-Id x TargetWord
predicate target: Int x Int x TargetWord;

//Group-Id x Translation-Prob
predicate groupScore: Int x Double;

//Target-Id x Target-Id x Follows-Prob
predicate followsScore: Int x Int x Double;

//Target-Id x Target-Id
predicate follows: Int x Int;

//Group-Id
predicate activeGroup: Int;

//Target-Id
predicate activeTarget: Int;

index: follows(*,*);
index: followsScore(*,*,_);

factor atMostOneGroup: for Int i if source(i,_) :
  |Int group: group(i,group) & activeGroup(group)| <= 1;

factor atLeastOneGroup: for Int i if source(i,_) :
  |Int group: group(i,group) & activeGroup(group)| >= 1;


factor groupImpliesTargetActive: for Int group, Int target if target(group,target,_):
  activeGroup(group) => activeTarget(target);

factor targetImpliesGroupActive: for Int group, Int target if target(group,target,_) & target >= 0:
  activeTarget(target) => activeGroup(group);

factor atLeastOneEnd: for Int begin if target(_,begin,_) & begin != 1 & begin >= 0:
  activeTarget(begin) => |Int end: target(_,end,_) & follows(begin,end) & followsScore(begin,end,_)| >= 1;

factor atMostOneEnd: for Int begin if target(_,begin,_):
  |Int end: follows(begin,end) & target(_,end,_) & followsScore(begin,end,_)| <= 1;

factor atLeastOneBegin: for Int end if target(_,end,_) & end > 0:
  activeTarget(end) => |Int begin: target(_,begin,_) & follows(begin,end)& followsScore(begin,end,_)| >= 1;

factor atMostOneBegin: for Int end if target(_,end,_):
  |Int begin: follows(begin,end) & target(_,begin,_) & followsScore(begin,end,_)| <= 1;
  

factor followsActiveBegin: for Int begin, Int end if followsScore(begin,end,_):
  follows(begin,end) => activeTarget(begin);

factor followsActiveEnd: for Int begin, Int end if followsScore(begin,end,_):
  follows(begin,end) => activeTarget(end);

factor acyclicity: follows acyclic;

factor: for Int begin, Int end, Double score if followsScore(begin,end,score)
  add [follows(begin,end)] * score;

factor: for Int group, Double score if groupScore(group,score)
  add [activeGroup(group)] * score;


