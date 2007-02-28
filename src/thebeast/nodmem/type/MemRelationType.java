package thebeast.nodmem.type;

import thebeast.nod.type.*;
import thebeast.nod.value.Value;
import thebeast.nodmem.mem.MemChunk;
import thebeast.nodmem.mem.MemVector;
import thebeast.nodmem.mem.MemPointer;
import thebeast.nodmem.value.MemRelation;
import thebeast.nodmem.value.AbstractMemValue;

import java.util.List;
import java.util.LinkedList;
import java.io.*;

/**
 * @author Sebastian Riedel
 */
public class MemRelationType extends AbstractMemType implements RelationType {

  private MemHeading heading;
  private LinkedList<KeyAttributes> candidateKeys;

  public MemRelationType(MemHeading heading) {
    this.heading = heading;
    setNumChunkCols(heading.getNumChunkCols());
    setNumDoubleCols(heading.getNumDoubleCols());
    setNumIntCols(heading.getNumIntCols());

    candidateKeys = new LinkedList<KeyAttributes>();
    candidateKeys.add(new MemKeyAttributes(heading, heading.attributes()));
  }

  public MemRelationType(MemHeading heading, List<KeyAttributes> candidates) {
    this.heading = heading;
    setNumChunkCols(heading.getNumChunkCols());
    setNumDoubleCols(heading.getNumDoubleCols());
    setNumIntCols(heading.getNumIntCols());
    candidateKeys = new LinkedList<KeyAttributes>(candidates);
  }


  public void acceptTypeVisitor(TypeVisitor visitor) {
    visitor.visitRelationType(this);
  }

  public MemHeading heading() {
    return heading;
  }

  public List<KeyAttributes> candidateKeys() {
    return candidateKeys;
  }

  public Type instanceType() {
    return new MemTupleType(heading);
  }

  public Value emptyValue() {
    return null;
  }

  public AbstractMemValue valueFromChunk(MemChunk chunk, MemVector pointer) {
    MemChunk memChunk = chunk.chunkData[pointer.xChunk];
    assert memChunk != null;
    return new MemRelation(memChunk, new MemVector(), this);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("RELATION {");
    buffer.append(heading);
    buffer.append("}");
    return buffer.toString();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MemRelationType that = (MemRelationType) o;

    return heading.equals(that.heading);

  }

  public int hashCode() {
    return heading.hashCode();
  }

  public void loadFromRows(InputStream is, MemChunk dst, MemVector ptr) throws IOException {
    //actually we need the inside chunk
    dst = dst.chunkData[ptr.xChunk];
    dst.size = 0;
    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    r.mark(3);
    int first = r.read();
    if (first == '#'){
      String line = r.readLine().trim();
      int length = Integer.valueOf(line);
      if (dst.capacity < length) dst.increaseCapacity(length - dst.capacity);
    } else {
      r.reset();
    }
    StreamTokenizer tokenizer = new StreamTokenizer(r);
    tokenizer.parseNumbers();
    tokenizer.quoteChar('"');
    tokenizer.quoteChar('\'');
    tokenizer.whitespaceChars(' ',' ');
    tokenizer.whitespaceChars('\t','\t');
    tokenizer.whitespaceChars('\n','\n');
    MemVector current = new MemVector(ptr);
    int line = 0;
    while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
      if (line++ >= dst.capacity) dst.increaseCapacity(40);
      tokenizer.pushBack();
      int index = 0;
      for (Attribute attribute : heading.attributes()) {
        AbstractMemType type = (AbstractMemType) attribute.type();
        MemVector local = new MemVector(current);
        local.add(heading.pointerForIndex(index++));
        type.load(tokenizer, dst, local);
      }
      current.add(getDim());
      ++dst.size;
    }
  }


}
