package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.xstream.LocalizedXStreams;


 
public class StepDefinitionContainer {
	   private final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<String, StepDefinition>();
	   private final Map<String, TreeMap> scopedStepDefinitionsByPattern = new TreeMap<String, TreeMap>();
	   
	   
	   
	   public void put (String regexPattern, StepDefinition step) {
		   
		   String featureScopeURIKey = step.getFeatureScopeURI();
		   //put in base step tree
		   if (featureScopeURIKey == null)	   
			   stepDefinitionsByPattern.put(regexPattern,step);
		   else {
			// put in scoped step tree   
			  TreeMap tm = scopedStepDefinitionsByPattern.get(featureScopeURIKey);
			  if (tm == null) scopedStepDefinitionsByPattern.put(featureScopeURIKey, new TreeMap<String,StepDefinition>());
			  ((TreeMap)scopedStepDefinitionsByPattern.get(featureScopeURIKey)).put(regexPattern, step); 
		   }
		  // System.out.println(step.getMethod().getDeclaringClass().getAnnotation(cucumber.annotation.en.FeatureScope.class));
		 
	   }
	   

	   
	   public StepDefinition getScoped(String scopedFeatureURIKey,String regexPattern) {
		   
		   StepDefinition step = null;
		   TreeMap scopedStepsMap =  null;
		   if (scopedFeatureURIKey!=null) {
			   scopedStepsMap = scopedStepDefinitionsByPattern.get(scopedFeatureURIKey);
			   if (scopedStepsMap != null)
				   step = (StepDefinition) scopedStepsMap.get(regexPattern);
		   } else
			   step = stepDefinitionsByPattern.get(regexPattern);
		   
		   return step;
		  
	   }
	   
	   public Collection<StepDefinition> scopedValues (String featureURI) {
		   
		   Collection<StepDefinition> values = null;
		   TreeMap scopedStepsMap =  scopedStepDefinitionsByPattern.get(featureURI);
		   if (scopedStepsMap != null)
			   values = ((TreeMap) scopedStepsMap).values();
		   else
			   values = stepDefinitionsByPattern.values();
		   
		   return values;
	   }
	   public Collection<StepDefinition> allValues () {
		   
		   Collection<StepDefinition> values = stepDefinitionsByPattern.values();
		   
		   scopedStepDefinitionsByPattern.keySet();
		   for (Iterator it = scopedStepDefinitionsByPattern.keySet().iterator(); it.hasNext();) {
               String key = (String)it.next();
               values.addAll(((TreeMap)scopedStepDefinitionsByPattern.get(key)).values());
               }

		   return values;
	   }   

	    /**
	     * @param uri
	     * @param step
	     * @param localizedXStreams
	     * @return
	     *  Returns a Glue Code step that matches the Feature step.  
	     *  Convention:  If scoped to a Feature URI, return that, otherwise see if its in the larger 'bucket' of unscoped steps.
	     *  This allows for reusable steps.  A reusable step gets there from classes that have no @FeatureScope annotation
	     */
	    public List<StepDefinitionMatch> matches(String uri, Step step, LocalizedXStreams  localizedXStreams ) {
	        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
	        for (StepDefinition stepDefinition : this.scopedValues(uri)) {
	            List<Argument> arguments = stepDefinition.matchedArguments(step);
	            if (arguments != null) {
	                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
	            }
	        }
	        if (result.size()==0){
		        for (StepDefinition stepDefinition : this.scopedValues("")) {
		            List<Argument> arguments = stepDefinition.matchedArguments(step);
		            if (arguments != null) {
		                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
		            }
		        }
	        } 
	        
	        return result;
	    }
	   
}
