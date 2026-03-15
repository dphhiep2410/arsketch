/*
 *  Copyright (c) 2018 Deven.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.arsketch.common.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.example.arsketch.R;

import java.nio.IntBuffer;

/**
 * Created by ${Deven} on 6/1/18.
 */
public class SketchImage {

    public static final int ORIGINAL_TO_GRAY = 0;
    public static final int ORIGINAL_TO_SKETCH = 1;
    public static final int ORIGINAL_TO_COLORED_SKETCH = 2;
    public static final int ORIGINAL_TO_SOFT_SKETCH = 3;
    public static final int ORIGINAL_TO_SOFT_COLOR_SKETCH = 4;
    public static final int GRAY_TO_SKETCH = 5;
    public static final int GRAY_TO_COLORED_SKETCH = 6;
    public static final int GRAY_TO_SOFT_SKETCH = 7;
    public static final int GRAY_TO_SOFT_COLOR_SKETCH = 8;
    public static final int SKETCH_TO_COLOR_SKETCH = 9;

    // required
    private Context context;
    private Bitmap bitmap;

    private Bitmap bmGray, bmInvert, bmBlur, bmBlend;

    private SketchImage(Builder builder) {
        this.context = builder.context;
        this.bitmap = builder.bitmap;
    }

    /**
     * @param type  ORIGINAL_TO_GRAY or ORIGINAL_TO_SKETCH and many more..
     * @param value 0 to 100 to controll effect
     * @return Processed Bitmap
     */



    public Bitmap getImageAs(int type, int value) {

        switch (type) {
            case ORIGINAL_TO_GRAY:
                bmGray = toGrayscale(bitmap, 101 - value); //101-i
                bmInvert = toInverted(bmGray, 1); //i
                bmBlur = toBlur(bmInvert, 1); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case ORIGINAL_TO_SKETCH:
                bmGray = toGrayscale(bitmap, 101 - value); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 100); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);



                Bitmap modifiedBitmap = bmBlend.copy(Bitmap.Config.ARGB_8888, true);
//
                for (int x = 0; x < bmBlend.getWidth(); x++) {
                    for (int y = 0; y < bmBlend.getHeight(); y++) {
                        int pixelColor = modifiedBitmap.getPixel(x, y);
                        if (pixelColor == Color.WHITE) {
                            modifiedBitmap.setPixel(x, y, Color.TRANSPARENT); // or set to another color
                        }
                        int pixelColor1 = modifiedBitmap.getPixel(x, y);
                        if (pixelColor1 <= Color.WHITE && pixelColor1 > Color.BLACK) {
                            modifiedBitmap.setPixel(x, y, Color.BLACK); // or set to another color
                        }
                    }
                }
                Log.d("TAG", "getImageAs: "+bmBlend.getWidth() );
                Log.d("TAG", "getImageAs: "+bmBlend.getHeight() );
//                return bitmap;
                return modifiedBitmap;

            case ORIGINAL_TO_COLORED_SKETCH:
                bmGray = toGrayscale(bitmap, 100); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, value); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case ORIGINAL_TO_SOFT_SKETCH:
                bmGray = toGrayscale(bitmap, 101 - value); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 1); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;


            case ORIGINAL_TO_SOFT_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, 100); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 101 - value); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SKETCH:
                bmGray = toGrayscale(bitmap, 1); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 100); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_COLORED_SKETCH:
                bmGray = toGrayscale(bitmap, value); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, value); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SOFT_SKETCH:
                bmGray = toGrayscale(bitmap, 100); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 1); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SOFT_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, value); //101-i
                bmInvert = toInverted(bmGray, value); //i
                bmBlur = toBlur(bmInvert, 1); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case SKETCH_TO_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, value); //101-i
                bmInvert = toInverted(bmGray, 100); //i
                bmBlur = toBlur(bmInvert, 100); //i
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;
        }
        return bitmap;
    }

    public Bitmap convertToHollow(Bitmap originalBitmap) {
        // Create a new Bitmap with the same dimensions as the original image
        Bitmap hollowBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a Canvas to draw on the new Bitmap
        Canvas canvas = new Canvas(hollowBitmap);

        // Paint object to draw the hollow effect
        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT); // Set the color to transparent

        // Draw the original image on the Canvas
        canvas.drawBitmap(originalBitmap, 0, 0, null);

        // Iterate through each pixel of the image to make non-transparent pixels hollow
        for (int x = 0; x < originalBitmap.getWidth(); x++) {
            for (int y = 0; y < originalBitmap.getHeight(); y++) {
                if (originalBitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    // Make non-transparent pixels hollow by drawing a transparent pixel
                    canvas.drawPoint(x, y, paint);
                }
            }
        }

        return hollowBitmap;
    }

    public static class Builder {
        // required
        private Context context;
        private Bitmap bitmap;
        // optional

        public Builder(Context context, Bitmap bitmap) {
            this.context = context;
            this.bitmap = bitmap;
        }

        public SketchImage build() {
            return new SketchImage(this);
        }

    }

    private Bitmap toGrayscale(Bitmap bmpOriginal, float saturation) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(saturation / 100);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

     public Bitmap scaleBitmap(Bitmap sourceBitmap) {
         Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());
         Canvas canvas = new Canvas(resultBitmap);

         Paint paint = new Paint();
         paint.setColorFilter(new android.graphics.ColorMatrixColorFilter(new float[]{
                 2, 0, 0, 0, -50,
                 0, 2, 0, 0, -50,
                 0, 0, 2, 0, -50,
                 0, 0, 0, 1, 0
         }));

         canvas.drawBitmap(sourceBitmap, 0, 0, paint);

         return resultBitmap;
    }
    public Bitmap convertToSolidWithIncreasedWidth(Bitmap sourceBitmap, int solidColor, int lineWidthIncrease) {
        // Create a new bitmap with the same dimensions as the source bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        // Create a paint object for drawing the solid background
        Paint solidPaint = new Paint();
        solidPaint.setColor(Color.TRANSPARENT);

        // Draw a solid rectangle with increased line width
        canvas.drawRect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), solidPaint);

        // Create a paint object for drawing the original image
        Paint imagePaint = new Paint();
        imagePaint.setAntiAlias(true);

        // Draw the original image on top
        canvas.drawBitmap(sourceBitmap, lineWidthIncrease, lineWidthIncrease, imagePaint);

        return resultBitmap;
    }

    private Bitmap toInverted(Bitmap src, float i) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, i / 100, 0});

        ColorFilter colorFilter = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    private Bitmap toBlur(Bitmap input, float i) {
        try {
            RenderScript rsScript = RenderScript.create(context);
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius((i * 25) / 100);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        } catch (Exception e) {
            // TODO: handle exception
            return input;
        }
    }

    /**
     * Blends 2 bitmaps to one and adds the color dodge blend mode to it.
     */
    public Bitmap colorDodgeBlend(Bitmap source, Bitmap layer, float i) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc, i);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc, i);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc, i);

            int pixel = Color.argb((int) (i * 255) / 100, redValueFinal, greenValueFinal, blueValueFinal);
            buffOut.put(pixel);
        }

        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        return base;
    }
    public  Bitmap enhanceEdges(Bitmap inputBitmap) {
        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();

        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Sobel operator kernels
        int[][] kernelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] kernelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        // Apply Sobel operator to each pixel
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0;
                int gy = 0;

                // Convolve image with Sobel kernels
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixel = inputBitmap.getPixel(x + kx - 1, y + ky - 1);
                        int grayPixel = (int) (Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11);
                        gx += kernelX[ky][kx] * grayPixel;
                        gy += kernelY[ky][kx] * grayPixel;
                    }
                }

                // Calculate magnitude of gradient
                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                outputBitmap.setPixel(x, y, Color.rgb(magnitude, magnitude, magnitude));
            }
        }

        return outputBitmap;
    }
    private int colordodge(int in1, int in2, float i) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << (int) (i * 8) / 100) / (255 - image)))));
    }

}
