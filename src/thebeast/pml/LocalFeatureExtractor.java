package thebeast.pml;

import thebeast.nod.expression.AttributeExpression;
import thebeast.nod.expression.DepthFirstExpressionVisitor;
import thebeast.nod.expression.RelationExpression;
import thebeast.nod.expression.Equality;
import thebeast.nod.statement.Interpreter;
import thebeast.nod.statement.Insert;
import thebeast.nod.statement.StatementFactory;
import thebeast.nod.statement.RelationAppend;
import thebeast.nod.variable.Index;
import thebeast.nod.variable.RelationVariable;
import thebeast.pml.formula.FactorFormula;
import thebeast.pml.formula.PredicateAtom;
import thebeast.pml.formula.QueryGenerator;
import thebeast.pml.function.WeightFunction;
import thebeast.util.HashMultiMapList;

import java.util.HashSet;

/**
 * The LocalFeatureExtractor can take some {@link thebeast.pml.GroundAtoms} and extract a set of {@link
 * thebeast.pml.LocalFeatures}. This functionality is taken out of the features object because there might be many local
 * feature objects (cached during training) and the all use the same queries.
 */
public class LocalFeatureExtractor {

  private HashMultiMapList<UserPredicate, RelationExpression>
          queries = new HashMultiMapList<UserPredicate, RelationExpression>();
  private HashMultiMapList<UserPredicate, Insert>
          inserts = new HashMultiMapList<UserPredicate, Insert>();
  private HashMultiMapList<UserPredicate, RelationAppend>
          appends = new HashMultiMapList<UserPredicate, RelationAppend>();
  private Interpreter interpreter = TheBeast.getInstance().getNodServer().interpreter();
  private StatementFactory factory = TheBeast.getInstance().getNodServer().statementFactory();
  private Model model;
  private Weights weights;
  private LocalFeatures features;

  private GroundAtoms atoms;

  public LocalFeatureExtractor(Model model, Weights weights) {
    configure(model, weights);
  }

  private void configure(Model model, Weights weights) {
    atoms = model.getSignature().createGroundAtoms();
    this.weights = weights;
    this.model = model;
    this.features = new LocalFeatures(model, weights);
    QueryGenerator generator = new QueryGenerator(model, weights, atoms);
    for (FactorFormula formula : model.getLocalFactorFormulas()) {
      if (!formula.usesWeights()) continue;
      RelationExpression query = generator.generateLocalQuery(formula, atoms, weights);
      UserPredicate userPredicate = (UserPredicate) ((PredicateAtom) formula.getFormula()).getPredicate();
      queries.add(userPredicate, query);
      Insert insert = factory.createInsert(features.getRelation(userPredicate), query);
      inserts.add(userPredicate, insert);
      RelationAppend append = factory.createRelationAppend(features.getRelation(userPredicate), query);
      appends.add(userPredicate, append);
      WeightFunction weightFunction = formula.getWeightFunction();
      RelationVariable relvar = weights.getRelation(weightFunction);
      if (relvar.getIndex(weightFunction.getName()) == null) {
        final HashSet<String> bound = new HashSet<String>();
        query.acceptExpressionVisitor(new DepthFirstExpressionVisitor() {

          public void visitEquality(Equality equality) {
            if (equality.leftHandSide() instanceof AttributeExpression) {
              AttributeExpression lhs = (AttributeExpression) equality.leftHandSide();
              if (lhs.prefix().equals("weights"))
                bound.add(lhs.attribute().name());
            } else if (equality.leftHandSide() instanceof AttributeExpression) {
              AttributeExpression rhs = (AttributeExpression) equality.rightHandSide();
              if (rhs.prefix().equals("weights"))
                bound.add(rhs.attribute().name());
            }
          }

//          public void visitAttribute(AttributeExpression attribute) {
//            if (attribute.prefix().equals("weights"))
//              bound.add(attribute.attribute().name());
//          }
        });
        interpreter.addIndex(relvar, weightFunction.getName(), Index.Type.HASH, bound);
      }
    }
  }

  /**
   * Returns the model this extractor is using
   *
   * @return a PML model
   */
  public Model getModel() {
    return model;
  }

  /**
   * The extractor extracts only features with nonzero weights, which are taken from the
   * weights object this method returns.
   *
   * @return the weights this extractor is using.
   */
  public Weights getWeights() {
    return weights;
  }

  /**
   * Extracts all local features for all ground atoms.
   *
   * @param groundAtoms the ground atoms we use as observation
   * @param features    the local features object the extractor writes its result to.
   */
  public void extract(GroundAtoms groundAtoms, LocalFeatures features) {
    atoms.load(model.getGlobalAtoms(), model.getGlobalPredicates());
    atoms.load(groundAtoms, model.getInstancePredicates());
    //this.features.load(features);
    features.clear();
    for (UserPredicate pred : model.getHiddenPredicates()) {
//      for (RelationAppend append : appends.get(pred))
//        interpreter.interpret(append);
//      for (Insert insert : inserts.get(pred))
//        interpreter.interpret(insert);
      for (RelationExpression expression : queries.get(pred)) {
        //interpreter.insert(features.getRelation(pred), expression);
        interpreter.append(features.getRelation(pred), expression);
      }
    }
    features.invalidate();
    //features.load(this.features);
  }

}
