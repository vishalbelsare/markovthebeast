package thebeast.nod.variable;

import thebeast.nod.value.RelationValue;
import thebeast.nod.value.ArrayValue;
import thebeast.nod.type.RelationType;
import thebeast.nod.type.ArrayType;
import thebeast.nod.expression.ArrayExpression;

/**
 * @author Sebastian Riedel
 */
public interface ArrayVariable extends Variable<ArrayValue,ArrayType>, ArrayExpression {
  int byteSize();

  double doubleValue(int index);

  void setDoubleArray(double[] array);

  void fill(double value, int howmany);

  void enforceBound(int[] indices, boolean lower, double value);

  int nonZeroCount(double eps);
}
