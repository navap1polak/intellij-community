package org.jetbrains.plugins.groovy.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.GrModifierList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrVariableDeclarationOwner;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyBaseElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.JavaIdentifier;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

/**
 * @author ilyas
 */
public abstract class GrVariableBaseImpl<T extends StubElement> extends GroovyBaseElementImpl<T> implements GrVariable {

  public static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.lang.psi.impl.statements.GrVariableImpl");

  public GrVariableBaseImpl(ASTNode node) {
    super(node);
  }

  protected GrVariableBaseImpl(final T stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Nullable
  public PsiTypeElement getTypeElement() {
    return null;
  }

  @Nullable
  public PsiExpression getInitializer() {
    return null;
  }

  public boolean hasInitializer() {
    return false;
  }

  public void normalizeDeclaration() throws IncorrectOperationException {
  }

  @Nullable
  public Object computeConstantValue() {
    return null;
  }

  //todo: see GrModifierListImpl.hasModifierProperty()
  public boolean hasModifierProperty(@NonNls @NotNull String property) {
    PsiModifierList modifierList = getModifierList();
    return modifierList != null && modifierList.hasModifierProperty(property);
  }

  @Nullable
  public PsiDocComment getDocComment() {
    return null;
  }

  public String getElementToCompare() {
    return getName();
  }

  @NotNull
  public PsiType getType() {
    PsiType type = getDeclaredType();
    return type != null ? type : TypesUtil.getJavaLangObject(this);
  }

  @Nullable
  public GrTypeElement getTypeElementGroovy() {
    return ((GrVariableDeclaration) getParent()).getTypeElementGroovy();
  }

  @Nullable
  public PsiType getDeclaredType() {
    GrTypeElement typeElement = getTypeElementGroovy();
    if (typeElement != null) return typeElement.getType();

    return null;
  }

  @Nullable
  public PsiType getTypeGroovy() {
    GrTypeElement typeElement = getTypeElementGroovy();
    PsiType declaredType = null;
    if (typeElement != null) {
      declaredType = typeElement.getType();
      if (!(declaredType instanceof PsiClassType)) {
        return declaredType;
      }
    }

    GrExpression initializer = getInitializerGroovy();
    if (initializer != null) {
      PsiType initializerType = initializer.getType();
      if (initializerType != null) {
        if (declaredType != null && initializerType instanceof PsiClassType) {
          final PsiClass declaredClass = ((PsiClassType) declaredType).resolve();
          if (declaredClass != null) {
            final PsiClassType.ClassResolveResult initializerResult = ((PsiClassType) initializerType).resolveGenerics();
            final PsiClass initializerClass = initializerResult.getElement();
            if (initializerClass != null &&
                    !com.intellij.psi.util.PsiUtil.isRawSubstitutor(initializerClass, initializerResult.getSubstitutor())) {
              if (declaredClass == initializerClass) return initializerType;
              final PsiSubstitutor superSubstitutor = TypeConversionUtil.getClassSubstitutor(declaredClass, initializerClass, initializerResult.getSubstitutor());
              if (superSubstitutor != null) {
                return JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(declaredClass, superSubstitutor);
              }
            }
          }
        }
      }

      if (declaredType == null) declaredType = initializerType;
    }


    return declaredType;
  }

  public void setType(@Nullable PsiType type) {
    final GrTypeElement typeElement = getTypeElementGroovy();
    if (type == null) {
      if (typeElement == null) return;
      final ASTNode typeElementNode = typeElement.getNode();
      final ASTNode parent = typeElementNode.getTreeParent();
      parent.addLeaf(GroovyTokenTypes.kDEF, "def", typeElementNode);
      parent.removeChild(typeElementNode);
    } else {
      type = TypesUtil.unboxPrimitiveTypeWrapper(type);
      GrTypeElement newTypeElement;
      try {
        newTypeElement = GroovyPsiElementFactory.getInstance(getProject()).createTypeElement(type);
      } catch (IncorrectOperationException e) {
        LOG.error(e);
        return;
      }

      final ASTNode newTypeElementNode = newTypeElement.getNode();
      if (typeElement == null) {
        final PsiElement defKeyword = findChildByType(GroovyTokenTypes.kDEF);
        if (defKeyword != null) {
          final ASTNode defKeywordNode = defKeyword.getNode();
          assert defKeywordNode != null;
          defKeywordNode.getTreeParent().removeChild(defKeywordNode);
        }
        final PsiElement nameID = getNameIdentifierGroovy();
        final ASTNode nameIdNode = nameID.getNode();
        assert nameIdNode != null;
        nameIdNode.getTreeParent().addChild(newTypeElementNode);
      } else {
        final ASTNode typeElementNode = typeElement.getNode();
        final ASTNode parent = typeElementNode.getTreeParent();
        parent.replaceChild(typeElementNode, newTypeElementNode);
      }

      PsiUtil.shortenReferences(newTypeElement);
    }
  }

  @NotNull
  public PsiElement getNameIdentifierGroovy() {
    PsiElement ident = findChildByType(TokenSets.PROPERTY_NAMES);
    assert ident != null;
    return ident;
  }

  @Nullable
  public GrExpression getInitializerGroovy() {
    return findChildByClass(GrExpression.class);
  }

  public int getTextOffset() {
    return getNameIdentifierGroovy().getTextRange().getStartOffset();
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiImplUtil.setName(name, getNameIdentifierGroovy());
    return this;
  }

  @NotNull
  public SearchScope getUseScope() {
    final GrVariableDeclarationOwner owner = PsiTreeUtil.getParentOfType(this, GrVariableDeclarationOwner.class);
    if (owner != null) return new LocalSearchScope(owner);
    return super.getUseScope();
  }

  public String getName() {
    return PsiImplUtil.getName(this);
  }

  @NotNull
  public PsiIdentifier getNameIdentifier() {
    PsiElement nameId = getNameIdentifierGroovy();
    return new JavaIdentifier(getManager(), getContainingFile(), nameId.getTextRange());
  }

  @Nullable
  public GrModifierList getModifierList() {
    PsiElement parent = getParent();
    if (parent instanceof GrVariableDeclaration) {
      return ((GrVariableDeclaration) parent).getModifierList();
    }
    return null;
  }


}
