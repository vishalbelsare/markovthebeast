package thebeast.util;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Sebastian Riedel
 */
public class TestUtil extends TestCase {

  public void testNBalls(){
    int[][] result = Util.nBalls(2, new int[]{1,2,3,4,5});
    assertEquals(10, result.length);
    assertEquals(result[0][0],1);
    assertEquals(result[0][1],2);
    assertEquals(result[1][0],1);
    assertEquals(result[1][1],3);
    assertEquals(result[2][0],1);
    assertEquals(result[2][1],4);
    assertEquals(result[3][0],1);
    assertEquals(result[3][1],5);
    assertEquals(result[4][0],2);
    assertEquals(result[4][1],3);
    assertEquals(result[5][0],2);
    assertEquals(result[5][1],4);
    assertEquals(result[6][0],2);
    assertEquals(result[6][1],5);
    assertEquals(result[7][0],3);
    assertEquals(result[7][1],4);
    assertEquals(result[8][0],3);
    assertEquals(result[8][1],5);
    assertEquals(result[9][0],4);
    assertEquals(result[9][1],5);
    System.out.println(result);
  }

  public void testClosure(){
    Set<Pair<Integer,Integer>> graph = new HashSet<Pair<Integer, Integer>>();
    graph.add(new Pair<Integer, Integer>(0,2));
    graph.add(new Pair<Integer, Integer>(2,4));
    graph.add(new Pair<Integer, Integer>(2,6));
    Set<Pair<Integer,Integer>> result = Util.transitiveReflexiveClosure(graph);
    System.out.println(result);
    assertTrue(result.contains(new Pair<Integer,Integer>(0,4)));
    assertTrue(result.contains(new Pair<Integer,Integer>(0,2)));
    assertTrue(result.contains(new Pair<Integer,Integer>(0,6)));
    assertTrue(result.contains(new Pair<Integer,Integer>(2,4)));
    assertTrue(result.contains(new Pair<Integer,Integer>(2,6)));
  }

}
