// This file was generated from Pie source file foreignTaskTestGen.pie.
package mb.pie.lang.test.supplier.fromTaskAndGet.foreignTask;

import java.io.Serializable;
import mb.pie.api.TaskDef;
import mb.pie.api.None;
import mb.pie.api.ExecException;
import mb.pie.api.ExecContext;
import javax.inject.Inject;
import mb.pie.api.Supplier;
import javax.inject.Provider;
import java.io.IOException;

public class foreignTask implements TaskDef<Boolean, String> {
  private static final String _id = "foreignTask";
  @Inject public foreignTask( ) {
  }
  public String getId( ) {
    return foreignTask._id;
  }
  @Override public Serializable key(Boolean input) {
    return input;
  }
  @Override public String exec(ExecContext execContext, Boolean input) throws ExecException {
    return "Not(" + input + ") = " + !input;
  }
}
// last cache update: 2020-01-09 13:52
