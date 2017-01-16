package husacct.analyse.task.analyser.java;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import husacct.analyse.infrastructure.antlr.java.Java7Parser;


import husacct.analyse.infrastructure.antlr.java.Java7Parser.ModifierContext;
import husacct.analyse.infrastructure.antlr.java.Java7Parser.TypeArgumentContext;
import husacct.analyse.infrastructure.antlr.java.Java7Parser.TypeArgumentsContext;
import husacct.analyse.domain.IModelCreationService;
import husacct.analyse.domain.famix.FamixCreationServiceImpl;
import husacct.analyse.task.analyser.VisibilitySet;
import husacct.common.enums.DependencySubTypes;

abstract class JavaGenerator {

    protected IModelCreationService modelService = new FamixCreationServiceImpl();
    
    protected String determineVisibility(List<ModifierContext> modifierList) {
    	String visibility = VisibilitySet.DEFAULT.toString();
        if (modifierList != null) {
			for (ModifierContext modifier : modifierList) {
	            if (VisibilitySet.isValidVisibillity(modifier.getText())) {
	            	visibility = modifier.getText();
	            }
			}
 		}
        return visibility;
    }

    protected boolean determineIsAbstract(List<ModifierContext> modifierList) {
    	boolean isAbstract = false;
        if (modifierList != null) {
			for (ModifierContext modifier : modifierList) {
	            if (modifier.getText().equals("abstract")) {
	            	isAbstract = true;
	            } 
			}
 		}
        return isAbstract;
    }

    protected boolean determineIsFinal(List<ModifierContext> modifierList) {
    	boolean isFinal = false;
        if (modifierList != null) {
			for (ModifierContext modifier : modifierList) {
	            if (modifier.getText().equals("final")) {
	            	isFinal = true;
	            } 
			}
 		}
        return isFinal;
    }

    protected boolean determineIsStatic(List<Java7Parser.ModifierContext> modifierList) {
    	boolean isStatic = false;
        if (modifierList != null) {
			for (ModifierContext modifier : modifierList) {
	            if (modifier.getText().equals("static")) {
	            	isStatic = true;
	            } 
			}
 		}
        return isStatic;
    }

    protected void dispatchAnnotationsOfMember(List<Java7Parser.ModifierContext> modifierList, String belongsToClass) {
        if (modifierList != null) {
        	int size = modifierList.size();
        	for (int i = 0; i < size; i++) {
            	if (modifierList.get(i).classOrInterfaceModifier().annotation() != null) {
    				new AnnotationAnalyser(modifierList.get(i).classOrInterfaceModifier().annotation(), belongsToClass);
            	}
			}
 		}
    }
    
    /** Transforms the output of a list of Identifiers to a String. E.g. needed to transform TypeType.Identifier().
     * 
     * @param List<TerminalNode> identifierList
     * @return String
     */
    protected String transformIdentifierToString(List<TerminalNode> identifierList) {
    	String returnValue = "";
    	int sequence = 1;
    	for(TerminalNode identifier : identifierList){
    		if (sequence == 1) {
    			returnValue += identifier.getText();
    		} else {
    			returnValue += "." + identifier.getText();
    		}
    		sequence ++;
    	}
    	return returnValue;
    }
    
    // Detects generic type parameters recursively. Also in complex types such as: HashMap<ProfileDAO, ArrayList<FriendsDAO>>>
    protected String dispatchGenericTypeParameters(String belongsToClass, List<TypeArgumentsContext> typeArgumentsList, int recursionLevel, DependencySubTypes dependencySubType) {
    	String typeInClassDiagram = "";
    	int levelOfRecursion = recursionLevel + 1;
		for (TypeArgumentsContext typeArguments : typeArgumentsList) {
			for (TypeArgumentContext typeArgument : typeArguments.typeArgument()) {
				String parameterTypeOfGeneric = transformIdentifierToString(typeArgument.typeType().classOrInterfaceType().Identifier());
            	int currentLineNumber = typeArgument.typeType().classOrInterfaceType().start.getLine();
            	modelService.createTypeParameter(belongsToClass, currentLineNumber, parameterTypeOfGeneric, dependencySubType);
				// Check if typeArgument contains type parameters too (recursively)
				if ((typeArgument.typeType().classOrInterfaceType() != null) 
					&& (typeArgument.typeType().classOrInterfaceType().typeArguments() != null)) {
					dispatchGenericTypeParameters(belongsToClass, typeArgument.typeType().classOrInterfaceType().typeArguments(), levelOfRecursion, dependencySubType);
				}
            	// If the variable is an instance variable, return this.typeInClassDiagram
                if ((dependencySubType == DependencySubTypes.DECL_INSTANCE_VAR) && (levelOfRecursion == 1)) { // E.g. ArrayList<Person>. In case of HashMap<String, Person> Person will be assigned.
            		typeInClassDiagram = parameterTypeOfGeneric; 
                }
			}
		}
		return typeInClassDiagram;
    }

}
