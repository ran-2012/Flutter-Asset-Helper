package com.shenyong.flutter.psi.dart;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.impl.DartReferenceExpressionImpl;
import com.shenyong.flutter.psi.AssetUtility;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DartAssetLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!isAssetElement(element)) {
            return;
        }
        String dartText = element.getText().replaceAll("[\"']", "");
        int index = dartText.lastIndexOf('/');
        String fileName = dartText;
        if (index != -1) {
            fileName = dartText.substring(index + 1);
        }
        boolean hasSuffix = fileName.lastIndexOf('.') != -1;

        if (element instanceof DartReferenceExpressionImpl && dartText.matches("^Res\\.\\w+$")) {
            fileName = dartText.replace("Res.", "");
            hasSuffix = false;
        }

        Project project = element.getProject();
        final List<PsiFile> psiFiles = new ArrayList<>();
        if (hasSuffix) {
            psiFiles.addAll(List.of(FilenameIndex.getFilesByName(project, fileName, ProjectScope.getProjectScope(project))));
        } else {
            psiFiles.addAll(List.of(AssetUtility.getAssetFileWithoutSuffix(project, fileName)));

        }
        if (psiFiles.isEmpty()) {
            return;
        }

        VirtualFile file = psiFiles.get(0).getVirtualFile();
        Icon icon;
        if(AssetUtility.isSvg(file)){
            icon = AssetUtility.loadSvg(file, false);
        }else{
            icon = AssetUtility.loadThumbnail(file);
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(icon)
                .setTargets(psiFiles)
                .setTooltipText("Navigate to " + dartText);
        result.add(builder.createLineMarkerInfo(element));
    }

    private boolean isAssetElement(PsiElement element) {
        String text = element.getText();
        if (element instanceof DartReferenceExpressionImpl && text.matches("^Res\\.\\w+$")) {
            // 支持 Res.xxx 显示 gutter icon
            // NOTE 2021/8/17: 这个特性支持，违反了运行时警告：Performance warning: LineMarker is supposed to be registered for leaf elements only
            return true;
        }
        // to fix runtime warning: Performance warning: LineMarker is supposed to be registered for leaf elements only
        return element instanceof LeafPsiElement
                && ((LeafPsiElement) element).getElementType() == DartTokenTypes.REGULAR_STRING_PART
                && text.matches(DartAssetReferenceContributor.ASSET_PATTERN);
    }
}
