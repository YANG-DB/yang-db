package com.kayhut.fuse.asg.validation;

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class AsgQueryValidator implements QueryValidator<AsgQuery> {
    //region Constructors
    @Inject
    public AsgQueryValidator(
            AsgValidatorStrategyRegistrar asgValidatorStrategyRegistrar,
            OntologyProvider ontologyProvider) {
        this.asgValidatorStrategies = asgValidatorStrategyRegistrar.register();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryValidator Implementation
    @Override
    public ValidationResult validate(AsgQuery query) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return new ValidationResult(false, "unknown ontology");
        }

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(new Ontology.Accessor(ontology.get()));

        List<ValidationResult> validationResults = Stream.ofAll(this.asgValidatorStrategies)
                .map(strategy -> strategy.apply(query, asgStrategyContext))
                .toJavaList();

        List<String> errors = Stream.ofAll(validationResults)
                .filter(queryValidation -> !queryValidation.valid())
                .flatMap(queryValidation -> Stream.ofAll(queryValidation.errors()))
                .toJavaList();

        return errors.isEmpty() ?
                ValidationResult.OK :
                new ValidationResult(false, errors);
    }
    //endregion

    //region Fields
    private Iterable<AsgValidatorStrategy> asgValidatorStrategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
