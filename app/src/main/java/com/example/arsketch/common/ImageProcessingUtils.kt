package com.example.arsketch.common

import android.graphics.Bitmap
import android.graphics.Color
import java.util.ArrayDeque
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageProcessingUtils {

    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).roundToInt().coerceIn(0, 255)
            pixels[i] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    fun gaussianBlur(bitmap: Bitmap, radius: Int = 2): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = IntArray(width * height) { (pixels[it] shr 16) and 0xFF }

        val size = 2 * radius + 1
        // Match OpenCV sigma=0 behavior: sigma = 0.3*((ksize-1)*0.5 - 1) + 0.8
        val sigma = 0.3f * ((size - 1) * 0.5f - 1f) + 0.8f
        val kernel = generateGaussianKernel(size, sigma)

        val result = convolve(gray, width, height, kernel, size)

        val output = IntArray(width * height)
        for (i in output.indices) {
            val v = result[i].coerceIn(0, 255)
            output[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    fun cannyEdgeDetection(bitmap: Bitmap, lowThreshold: Int = 50, highThreshold: Int = 150): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(width * height) { (pixels[it] shr 16) and 0xFF }

        // Sobel gradients
        val sobelX = intArrayOf(-1, 0, 1, -2, 0, 2, -1, 0, 1)
        val sobelY = intArrayOf(-1, -2, -1, 0, 0, 0, 1, 2, 1)

        val magnitude = FloatArray(width * height)
        val direction = FloatArray(width * height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var sumX = 0
                var sumY = 0
                var ki = 0
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = gray[(y + ky) * width + (x + kx)]
                        sumX += pixel * sobelX[ki]
                        sumY += pixel * sobelY[ki]
                        ki++
                    }
                }
                val idx = y * width + x
                magnitude[idx] = hypot(sumX.toFloat(), sumY.toFloat())
                direction[idx] = atan2(sumY.toFloat(), sumX.toFloat())
            }
        }

        // Non-maximum suppression
        val suppressed = FloatArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val angle = ((direction[idx] * 180f / Math.PI.toFloat()) + 180f) % 180f
                val mag = magnitude[idx]

                val (n1, n2) = when {
                    angle < 22.5f || angle >= 157.5f -> {
                        magnitude[y * width + (x - 1)] to magnitude[y * width + (x + 1)]
                    }
                    angle < 67.5f -> {
                        magnitude[(y - 1) * width + (x + 1)] to magnitude[(y + 1) * width + (x - 1)]
                    }
                    angle < 112.5f -> {
                        magnitude[(y - 1) * width + x] to magnitude[(y + 1) * width + x]
                    }
                    else -> {
                        magnitude[(y - 1) * width + (x - 1)] to magnitude[(y + 1) * width + (x + 1)]
                    }
                }

                suppressed[idx] = if (mag >= n1 && mag >= n2) mag else 0f
            }
        }

        // Hysteresis thresholding with BFS (matches OpenCV behavior)
        val edge = IntArray(width * height) // 0=none, 1=weak, 2=strong
        val queue = ArrayDeque<Int>()

        for (i in suppressed.indices) {
            when {
                suppressed[i] >= highThreshold -> {
                    edge[i] = 2
                    queue.add(i)
                }
                suppressed[i] >= lowThreshold -> edge[i] = 1
            }
        }

        // BFS: propagate strong status to connected weak edges
        while (queue.isNotEmpty()) {
            val idx = queue.poll()
            val x = idx % width
            val y = idx / width
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dy == 0 && dx == 0) continue
                    val ny = y + dy
                    val nx = x + dx
                    if (ny in 0 until height && nx in 0 until width) {
                        val nIdx = ny * width + nx
                        if (edge[nIdx] == 1) {
                            edge[nIdx] = 2
                            queue.add(nIdx)
                        }
                    }
                }
            }
        }

        val output = IntArray(width * height)
        for (i in output.indices) {
            val v = if (edge[i] == 2) 255 else 0
            output[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    fun adaptiveThreshold(bitmap: Bitmap, blockSize: Int = 15, c: Int = 10): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(width * height) { (pixels[it] shr 16) and 0xFF }

        // Compute integral image with 1-based indexing (padded)
        // integral[y+1][x+1] = sum of all pixels from (0,0) to (y,x)
        val integral = LongArray((width + 1) * (height + 1))
        val iw = width + 1
        for (y in 0 until height) {
            var rowSum = 0L
            for (x in 0 until width) {
                rowSum += gray[y * width + x]
                integral[(y + 1) * iw + (x + 1)] = rowSum + integral[y * iw + (x + 1)]
            }
        }

        val halfBlock = blockSize / 2
        val output = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Window boundaries (inclusive in image coords)
                val y1 = max(0, y - halfBlock)
                val x1 = max(0, x - halfBlock)
                val y2 = min(height - 1, y + halfBlock)
                val x2 = min(width - 1, x + halfBlock)

                val count = (y2 - y1 + 1) * (x2 - x1 + 1)
                // Use 1-based integral image: sum = I[y2+1][x2+1] - I[y1][x2+1] - I[y2+1][x1] + I[y1][x1]
                val sum = integral[(y2 + 1) * iw + (x2 + 1)] -
                        integral[y1 * iw + (x2 + 1)] -
                        integral[(y2 + 1) * iw + x1] +
                        integral[y1 * iw + x1]

                val mean = sum / count
                val idx = y * width + x
                val v = if (gray[idx] > mean - c) 255 else 0
                output[idx] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    fun erode(bitmap: Bitmap, kernelSize: Int): Bitmap {
        if (kernelSize <= 1) return bitmap.copy(Bitmap.Config.ARGB_8888, false)

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(width * height) { (pixels[it] shr 16) and 0xFF }

        val half = kernelSize / 2
        val output = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var minVal = 255
                for (ky in -half..half) {
                    for (kx in -half..half) {
                        val ny = (y + ky).coerceIn(0, height - 1)
                        val nx = (x + kx).coerceIn(0, width - 1)
                        minVal = min(minVal, gray[ny * width + nx])
                    }
                }
                output[y * width + x] = (0xFF shl 24) or (minVal shl 16) or (minVal shl 8) or minVal
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    fun convertToSketchHollow(inputBitmap: Bitmap): Bitmap {
        val grayscale = toGrayscale(inputBitmap)
        val blurred = gaussianBlur(grayscale, 2)
        val edges = cannyEdgeDetection(blurred, 50, 150)

        // Invert
        val width = edges.width
        val height = edges.height
        val pixels = IntArray(width * height)
        edges.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val v = 255 - ((pixels[i] shr 16) and 0xFF)
            pixels[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    fun convertToSketchSolid(inputBitmap: Bitmap): Bitmap {
        val grayscale = toGrayscale(inputBitmap)
        val blurred = gaussianBlur(grayscale, 2)
        return adaptiveThreshold(blurred, 15, 10)
    }

    fun adjustLineThickness(inputBitmap: Bitmap, size: Int): Bitmap {
        val grayscale = toGrayscale(inputBitmap)
        val blurred = gaussianBlur(grayscale, 2)
        val thresholded = adaptiveThreshold(blurred, 15, 10)
        val eroded = erode(thresholded, size)
        return replaceColor(eroded)
    }

    private fun replaceColor(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            pixels[i] = (pixels[i] shl 8 and -0x1000000).inv() and Color.BLACK
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun generateGaussianKernel(size: Int, sigma: Float): FloatArray {
        val kernel = FloatArray(size * size)
        val center = size / 2
        var sum = 0f
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val value = Math.exp(-(dx * dx + dy * dy).toDouble() / (2.0 * sigma * sigma)).toFloat()
                kernel[y * size + x] = value
                sum += value
            }
        }
        for (i in kernel.indices) kernel[i] /= sum
        return kernel
    }

    private fun convolve(input: IntArray, width: Int, height: Int, kernel: FloatArray, kernelSize: Int): IntArray {
        val output = IntArray(width * height)
        val half = kernelSize / 2
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0f
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val ny = (y + ky - half).coerceIn(0, height - 1)
                        val nx = (x + kx - half).coerceIn(0, width - 1)
                        sum += input[ny * width + nx] * kernel[ky * kernelSize + kx]
                    }
                }
                output[y * width + x] = sum.roundToInt()
            }
        }
        return output
    }
}
