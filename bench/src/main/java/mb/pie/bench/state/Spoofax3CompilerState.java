package mb.pie.bench.state;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.esv.DaggerEsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.log.noop.NoopLoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceKeyString;
import mb.sdf3.DaggerSdf3Component;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofax3.dagger.DaggerSpoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerModule;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.statix.DaggerStatixComponent;
import mb.str.DaggerStrategoComponent;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.nio.charset.StandardCharsets;

@State(Scope.Thread)
public class Spoofax3CompilerState {
    public PlatformComponent platformComponent;
    public Spoofax3CompilerComponent spoofax3CompilerComponent;
    public Spoofax3LanguageProjectCompilerInputBuilder inputBuilder;

    public Spoofax3CompilerState setup() {
        platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(new NoopLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();
        spoofax3CompilerComponent = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(new TemplateCompiler(StandardCharsets.UTF_8)))
            .platformComponent(platformComponent)
            .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
            .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
            .esvComponent(DaggerEsvComponent.builder().platformComponent(platformComponent).build())
            .statixComponent(DaggerStatixComponent.builder().platformComponent(platformComponent).build())
            .libSpoofax2Component(DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build())
            .libStatixComponent(DaggerLibStatixComponent.builder().platformComponent(platformComponent).build())
            .build();
        inputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
        return this;
    }

    public Pie getPie() {
        return spoofax3CompilerComponent.getPie();
    }

    public Spoofax3LanguageProjectCompiler.Input buildInput() {
        final Shared shared = Shared.builder().name(name).build();
        final LanguageProject languageProject = LanguageProject.builder().withDefaults(spoofax3CompilerComponent.getResourceService().getResourcePath(ResourceKeyString.parse(languageProjectBaseDirectory)), shared).build();
        final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
        return inputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
    }

    public Task<Result<KeyedMessages, CompilerException>> buildTask() {
        return spoofax3CompilerComponent.getSpoofax3LanguageProjectCompiler().createTask(buildInput());
    }

    @Param("Language") public String name;
    @Param(".") public String languageProjectBaseDirectory;
}
