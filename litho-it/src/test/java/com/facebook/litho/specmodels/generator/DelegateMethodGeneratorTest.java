/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */


package com.facebook.litho.specmodels.generator;

import static com.facebook.litho.specmodels.generator.DelegateMethodGenerator.generateDelegates;
import static com.facebook.litho.specmodels.model.DelegateMethodDescriptions.LAYOUT_SPEC_DELEGATE_METHODS_MAP;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;
import com.facebook.litho.specmodels.model.ClassNames;
import com.facebook.litho.specmodels.model.DelegateMethod;
import com.facebook.litho.specmodels.model.DelegateMethodDescription;
import com.facebook.litho.specmodels.model.DelegateMethodDescriptions;
import com.facebook.litho.specmodels.model.DependencyInjectionHelper;
import com.facebook.litho.specmodels.model.MethodParamModelFactory;
import com.facebook.litho.specmodels.model.SpecMethodModel;
import com.facebook.litho.specmodels.model.SpecModel;
import com.facebook.litho.specmodels.model.SpecModelImpl;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.junit.Before;
import org.junit.Test;

/** Tests {@link DelegateMethodGenerator} */
public class DelegateMethodGeneratorTest {
  private static final String TEST_QUALIFIED_SPEC_NAME = "com.facebook.litho.TestSpec";

  private final DependencyInjectionHelper mDependencyInjectionHelper =
      mock(DependencyInjectionHelper.class);

  private SpecModel mSpecModelWithoutDI;
  private SpecModel mSpecModelWithDI;

  private SpecMethodModel<DelegateMethod, Void> mDelegateMethodModel;

  @Before
  public void setUp() {
    mDelegateMethodModel =
        new SpecMethodModel<>(
            ImmutableList.of(createAnnotation(OnCreateLayout.class)),
            ImmutableList.of(Modifier.PROTECTED),
            "onCreateLayout",
            DelegateMethodDescriptions.ON_CREATE_LAYOUT.returnType,
            ImmutableList.of(),
            ImmutableList.of(
                MethodParamModelFactory.create(
                    mock(ExecutableElement.class),
                    createTypeMirror(TypeKind.OTHER, ClassNames.COMPONENT_CONTEXT),
                    "c",
                    ImmutableList.<Annotation>of(),
                    new ArrayList<AnnotationSpec>(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    null),
                MethodParamModelFactory.create(
                    mock(ExecutableElement.class),
                    createBooleanTypeMirror(),
                    "prop",
                    ImmutableList.of(createAnnotation(Prop.class)),
                    new ArrayList<AnnotationSpec>(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    null),
                MethodParamModelFactory.create(
                    mock(ExecutableElement.class),
                    createIntTypeMirror(),
                    "state",
                    ImmutableList.of(createAnnotation(State.class)),
                    new ArrayList<AnnotationSpec>(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    null)),
            null,
            null);

    mSpecModelWithoutDI = SpecModelImpl.newBuilder()
        .qualifiedSpecClassName(TEST_QUALIFIED_SPEC_NAME)
        .componentClass(ClassNames.COMPONENT)
        .delegateMethods(ImmutableList.of(mDelegateMethodModel))
        .representedObject(new Object())
        .build();

    mSpecModelWithDI = SpecModelImpl.newBuilder()
        .qualifiedSpecClassName(TEST_QUALIFIED_SPEC_NAME)
        .componentClass(ClassNames.COMPONENT)
        .delegateMethods(ImmutableList.of(mDelegateMethodModel))
        .dependencyInjectionGenerator(mDependencyInjectionHelper)
        .representedObject(new Object())
        .build();
  }

  @Test
  public void testGenerateWithoutDependencyInjection() {
    TypeSpecDataHolder typeSpecDataHolder =
        generateDelegates(
            mSpecModelWithoutDI,
            LAYOUT_SPEC_DELEGATE_METHODS_MAP);

    assertThat(typeSpecDataHolder.getFieldSpecs()).isEmpty();
    assertThat(typeSpecDataHolder.getMethodSpecs()).hasSize(1);
    assertThat(typeSpecDataHolder.getTypeSpecs()).isEmpty();

    assertThat(typeSpecDataHolder.getMethodSpecs().get(0).toString()).isEqualTo(
        "@java.lang.Override\n" +
            "protected com.facebook.litho.ComponentLayout onCreateLayout(com.facebook.litho.ComponentContext c,\n" +
            "    com.facebook.litho.Component _abstract) {\n" +
            "  Test _ref = (Test) _abstract;\n" +
            "  com.facebook.litho.ComponentLayout _result = (com.facebook.litho.ComponentLayout) TestSpec.onCreateLayout(\n" +
            "    (com.facebook.litho.ComponentContext) c,\n" +
            "    (boolean) _ref.prop,\n" +
            "    (int) _ref.state);\n" +
            "  return _result;\n" +
            "}\n");
  }

  @Test
  public void testGenerateWithDependencyInjection() {
    TypeSpecDataHolder typeSpecDataHolder =
        generateDelegates(
            mSpecModelWithDI,
            LAYOUT_SPEC_DELEGATE_METHODS_MAP);

    assertThat(typeSpecDataHolder.getFieldSpecs()).isEmpty();
    assertThat(typeSpecDataHolder.getMethodSpecs()).hasSize(1);
    assertThat(typeSpecDataHolder.getTypeSpecs()).isEmpty();

    assertThat(typeSpecDataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "@java.lang.Override\n"
                + "protected com.facebook.litho.ComponentLayout onCreateLayout(com.facebook.litho.ComponentContext c,\n"
                + "    com.facebook.litho.Component _abstract) {\n"
                + "  Test _ref = (Test) _abstract;\n"
                + "  com.facebook.litho.ComponentLayout _result = (com.facebook.litho.ComponentLayout) mSpec.onCreateLayout(\n"
                + "    (com.facebook.litho.ComponentContext) c,\n"
                + "    (boolean) _ref.prop,\n"
                + "    (int) _ref.state);\n"
                + "  return _result;\n"
                + "}\n");
  }

  @Test
  public void testExtraOptionalParameterHasNoEffectIfSpecMethodDoesntUseIt() throws Exception {
    Map<Class<? extends Annotation>, DelegateMethodDescription> map =
        new TreeMap<>(
            new Comparator<Class<? extends Annotation>>() {
              @Override
              public int compare(Class<? extends Annotation> lhs, Class<? extends Annotation> rhs) {
                return lhs.toString().compareTo(rhs.toString());
              }
            });
    map.put(
        OnCreateLayout.class,
        DelegateMethodDescription.fromDelegateMethodDescription(
                LAYOUT_SPEC_DELEGATE_METHODS_MAP.get(OnCreateLayout.class))
            .optionalParameters(
                ImmutableList.of(
                    MethodParamModelFactory.createSimpleMethodParamModel(
                        TypeName.CHAR, "optionalParam", new Object())))
            .build());

    TypeSpecDataHolder typeSpecDataHolder = generateDelegates(mSpecModelWithDI, map);

    assertThat(typeSpecDataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "@java.lang.Override\n"
                + "protected com.facebook.litho.ComponentLayout onCreateLayout(com.facebook.litho.ComponentContext c,\n"
                + "    com.facebook.litho.Component _abstract) {\n"
                + "  Test _ref = (Test) _abstract;\n"
                + "  com.facebook.litho.ComponentLayout _result = (com.facebook.litho.ComponentLayout) mSpec.onCreateLayout(\n"
                + "    (com.facebook.litho.ComponentContext) c,\n"
                + "    (boolean) _ref.prop,\n"
                + "    (int) _ref.state);\n"
                + "  return _result;\n"
                + "}\n");
  }

  @Test
  public void testExtraOptionalParameterIncludedIfSpecMethodUsesIt() throws Exception {
    Map<Class<? extends Annotation>, DelegateMethodDescription> map =
        new TreeMap<>(
            new Comparator<Class<? extends Annotation>>() {
              @Override
              public int compare(Class<? extends Annotation> lhs, Class<? extends Annotation> rhs) {
                return lhs.toString().compareTo(rhs.toString());
              }
            });
    map.put(
        OnCreateLayout.class,
        DelegateMethodDescription.fromDelegateMethodDescription(
                LAYOUT_SPEC_DELEGATE_METHODS_MAP.get(OnCreateLayout.class))
            .optionalParameters(
                ImmutableList.of(
                    MethodParamModelFactory.createSimpleMethodParamModel(
                        TypeName.CHAR, "optionalParam", new Object())))
            .build());

    SpecMethodModel<DelegateMethod, Void> delegateMethodExpectingOptionalParameter =
        new SpecMethodModel<DelegateMethod, Void>(
            ImmutableList.of(createAnnotation(OnCreateLayout.class)),
            ImmutableList.of(Modifier.PROTECTED),
            "onCreateLayout",
            DelegateMethodDescriptions.ON_CREATE_LAYOUT.returnType,
            ImmutableList.of(),
            ImmutableList.of(
                MethodParamModelFactory.create(
                    mock(ExecutableElement.class),
                    createTypeMirror(TypeKind.OTHER, ClassNames.COMPONENT_CONTEXT),
                    "c",
                    ImmutableList.<Annotation>of(),
                    new ArrayList<AnnotationSpec>(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    null),
                MethodParamModelFactory.createSimpleMethodParamModel(
                    TypeName.CHAR, "unimportantName", new Object()),
                MethodParamModelFactory.create(
                    mock(ExecutableElement.class),
                    createBooleanTypeMirror(),
                    "prop",
                    ImmutableList.of(createAnnotation(Prop.class)),
                    new ArrayList<AnnotationSpec>(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    ImmutableList.<Class<? extends Annotation>>of(),
                    null)),
            null,
            null);

    SpecModel specModel =
        SpecModelImpl.newBuilder()
            .qualifiedSpecClassName(TEST_QUALIFIED_SPEC_NAME)
            .componentClass(ClassNames.COMPONENT)
            .delegateMethods(ImmutableList.of(delegateMethodExpectingOptionalParameter))
            .representedObject(new Object())
            .build();

    TypeSpecDataHolder typeSpecDataHolder = generateDelegates(specModel, map);

    assertThat(typeSpecDataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "@java.lang.Override\n"
                + "protected com.facebook.litho.ComponentLayout onCreateLayout(com.facebook.litho.ComponentContext c,\n"
                + "    com.facebook.litho.Component _abstract) {\n"
                + "  Test _ref = (Test) _abstract;\n"
                + "  com.facebook.litho.ComponentLayout _result = (com.facebook.litho.ComponentLayout) TestSpec.onCreateLayout(\n"
                + "    (com.facebook.litho.ComponentContext) c,\n"
                + "    (char) _ref.optionalParam,\n"
                + "    (boolean) _ref.prop);\n"
                + "  return _result;\n"
                + "}\n");
  }

  private static Annotation createAnnotation(final Class<? extends Annotation> annotationClass) {
    return new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return annotationClass;
      }
    };
  }

  public static TypeMirror createBooleanTypeMirror() {
    return createTypeMirror(TypeKind.BOOLEAN, TypeName.BOOLEAN);
  }

  public static TypeMirror createIntTypeMirror() {
    return createTypeMirror(TypeKind.INT, TypeName.INT);
  }

  public static TypeMirror createTypeMirror(TypeKind typeKind, TypeName typeName) {
    return new CustomTypeMirror(typeKind, typeName);
  }

  private static class CustomTypeMirror implements TypeMirror {

    private final TypeKind mTypeKind;
    private final TypeName mTypeName;

    CustomTypeMirror(TypeKind typeKind, TypeName typeName) {
      mTypeKind = typeKind;
      mTypeName = typeName;
    }

    @Override
    public TypeKind getKind() {
      return mTypeKind;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof CustomTypeMirror)) {
        return false;
      }

      CustomTypeMirror other = (CustomTypeMirror) obj;
      return mTypeKind.equals(other.mTypeKind) && mTypeName.equals(other.mTypeName);
    }

    @Override
    public int hashCode() {
      return mTypeName.hashCode();
    }

    @Override
    public String toString() {
      return mTypeName.toString();
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return (R) mTypeName;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return null;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return null;
    }
  }
}
