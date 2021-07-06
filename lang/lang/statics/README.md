# Ad-hoc Documentation
## Scoping
Shadowing of values and functions is disabled.
Disabling it is backwards compatible, and it may prevent bugs and make code
more readable.
Names leaking in from higher scopes is not a concern because values are only
available in functions and functions should be small.
Shadowing for functions does not matter right now, so I chose the backwards
compatible option.

## Notes on the PIE DSL type system
### Subtyping
Values in PIE are immutable.
This means that recursive types could be subtypes if their inner types are
subtypes.
For example, assuming int is a subtype of number, a list of ints can be a
subtype of a list of numbers `int* < number*`.
The same goes for suppliers and tuples.
This is not implemented because it would complicate the compilation to Java.

The bottom type (sometimes called "never" or "Nothing") is implemented because
that was useful for the implementation of wildcards and generic parameters.
Bottom types cannot be compiled to Java, but this is fine because they can either be omitted (in the case of wildcard / generic parameter bounds) or not compiled at all expressions with bottom type throw an exception, so all code after that can be removed.
The type of `return` and `fail` expressions could be changed to bottom type, but this has not been done yet.
See [issue 115](https://github.com/MeAmAnUsername/pie/issues/115)

### Adding two values
The type of two values depends on their static types:
Adding two ints just uses mathematical plus: 1 + 2 = 3.
Adding any value to a string converts the value to a string and then
concatenates the strings.
Adding a string or a path to a path concatenates the values.
Finally, adding a type `T2` to a list with type `ListType(T1)` has two cases:
- adding two lists (`T2 == ListType(T2_inner)`) will concatenate the lists.
  `T2_inner` must be a subtype of `T1`.
  If you want to add a list `xs: T*` as an element to a list `yss: T**`, use `yss + [xs]`
- All other cases will add the second item to the first list.
  This requires that `T2` is a (non-strict) subtype of T1 (ignoring nullability).
  The element type of the resulting list is T1, unless T2 is nullable.
  In that case, the resulting element type is nullable as well.

### Combining branches in if-else to TopType()
This is an error because I don't see a use case for it.

## Implementation
### Conventions
Lexical and semantic types have sorts `Type` and `TYPE` respectively.
Lexical types end in `Ty`, semantic types end in `Type`.
All functions only take the semantic types, the only place where syntactic
types are used is when they come in from the AST.
They are converted using `typeOfType`.

### File structure and dependencies
Types and functions on them are defined in type.stx.
Booleans, resolution and declaration are defined base.stx.
type.stx and base.stx mutually depend on each other.

util.stx depends on type.stx and base.stx.
It contains functions with frequently used patterns.

Most other files depend on type.stx, base.stx and util.stx as required.

file.stx is the entry point for Statix analysis.
To change the entry point, go to trans/analysis.str

### Wildcards
Wildcards differentiate between an explicit top type upper bound and no upper bound.
This is not useful right now, but is required if we ever want to copy the upper bound from a supertype.
For example, in
```
data Foo<T : Fruit> = foreign java org.example.Foo {}
data Bar<E> : Foo<E> = foreign java org.example.Bar {}
data Buz<A : Object> : Foo<A> = foreign java org.example.Buz {}
```
`E` gets the implicit upper bound `Fruit` because it is passed to `Foo`.
`A` fails because the upper bound `Object` is not compatible with the upper bound `Fruit` of `A` in `Foo<A>`.
