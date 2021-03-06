/*
 * Developed by Michel Faria on 10/29/18 8:39 PM.
 * Last modified 10/25/18 7:46 PM.
 * Copyright (c) 2018. All rights reserved.
 */

package io.michelfaria.chrono.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.michelfaria.chrono.Game;
import io.michelfaria.chrono.textures.TRD.FlipData;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for animation map and current playing animation.
 * Draws animation on coordinates.
 */
public class AnimationManager<Key> {

    private final Map<Key, AnimationData<TextureRegion>> animations = new HashMap<>();

    private Key currentAnimation = null;
    private float stateTime = 0;

    /**
     * Draws the current animation at the X and Y location.
     */
    public void draw(Batch batch, float x, float y) {
        draw(batch, x, y, 0);
    }

    /**
     * Draws the current animation at the X and Y location.
     */
    public void draw(Batch batch, float x, float y, float parentAlpha) {
        stateTime += Gdx.graphics.getDeltaTime();
        if (getCurrentAnimation() == null) {
            throw new IllegalStateException("No animation");
        }

        AnimationData<TextureRegion> animationData = animations.get(getCurrentAnimation());
        int keyFrameIndex = animationData.animation.getKeyFrameIndex(stateTime);
        TextureRegion keyFrame = animationData.animation.getKeyFrames()[keyFrameIndex];
        FlipData flipData = null;
        if (animationData.trd != null) {
            flipData = animationData.trd.flipData;
        }

        if (flipData == null) {
            keyFrame.flip(false, false);
        } else {
            flipKeyFrame(keyFrame, getFlipInstructionForKeyFrameIndex(keyFrameIndex, flipData));
        }
        batch.draw(keyFrame, x, y);
    }

    private void flipKeyFrame(TextureRegion keyFrame, byte flipInstruction) {
        boolean flipX = false;
        boolean flipY = false;

        switch (flipInstruction) {
            case FlipData.FLIP_HORZ:
                flipX = true;
                break;
            case FlipData.FLIP_VERT:
                flipY = true;
                break;
            case FlipData.FLIP_BOTH:
                flipX = true;
                flipY = true;
                break;
            case FlipData.FLIP_NONE:
                break;
            default:
                throw new IllegalStateException("Unknown byte");
        }
        keyFrame.flip(!keyFrame.isFlipX() && flipX, !keyFrame.isFlipY() && flipY);
    }

    private byte getFlipInstructionForKeyFrameIndex(int keyFrameIndex, FlipData flipData) {
        int flipIndex = 0;
        for (int i = 0; i < flipData.indexes.length; i++) {
            if (flipData.indexes[i] <= keyFrameIndex) {
                flipIndex = i;
            }
            if (flipData.indexes[i] > keyFrameIndex) {
                break;
            }
        }
        return flipData.flip[flipIndex];
    }

    public Map<Key, AnimationData<TextureRegion>> getAnimations() {
        return animations;
    }

    public Key getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(Key currentAnimation) {
        if (currentAnimation != this.currentAnimation) {
            stateTime = 0;
            this.currentAnimation = currentAnimation;
        }
    }
}
