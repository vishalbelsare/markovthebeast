predicate word: Int x Word;
predicate pos: Int x Pos;
predicate span: Int x Int x Int;
predicate label: Int x Label;
predicate parentlabel: Int x Label;
predicate head: Int x Int;
predicate parenthead: Int x Int;
predicate candidate: Int;
predicate path: Int x Path;
predicate pathlength: Int x Int;
predicate pred: Int x Predicate x Voice;
predicate subcat: Subcat;
predicate position: Int x Position;
predicate frame: Int x Frame;
predicate framepattern: Int x FramePattern;
predicate shortframe: Int x ShortFrame;
predicate chunkdistance: Int x Int;
predicate sister: Int x Int x Int;
predicate pprightmosthead: Int x Int;
predicate distance: Int x Int;
predicate class: Class;
predicate nooverlap: Int x Int;

//global predicates
predicate properarg: Argument;
predicate modifier: Argument;
predicate carg: Argument;
predicate rarg: Argument;
predicate allargs: Argument;
predicate cargpair: Argument x Argument;
predicate rargpair: Argument x Argument;


predicate arg: Int x Argument;
predicate isarg: Int;
predicate argpair: Int x Int x Argument x Argument;

//index: span(*,*,_);

hidden: arg,isarg, argpair;//,class;
observed: word,pos,span,label,head,candidate,pred,path,subcat,position,pathlength,shortframe,
  frame,chunkdistance,framepattern,parentlabel,parenthead,sister,pprightmosthead,distance,nooverlap;
global: properarg, modifier, carg, rarg, cargpair, rargpair,allargs;

auxiliary: argpair;


include "weights-arg.pml";
include "weights-isarg.pml";
include "weights-argpair.pml";
//include "weights-class.pml";
//include "weights-isarg-compact.pml";

load global from "global.atoms";


//weight w_argpair[2]: Argument x Argument -> Double-;
//factor argpair:
//  for Int c1, Int c2, Argument a1, Argument a2, Int b1, Int b2
//  if candidate(c1) & candidate(c2) & span(c1,b1,_) & span(c2,b2,_) & b2 > b1
//  add [arg(c1,a1) & arg(c2,a2)] * w_argpair(a1,a2);
//set collector.all.w_argpair = true;

//induce non overlapping candidates
//factor : for Int c1, Int c2, Int b1, Int e1, Int b2, Int e2
//  if span(c1,b1,e1) & span(c2,b2,e2) & b1 < b2 & e1 < b2 : nooverlap(c1,c2);

//induce argpairs
factor[0]: for Int c1, Int c2, Argument a1, Argument a2
  if nooverlap(c1,c2) : arg(c1,a1) & arg(c2,a2) => argpair(c1,c2,a1,a2);

//induce args
factor[0]: for Int c1, Int c2, Argument a1, Argument a2
  if nooverlap(c1,c2) : argpair(c1,c2,a1,a2) => arg(c1,a1);

factor[0]: for Int c1, Int c2, Argument a1, Argument a2
  if nooverlap(c1,c2) : argpair(c1,c2,a1,a2) => arg(c2,a2);

//no overlaps
factor [1]: for Int c1, Int c2, Int b1, Int e1, Int b2, Int e2, Argument a1, Argument a2
  if span(c1,b1,e1) & span(c2,b2,e2) & b1 < b2 & e1 >= b2 : !(arg(c1,a1) & arg(c2,a2));

factor [1]: for Int c1, Int c2, Int b1, Int e1, Int b2, Int e2
  if span(c1,b1,e1) & span(c2,b2,e2) & b1 < b2 & e1 >= b2 : !(isarg(c1) & isarg(c2));

factor [0]: for Argument a if properarg(a) : |Int c: arg(c,a)| <= 1;

factor [0]: for Argument a1 if properarg(a1) : |Int c1, Int c2, Argument a2: nooverlap(c1,c2) & argpair(c1,c2,a1,a2)| <= 1;
factor [0]: for Argument a1 if properarg(a1) : |Int c1, Int c2, Argument a2: nooverlap(c1,c2) & argpair(c1,c2,a2,a1)| <= 1;


//not more than one argument for a candidate
factor [0]: for Int c if candidate(c): |Argument a: arg(c,a)| <= 1;

factor [0]: for Int c1, Int c2 if nooverlap(c1,c2): |Argument a1, Argument a2: argpair(c1,c2,a1,a2)| <= 1;

//if there is an isarg there has to be an arg
factor [1]: for Int c if candidate(c): isarg(c) => |Argument a: arg(c,a)| >= 1;


//if there is an arg there has to be a isarg
//factor implyIsarg: for Int c, Argument a if candidate(c): arg(c,a) => isarg(c);

//factor implyIsarg: for Int c if candidate(c): |Argument a: arg(c,a)| >= 1 => isarg(c);

/*
//not duplicate arguments
factor duplicatearg: for Argument a if properarg(a) : |Int c: arg(c,a)| <= 1;

//if there is a c-arg there has to be an arg
factor cargimpliesarg: for Argument start, Argument ca, Int c, Int bc
  if carg(ca) & cargpair(ca,start) & candidate(c) & span(c,bc,_) :
  arg(c,ca) => |Int a, Int ba: candidate(a) & span(a,ba,_) & ba < bc & arg(a,start)| >= 1;

factor rargimpliesarg: for Argument start, Argument ra, Int c
  if carg(ra) & rargpair(ra,start) & candidate(c) :
  arg(c,ra) => |Int a: candidate(a) & arg(a,start)| >= 1;
*/

//at least one argument
//factor atLeastOne: |Int c, Argument a: candidate(c) & arg(c,a)| >= 1;


  
