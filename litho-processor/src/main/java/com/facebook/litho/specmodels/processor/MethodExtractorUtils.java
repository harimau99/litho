/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import com.facebook.litho.specmodels.model.MethodParamModel;
import com.facebook.litho.specmodels.model.MethodParamModelFactory;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

/**
 * Extracts methods from the given input.
 */
public class MethodExtractorUtils {
  private static final String COMPONENTS_PACKAGE = "com.facebook.litho";

  /** @return a list of params for a method. */
  static List<MethodParamModel> getMethodParams(
      ExecutableElement method,
      List<Class<? extends Annotation>> permittedAnnotations,
      List<Class<? extends Annotation>> permittedInterStageInputAnnotations,
      List<Class<? extends Annotation>> delegateMethodAnnotationsThatSkipDiffModels) {

    final List<MethodParamModel> methodParamModels = new ArrayList<>();
    final List<Name> savedParameterNames = getSavedParameterNames(method);
    final List<? extends VariableElement> params = method.getParameters();

    for (int i = 0, size = params.size(); i < size; i++) {
      final VariableElement param = params.get(i);
      final String paramName =
          savedParameterNames == null
              ? param.getSimpleName().toString()
              : savedParameterNames.get(i).toString();

      try {
        methodParamModels.add(
            MethodParamModelFactory.create(
                method,
                param.asType(),
                paramName,
                getLibraryAnnotations(param, permittedAnnotations),
                getExternalAnnotations(param),
                permittedInterStageInputAnnotations,
                delegateMethodAnnotationsThatSkipDiffModels,
                param));
      } catch (Exception e) {
        throw new ComponentsProcessingException(
            param,
            "Error processing this param. Are your imports set up correctly?");
      }
    }

    return methodParamModels;
  }

  /**
   * Attempt to recover saved parameter names for a method. This will likely only work for code
   * compiled with javac >= 8, but it's often the only chance to get named parameters as opposed to
   * 'arg0', 'arg1', ...
   */
  @Nullable
  private static List<Name> getSavedParameterNames(ExecutableElement method) {
    if (method instanceof Symbol.MethodSymbol) {
      final Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) method;
      return methodSymbol.savedParameterNames;
    }
    return null;
  }

  private static List<Annotation> getLibraryAnnotations(
      VariableElement param,
      List<Class<? extends Annotation>> permittedAnnotations) {
    List<Annotation> paramAnnotations = new ArrayList<>();
    for (Class<? extends Annotation> possibleMethodParamAnnotation : permittedAnnotations) {
      final Annotation paramAnnotation = param.getAnnotation(possibleMethodParamAnnotation);
      if (paramAnnotation != null) {
        paramAnnotations.add(paramAnnotation);
      }
    }

    return paramAnnotations;
  }

  private static List<AnnotationSpec> getExternalAnnotations(VariableElement param) {
    final List<? extends AnnotationMirror> annotationMirrors = param.getAnnotationMirrors();
    final List<AnnotationSpec> annotations = new ArrayList<>();

    for (AnnotationMirror annotationMirror : annotationMirrors) {
      if (annotationMirror.getAnnotationType().toString().startsWith(COMPONENTS_PACKAGE)) {
        continue;
      }

      final AnnotationSpec.Builder annotationSpec =
          AnnotationSpec.builder(
              ClassName.bestGuess(annotationMirror.getAnnotationType().toString()));

      Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
          annotationMirror.getElementValues();
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementValue :
          elementValues.entrySet()) {
        annotationSpec.addMember(
            elementValue.getKey().getSimpleName().toString(),
            elementValue.getValue().toString());
      }

      annotations.add(annotationSpec.build());
    }

    return annotations;
  }

  static List<TypeVariableName> getTypeVariables(ExecutableElement method) {
    final List<TypeVariableName> typeVariables = new ArrayList<>();
    for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
      typeVariables.add(
          TypeVariableName.get(
              typeParameterElement.getSimpleName().toString(),
              getBounds(typeParameterElement)));
    }

    return typeVariables;
  }

  private static TypeName[] getBounds(TypeParameterElement typeParameterElement) {
    final TypeName[] bounds = new TypeName[typeParameterElement.getBounds().size()];
    for (int i = 0, size = typeParameterElement.getBounds().size(); i < size; i++) {
      bounds[i] = TypeName.get(typeParameterElement.getBounds().get(i));
    }

    return bounds;
  }
}
