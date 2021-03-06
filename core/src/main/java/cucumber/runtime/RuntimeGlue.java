package cucumber.runtime;

import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.autocomplete.MetaStepdef;
import cucumber.runtime.autocomplete.StepdefGenerator;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Collections.emptyList;

public class RuntimeGlue implements Glue {
    private static final List<Object> NO_FILTERS = emptyList();

    private final StepDefinitionContainer stepDefinitionContainer = new StepDefinitionContainer ();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();

    private final UndefinedStepsTracker tracker;
    private final LocalizedXStreams localizedXStreams;

    public RuntimeGlue(UndefinedStepsTracker tracker, LocalizedXStreams localizedXStreams) {
        this.tracker = tracker;
        this.localizedXStreams = localizedXStreams;
    }

    @Override
    public void addStepDefinition(StepDefinition stepDefinition) { 
        StepDefinition previous = stepDefinitionContainer.getScoped(stepDefinition.getFeatureScopeURI(), stepDefinition.getPattern()); 
        if (previous != null) {
            throw new DuplicateStepDefinitionException(previous, stepDefinition);
        }
        stepDefinitionContainer.put(stepDefinition.getPattern(), stepDefinition);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
        Collections.sort(beforeHooks, new HookComparator(true));
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        Collections.sort(afterHooks, new HookComparator(false));
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks; 
    }

    @Override
    public StepDefinitionMatch stepDefinitionMatch(String uri, Step step, I18n i18n) {
        List<StepDefinitionMatch> matches = stepDefinitionContainer.matches(uri, step, localizedXStreams);;
        try {
            if (matches.size() == 0) {
                tracker.addUndefinedStep(step, i18n);
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                throw new AmbiguousStepDefinitionsException(matches);
            }
        } finally {
            tracker.storeStepKeyword(step, i18n);
        }
    }


    @Override
    public void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException {
        if (dotCucumber != null) {
            List<CucumberFeature> features = load(new FileResourceLoader(), featurePaths, NO_FILTERS);
            List<MetaStepdef> metaStepdefs = new StepdefGenerator().generate(stepDefinitionContainer.allValues(), features);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(metaStepdefs);

            FileWriter stepdefsJson = new FileWriter(new File(dotCucumber, "stepdefs.json"));
            stepdefsJson.append(json);
            stepdefsJson.close();
        }
    }
}
