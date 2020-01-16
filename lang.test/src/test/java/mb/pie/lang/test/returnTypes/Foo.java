package mb.pie.lang.test.returnTypes;

import java.io.Serializable;

public import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class Foo implements Serializable {
	public boolean equals(Object other) {
		return other instanceof Foo;
	}
}
