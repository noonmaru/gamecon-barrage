package com.github.noonmaru.barrage.process;

import com.github.noonmaru.math.Vector;

/**
 * @author Nemo
 */
public class RayTracePath
{
    public final Vector from;

    public final Vector to;

    public final int calculateTick;

    public RayTracePath(Vector from, Vector to, int calculateTick)
    {
        this.from = from;
        this.to = to;
        this.calculateTick = calculateTick;
    }
}
