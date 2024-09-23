package com.shenyong.flutter.psi

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.ui.icons.CachedImageIcon
import com.jetbrains.rd.util.AtomicInteger
import com.jetbrains.rd.util.ConcurrentHashMap
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.swing.Icon
import javax.swing.ImageIcon

object AssetUtility {
    val log = Logger.getInstance(this::class.java)

    private var thumbNailSize = AtomicInteger(16)

    private var cachedImageMap = ConcurrentHashMap<String, CachedImageIcon>()
    private var cachedIconMap = ConcurrentHashMap<String, Icon>()

    @JvmStatic
    fun setThumbNailSize(size: Int) {
        thumbNailSize.set(size)
    }

    private val supportedImageSuffix: Array<String>
        get() = arrayOf("png", "jpg", "jpeg", "webp", "bmp", "svg")


    @JvmStatic
    fun isImage(file: VirtualFile): Boolean {
        val supportedImageSuffix = supportedImageSuffix
        for (suffix in supportedImageSuffix) {
            if (file.path.endsWith(suffix)) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun isSvg(file: VirtualFile): Boolean {
        return file.path.endsWith("svg")
    }

    @JvmStatic
    fun loadSvg(file: VirtualFile, forceUpdate: Boolean = false): Icon {
        return ReadAction.compute<Icon, Exception> {
            try {
                if (!file.exists()) {
                    return@compute AllIcons.FileTypes.Image
                }

                // TODO: Invalidate cache when file is updated using AssetFileListener

                val cachedIcon = cachedIconMap[file.path]
                if (cachedIcon != null) {
                    return@compute cachedIcon
                }

                var cachedImage = cachedImageMap[file.path]
                if (cachedImage == null) {
                    cachedImage = CachedImageIcon(file.toNioPath().toUri().toURL(), null)
                    cachedImageMap[file.path] = cachedImage
                }

                val imageObserver = ImageObserver { img, infoflags, x, y, width, height -> true }

                val width =
                    cachedImage.getRealImage()?.getWidth(imageObserver)
                val height =
                    cachedImage.getRealImage()?.getHeight(imageObserver)

                if (width == null || height == null) {
                    return@compute AllIcons.FileTypes.Image
                }

                var realWidth = 16
                var realHeight = 16
                if (width > height) {
                    realHeight = height * realWidth / width
                    if (realHeight < 2) {
                        realHeight = 2
                    }
                } else {
                    realWidth = width * realHeight / height
                    if (realWidth < 2) {
                        realWidth = 2
                    }
                }

                val scaled =
                    cachedImage.getRealImage()?.getScaledInstance(realWidth, realHeight, BufferedImage.SCALE_SMOOTH);

                cachedIconMap[file.path] = ImageIcon(scaled)
                ImageIcon(scaled)
            } catch (e: Exception) {
                log.error(e)
                AllIcons.FileTypes.Image
            }
        }
    }

    @JvmStatic
    fun loadThumbnail(file: VirtualFile): Icon {
        return AllIcons.FileTypes.Image
    }

    @JvmStatic
    fun loadImage(file: VirtualFile): Icon {
        return ReadAction.compute<Icon, Exception> {
            val bufferedInputStream = file.inputStream.buffered()
            try {
                val icon = ImageIcon(bufferedInputStream.readAllBytes())
                icon
            } catch (e: Exception) {
                AllIcons.FileTypes.Image
            }
        }
    }


    @JvmStatic
    fun getAssetVirtualFile(psiElement: PsiElement): Array<VirtualFile?>? {
        val psiFiles = getAssetPsiFiles(psiElement)
        if (psiFiles.isEmpty()) {
            return null
        }
        val virtualFiles = arrayOfNulls<VirtualFile>(psiFiles.size)
        for (i in psiFiles.indices) {
            virtualFiles[i] = psiFiles[i].virtualFile
        }
        return virtualFiles
    }

    @JvmStatic
    fun getAssetPsiFiles(psiElement: PsiElement): Array<out PsiFile> {
        val text = psiElement.text.replace("[\"']".toRegex(), "")
        var fileName = text
        val slashIndex = text.lastIndexOf('/')
        if (slashIndex != -1) {
            fileName = text.substring(text.lastIndexOf('/') + 1)
        }
        val hasSuffix = fileName.lastIndexOf('.') != -1
        val project = psiElement.project
        return if (hasSuffix) {
            FilenameIndex.getFilesByName(
                project,
                fileName,
                ProjectScope.getProjectScope(project)
            )
        } else {
            getAssetFileWithoutSuffix(project, fileName)
        }
    }

    @JvmStatic
    fun getAssetFileWithoutSuffix(project: Project, nameWithoutSuffix: String): Array<out PsiFile> {
        for (suffix in supportedImageSuffix) {
            val files: Array<out PsiFile> = FilenameIndex.getFilesByName(
                project,
                "$nameWithoutSuffix.$suffix", ProjectScope.getProjectScope(project)
            )
            if (files.isNotEmpty()) {
                return files
            }
        }
        return arrayOf()
    }
}
