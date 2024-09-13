package nl.jqno.equalsverifier.internal.util;

import nl.jqno.equalsverifier.internal.instantiation.*;
import nl.jqno.equalsverifier.internal.prefabvalues.FactoryCache;
import nl.jqno.equalsverifier.internal.prefabvalues.JavaApiPrefabValues;
import nl.jqno.equalsverifier.internal.prefabvalues.PrefabValues;

public final class Context<T> {

    private final Class<T> type;
    private final Configuration<T> configuration;
    private final ClassProbe<T> classProbe;
    private final SubjectCreator<T> subjectCreator;
    private final InstanceCreator instanceCreator;

    public Context(Configuration<T> configuration, FactoryCache factoryCache) {
        this.type = configuration.getType();
        this.configuration = configuration;
        this.classProbe = new ClassProbe<>(configuration.getType());

        FactoryCache cache = JavaApiPrefabValues.build().merge(factoryCache);
        PrefabValues prefabValues = new PrefabValues(cache);

        this.instanceCreator = new VintageInstanceCreator(prefabValues);
        this.subjectCreator =
            new ModernSubjectCreator<>(configuration.getTypeTag(), configuration, instanceCreator);
    }

    public Class<T> getType() {
        return type;
    }

    public Configuration<T> getConfiguration() {
        return configuration;
    }

    public ClassProbe<T> getClassProbe() {
        return classProbe;
    }

    public InstanceCreator getInstanceCreator() {
        return instanceCreator;
    }

    public SubjectCreator<T> getSubjectCreator() {
        return subjectCreator;
    }
}
