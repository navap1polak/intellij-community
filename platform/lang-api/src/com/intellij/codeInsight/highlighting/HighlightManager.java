// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.highlighting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;

/**
 * Allows to highlight areas of text and occurrences of PSI elements in the editor, and to remove the highlighting automatically
 * by some condition.
 *
 * @author max
 */
public abstract class HighlightManager {
  public static HighlightManager getInstance(Project project) {
    return ServiceManager.getService(project, HighlightManager.class);
  }

  /**
   * Specifies that a highlighter added with {@link #addOccurrenceHighlight} should be removed when the user presses Esc.
   */
  public static final int HIDE_BY_ESCAPE = 0x01;

  /**
   * Specifies that a highlighter added with {@link #addOccurrenceHighlight} should be removed when the user presses any key.
   */
  public static final int HIDE_BY_ANY_KEY = 0x02;

  /**
   * Specifies that a highlighter added with {@link #addOccurrenceHighlight} should be removed when the editor text is changed.
   */
  public static final int HIDE_BY_TEXT_CHANGE = 0x04;

  @MagicConstant(flags = {HIDE_BY_ESCAPE, HIDE_BY_ANY_KEY, HIDE_BY_TEXT_CHANGE})
  public @interface HideFlags {}

  /**
   * Highlights a specified range of text in an editor. The highlighting is removed when the user presses the Esc key, and optionally
   * when the editor text is changed. If the highlighter added by this method needs to be removed
   * manually, {@link #removeSegmentHighlighter} must be used for that.
   *
   * @param editor           the editor in which the highlighting is performed.
   * @param startOffset      the start offset of the text range to highlight.
   * @param endOffset        the end offset of the text range to highlight.
   * @param attributes       the attributes to highlight the text with.
   * @param hideByTextChange if true, the highlighting is removed automatically if the editor text is changed.
   * @param outHighlighters  if not null, the created {@link RangeHighlighter} object is added to this collection.
   */
  public abstract void addRangeHighlight(@NotNull Editor editor,
                                         int startOffset,
                                         int endOffset,
                                         @NotNull TextAttributes attributes,
                                         boolean hideByTextChange,
                                         @Nullable Collection<RangeHighlighter> outHighlighters);

  /**
   * Highlights a specified range of text in an editor. The highlighting is removed when the user presses the Esc key or (optionally)
   * any other key, and optionally when the editor text is changed. If the highlighter added by this method needs to be removed
   * manually, {@link #removeSegmentHighlighter} must be used for that.
   *
   * @param editor           the editor in which the highlighting is performed.
   * @param startOffset      the start offset of the text range to highlight.
   * @param endOffset        the end offset of the text range to highlight.
   * @param attributes       the attributes to highlight the text with.
   * @param hideByTextChange if true, the highlighting is removed automatically if the editor text is changed.
   * @param hideByAnyKey     if true, the highlighting is removed automatically when the user presses any key.
   * @param highlighters     if not null, the created {@link RangeHighlighter} object is added to this collection.
   */
  public abstract void addRangeHighlight(@NotNull Editor editor,
                                         int startOffset,
                                         int endOffset,
                                         @NotNull TextAttributes attributes,
                                         boolean hideByTextChange,
                                         boolean hideByAnyKey,
                                         @Nullable Collection<RangeHighlighter> highlighters);

  /**
   * Removes a range highlighter added by {@link #addRangeHighlight} or another method in this class.
   *
   * @param editor      the editor in which the highlighter should be removed.
   * @param highlighter the highlighter to remove.
   * @return true if the remove was successful, false if the highlighter was not found in the editor.
   */
  public abstract boolean removeSegmentHighlighter(@NotNull Editor editor, @NotNull RangeHighlighter highlighter);

  /**
   * Highlights the text ranges of the specified references in the specified editor. The highlighting is removed when the user presses
   * the Esc key, and optionally when the editor text is changed.
   *
   * @param editor           the editor in which the highlighting is performed.
   * @param occurrences      the references to highlight.
   * @param attributes       the attributes to highlight the text with.
   * @param hideByTextChange if true, the highlighting is removed automatically if the editor text is changed.
   * @param outHighlighters  if not null, the created {@link RangeHighlighter} objects are added to this collection.
   */
  public abstract void addOccurrenceHighlights(@NotNull Editor editor,
                                               @NotNull PsiReference[] occurrences,
                                               @NotNull TextAttributes attributes,
                                               boolean hideByTextChange,
                                               @Nullable Collection<RangeHighlighter> outHighlighters);

  /**
   * Highlights the text ranges of the specified elements in the specified editor. The highlighting is removed when the user presses
   * the Esc key, and optionally when the editor text is changed.
   *
   * @param editor           the editor in which the highlighting is performed.
   * @param elements         the elements to highlight.
   * @param attributes       the attributes to highlight the text with.
   * @param hideByTextChange if true, the highlighting is removed automatically if the editor text is changed.
   * @param outHighlighters  if not null, the created {@link RangeHighlighter} objects are added to this collection.
   */
  public abstract void addOccurrenceHighlights(@NotNull Editor editor,
                                               @NotNull PsiElement[] elements,
                                               @NotNull TextAttributes attributes,
                                               boolean hideByTextChange,
                                               @Nullable Collection<RangeHighlighter> outHighlighters);

  /**
   * Highlights a specified range of text in an editor and optionally adds a mark on the gutter. The highlighting is optionally removed
   * when the user presses the Esc key or any key, or when the editor text is changed. If the highlighter added by this method needs
   * to be removed manually, {@link #removeSegmentHighlighter} must be used for that.
   *
   * @param editor           the editor in which the highlighting is performed.
   * @param start            the start offset of the text range to highlight.
   * @param end              the end offset of the text range to highlight.
   * @param attributes       the attributes to highlight the text with.
   * @param flags            the flags specifying when the highlighting is removed (a combination of
   *                         {@link #HIDE_BY_ESCAPE}, {@link #HIDE_BY_ANY_KEY} and {@link #HIDE_BY_TEXT_CHANGE}).
   * @param outHighlighters  if not null, the created {@link RangeHighlighter} object is added to this collection.
   * @param scrollMarkColor  if not null, a gutter mark with the specified color is added in addition to the editor highlight.
   */
  public abstract void addOccurrenceHighlight(@NotNull Editor editor,
                                              int start,
                                              int end,
                                              TextAttributes attributes,
                                              @HideFlags int flags,
                                              @Nullable Collection<RangeHighlighter> outHighlighters,
                                              @Nullable Color scrollMarkColor);
}
