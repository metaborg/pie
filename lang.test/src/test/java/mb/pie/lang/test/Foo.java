package mb.pie.lang.test;

import java.io.Serializable;

public class Foo implements Serializable {
	public boolean equals(Object other) {
		return other instanceof Foo;
	}
}
