package nl.jqno.equalsverifier.internal.checkers;

import java.util.function.Predicate;
import nl.jqno.equalsverifier.Warning;
import nl.jqno.equalsverifier.internal.checkers.fieldchecks.*;
import nl.jqno.equalsverifier.internal.instantiation.SubjectCreator;
import nl.jqno.equalsverifier.internal.prefabvalues.PrefabValues;
import nl.jqno.equalsverifier.internal.prefabvalues.TypeTag;
import nl.jqno.equalsverifier.internal.reflection.ClassAccessor;
import nl.jqno.equalsverifier.internal.reflection.FieldAccessor;
import nl.jqno.equalsverifier.internal.reflection.annotations.AnnotationCache;
import nl.jqno.equalsverifier.internal.reflection.annotations.SupportedAnnotations;
import nl.jqno.equalsverifier.internal.util.Configuration;

public class FieldsChecker<T> implements Checker {

    private final Configuration<T> config;
    private final ArrayFieldCheck<T> arrayFieldCheck;
    private final FloatAndDoubleFieldCheck<T> floatAndDoubleFieldCheck;
    private final MutableStateFieldCheck<T> mutableStateFieldCheck;
    private final ReflexivityFieldCheck<T> reflexivityFieldCheck;
    private final SignificantFieldCheck<T> significantFieldCheck;
    private final SignificantFieldCheck<T> skippingSignificantFieldCheck;
    private final SymmetryFieldCheck<T> symmetryFieldCheck;
    private final TransientFieldsCheck<T> transientFieldsCheck;
    private final TransitivityFieldCheck<T> transitivityFieldCheck;
    private final StringFieldCheck<T> stringFieldCheck;
    private final BigDecimalFieldCheck<T> bigDecimalFieldCheck;
    private final JpaLazyGetterFieldCheck<T> jpaLazyGetterFieldCheck;

    public FieldsChecker(Configuration<T> config) {
        this.config = config;

        final PrefabValues prefabValues = config.getPrefabValues();
        final TypeTag typeTag = config.getTypeTag();
        final SubjectCreator<T> subjectCreator = new SubjectCreator<>(typeTag, prefabValues);

        final String cachedHashCodeFieldName = config
            .getCachedHashCodeInitializer()
            .getCachedHashCodeFieldName();
        final Predicate<FieldAccessor> isCachedHashCodeField = a ->
            a.getFieldName().equals(cachedHashCodeFieldName);

        this.arrayFieldCheck =
            new ArrayFieldCheck<>(subjectCreator, config.getCachedHashCodeInitializer());
        this.floatAndDoubleFieldCheck = new FloatAndDoubleFieldCheck<>(subjectCreator);
        this.mutableStateFieldCheck =
            new MutableStateFieldCheck<>(subjectCreator, isCachedHashCodeField);
        this.reflexivityFieldCheck = new ReflexivityFieldCheck<>(subjectCreator, config);
        this.significantFieldCheck =
            new SignificantFieldCheck<>(config, isCachedHashCodeField, false);
        this.skippingSignificantFieldCheck =
            new SignificantFieldCheck<>(config, isCachedHashCodeField, true);
        this.symmetryFieldCheck = new SymmetryFieldCheck<>(subjectCreator);
        this.transientFieldsCheck =
            new TransientFieldsCheck<>(subjectCreator, typeTag, config.getAnnotationCache());
        this.transitivityFieldCheck = new TransitivityFieldCheck<>(subjectCreator);
        this.stringFieldCheck =
            new StringFieldCheck<>(
                subjectCreator,
                config.getPrefabValues(),
                config.getCachedHashCodeInitializer()
            );
        this.bigDecimalFieldCheck =
            new BigDecimalFieldCheck<>(subjectCreator, config.getCachedHashCodeInitializer());
        this.jpaLazyGetterFieldCheck = new JpaLazyGetterFieldCheck<>(config);
    }

    @Override
    public void check() {
        ClassAccessor<T> classAccessor = config.getClassAccessor();
        FieldInspector<T> inspector = new FieldInspector<>(classAccessor, config.getTypeTag());

        if (!classAccessor.isEqualsInheritedFromObject()) {
            inspector.check(arrayFieldCheck);
            inspector.check(floatAndDoubleFieldCheck);
            inspector.check(reflexivityFieldCheck);
        }

        if (!ignoreMutability(config.getType())) {
            inspector.check(mutableStateFieldCheck);
        }

        if (!config.getWarningsToSuppress().contains(Warning.TRANSIENT_FIELDS)) {
            inspector.check(transientFieldsCheck);
        }

        inspector.check(significantFieldCheck);
        inspector.check(symmetryFieldCheck);
        inspector.check(transitivityFieldCheck);
        inspector.check(stringFieldCheck);

        inspector.checkWithNull(
            config.getWarningsToSuppress().contains(Warning.NULL_FIELDS),
            config.getWarningsToSuppress().contains(Warning.ZERO_FIELDS),
            config.getNonnullFields(),
            config.getAnnotationCache(),
            skippingSignificantFieldCheck
        );

        if (!config.getWarningsToSuppress().contains(Warning.BIGDECIMAL_EQUALITY)) {
            inspector.check(bigDecimalFieldCheck);
        }

        AnnotationCache cache = config.getAnnotationCache();
        if (
            cache.hasClassAnnotation(config.getType(), SupportedAnnotations.ENTITY) &&
            !config.getWarningsToSuppress().contains(Warning.JPA_GETTER)
        ) {
            inspector.check(jpaLazyGetterFieldCheck);
        }
    }

    private boolean ignoreMutability(Class<?> type) {
        AnnotationCache cache = config.getAnnotationCache();
        return (
            config.getWarningsToSuppress().contains(Warning.NONFINAL_FIELDS) ||
            cache.hasClassAnnotation(type, SupportedAnnotations.IMMUTABLE) ||
            cache.hasClassAnnotation(type, SupportedAnnotations.ENTITY)
        );
    }
}
