package mb.pie.lang.test.returnTypes.__error__listOfTupleWithNullable;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface __error__listOfTupleWithNullableComponent extends PieComponent { }
