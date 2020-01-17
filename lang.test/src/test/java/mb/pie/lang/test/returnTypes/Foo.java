package mb.pie.lang.test.returnTypes;

import java.io.Serializable;

class Foo implements Serializable {
	public boolean equals(Object other) {
		return other instanceof Foo;
	}
}
