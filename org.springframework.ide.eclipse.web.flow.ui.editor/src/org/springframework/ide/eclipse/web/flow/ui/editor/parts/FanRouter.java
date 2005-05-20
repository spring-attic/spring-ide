/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Ray;

/**
 * Automatic router that spreads its {@link Connection Connections} in a fan-like fashion 
 * upon collision.
 */
public class FanRouter extends AutomaticRouter {

    private int separation = 10;

    /**
     * Returns the separation in pixels between fanned connections.
     * 
     * @return the separation
     * @since 2.0
     */
    public int getSeparation() {
        return separation;
    }

    /**
     * Modifies a given PointList that collides with some other PointList.  The given 
     * <i>index</i> indicates that this it the i<sup>th</sup> PointList in a group of 
     * colliding points.
     * 
     * @param points the colliding points
     * @param index the index
     */
    protected void handleCollision(PointList points, int index) {
        Point start = points.getFirstPoint();
        Point end = points.getLastPoint();

        if (start.equals(end))
            return;

        Point midPoint = new Point((end.x + start.x) / 2, (end.y + start.y) / 2);
        int position = end.getPosition(start);
        Ray ray;
        if (position == PositionConstants.SOUTH
                || position == PositionConstants.EAST)
            ray = new Ray(start, end);
        else
            ray = new Ray(end, start);
        double length = ray.length();

        double xSeparation = separation * ray.x / length;
        double ySeparation = separation * ray.y / length;

        Point bendPoint;

        if (index % 2 == 0) {
            bendPoint = new Point(
                    midPoint.x + (index / 2) * (-1 * ySeparation), midPoint.y
                            + (index / 2) * xSeparation);
        }
        else {
            bendPoint = new Point(midPoint.x + (index / 2) * ySeparation,
                    midPoint.y + (index / 2) * (-1 * xSeparation));
        }
        if (!bendPoint.equals(midPoint))
            points.insertPoint(bendPoint, 1);
    }

    /**
     * Sets the colliding {@link Connection Connection's} separation in pixels.
     * 
     * @param value the separation
     * @since 2.0 
     */
    public void setSeparation(int value) {
        separation = value;
    }

}
