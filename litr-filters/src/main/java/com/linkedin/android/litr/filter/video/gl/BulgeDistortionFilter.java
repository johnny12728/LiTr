/*
 * Copyright 2018 Masayuki Suda
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.linkedin.android.litr.filter.video.gl;

import android.graphics.PointF;
import android.opengl.GLES20;

import androidx.annotation.NonNull;

/**
 * Frame render filter that applies a bulge distortion to video frame
 */
public class BulgeDistortionFilter extends BaseFrameRenderFilter {

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +

            "precision mediump float;\n" +

            "varying highp vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +

            "uniform highp vec2 center;\n" +
            "uniform highp float radius;\n" +
            "uniform highp float scale;\n" +

            "void main()\n" +
            "{\n" +
                "highp vec2 textureCoordinateToUse = vTextureCoord;\n" +
                "highp float dist = distance(center, vTextureCoord);\n" +
                "textureCoordinateToUse -= center;\n" +
                "if (dist < radius)\n" +
                "{\n" +
                    "highp float percent = 1.0 - ((radius - dist) / radius) * scale;\n" +
                    "percent = percent * percent;\n" +
                    "textureCoordinateToUse = textureCoordinateToUse * percent;\n" +
                "}\n" +
                "textureCoordinateToUse += center;\n" +
                "gl_FragColor = texture2D(sTexture, textureCoordinateToUse);\n" +
            "}";

    private PointF center;
    private float radius;
    private float scale;

    /**
     * Create bulge distortion render filter
     * @param center center of distortion, in relative coordinates in 0 - 1 range
     * @param radius radius of distortion, in relative coordinaes in 0 - 1 range
     * @param scale scale of distortion
     */
    public BulgeDistortionFilter(@NonNull PointF center, float radius, float scale) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);

        this.center = center;
        this.radius = radius;
        this.scale = scale;
    }

    /**
     * Create frame render filter with source video frame, then scale, then position and then rotate the bitmap around its center as specified.
     * @param center center of distortion, in relative coordinates in 0 - 1 range
     * @param radius radius of distortion, in relative coordinaes in 0 - 1 range
     * @param scale scale of distortion
     * @param size size in X and Y direction, relative to target video frame
     * @param position position of source video frame  center, in relative coordinate in 0 - 1 range
     *                 in fourth quadrant (0,0 is top left corner)
     * @param rotation rotation angle of overlay, relative to target video frame, counter-clockwise, in degrees
     */
    public BulgeDistortionFilter(@NonNull PointF center, float radius, float scale, @NonNull PointF size, @NonNull PointF position, float rotation) {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER, size, position, rotation);

        this.center = center;
        this.radius = radius;
        this.scale = scale;
    }

    @Override
    protected void applyCustomGlAttributes() {
        GLES20.glUniform2f(getHandle("center"), center.x, center.y);
        GLES20.glUniform1f(getHandle("radius"), radius);
        GLES20.glUniform1f(getHandle("scale"), scale);
    }
}
