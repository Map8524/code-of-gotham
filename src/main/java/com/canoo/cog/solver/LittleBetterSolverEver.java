package com.canoo.cog.solver;

/*
 * #%L
 * code-of-gotham
 * %%
 * Copyright (C) 2015 Canoo Engineering AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.List;

public class LittleBetterSolverEver implements Solver {
    @Override
    public void solveProblem(CityNode node, int streetSize) {

        // Solve leaf
        if (node.isLeaf()) {
            return;
        }

        // Solve problem for children hoods
        for (CityNode childNode : node.getChildren()) {
            solveProblem(childNode, streetSize);
        }

        //Solve problem
        int max_y_axes = 0;
        int max_x_offset = (int) Math.sqrt(node.getTotalArea());
        int next_y_step = 0;
        int offset_x = 0;
        int offset_y = 0;
        List<CityNode> children = node.getChildren();
        for (CityNode child : children) {


            // If reach end of row go to new row
            if (offset_x >= max_x_offset) {
                offset_x = 0;
                offset_y += next_y_step + streetSize;
                next_y_step = 0;
            }

            // Get maximum size in this row
            if (child.getSize() > next_y_step) {
                next_y_step = child.getSize();
            }

            // Set the current node
            child.setX(offset_x);
            child.setY(offset_y);

            // Add the current node to the offset
            offset_x += child.getSize() + streetSize;

            // Update the maximum y offset
            final int potentialNewYOffset = offset_y + child.getSize();
            if (max_y_axes < potentialNewYOffset) {
                max_y_axes = potentialNewYOffset;
            }

            // Update the maximum x offset
            final int potentialNewXOffset = offset_x;
            if (max_x_offset < potentialNewXOffset) {
                max_x_offset = potentialNewXOffset;
            }
        }
        node.setSize(Math.max(max_x_offset, max_y_axes));
    }
}
