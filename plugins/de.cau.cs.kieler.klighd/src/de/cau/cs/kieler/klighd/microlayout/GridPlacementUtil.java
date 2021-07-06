/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2013 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.microlayout;

import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.asGridPlacement;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.asGridPlacementData;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.getPlacementData;
import static de.cau.cs.kieler.klighd.microlayout.PlacementUtil.basicEstimateSize;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.elk.core.math.ElkMath;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KGridPlacement;
import de.cau.cs.kieler.klighd.krendering.KGridPlacementData;
import de.cau.cs.kieler.klighd.krendering.KPosition;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;

/**
 * A utility class for evaluating the grid micro layout of KRenderings.
 *
 * @author chsch
 * @author akoc
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public final class GridPlacementUtil {

    /**
     * Property definition allowing attach grid size assignments to {@link KRendering KRenderings}
     * with {@link KGridPlacement}. This is done in order to avoid unnecessary re-computations of
     * the grid's cell sizes while drawing the grid. This is especially important in case of nested
     * grids.
     */
    public static final IProperty<GridSizeAssignment> ESTIMATED_GRID_DATA
            = new Property<GridSizeAssignment>("klighd.grid.estimatedGridData");

    /**
     * Property definition allowing attach the determined position of the
     * {@link de.cau.cs.kieler.klighd.kgraph.KNode KNode's}
     * {@link de.cau.cs.kieler.klighd.krendering.KChildArea KChildArea} within the grid. This reduces
     * searching especially the child area is hidden in nested grids.
     */
    public static final IProperty<Pair<Integer, Integer>> CHILD_AREA_POSITION
            = new Property<Pair<Integer, Integer>>("klighd.grid.childAreaPosition");


    /**
     * Hidden standard constructor.
     */
    private GridPlacementUtil() {
    }


    /**
     * Returns the bounds for a gridPlacement data in given parent bounds.<br>
     * <br>
     * TODO: Improve doc wrt. returned empty bounds or <code>null</code> values!!
     *
     * @param gridPlacement
     *            the definition of the parent gridPlacement defining the column number
     * @param children
     *            the children to be placed inside the grid with their placement data
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    public static Bounds[] evaluateGridPlacement(final KGridPlacement gridPlacement,
            final List<KRendering> children, final Bounds parentBounds) {
        if (parentBounds.isEmpty()) {
            final Bounds[] result = new Bounds[children.size()];
            Arrays.fill(result, Bounds.of(0, 0));
            return result;
        }
        final GridPlacer placer = new GridPlacer(gridPlacement, children);
        return placer.evaluate(parentBounds);
    }


    /**
     * A helper class to calculate bounds for the elements placed in a grid.
     */
    private static class GridPlacer {

        private KContainerRendering parent;

        private KPosition topLeft = null;
        private KPosition bottomRight = null;

        /** the associated grid placement data. */
        private List<KRendering> children;

        /** the number of columns. */
        private int numColumns;

        /** the number of rows. */
        private int numRows;

        /** the number of flexible columns (those additional space can be assigned). */
        private int numFlexibleCols = 0;

        /** the number of flexible columns (those additional space can be assigned). */
        private int numFlexibleRows = 0;

        /** the distribution of flexible columns (those additional space can be assigned). */
        private boolean[] flexibleCols;

        /** the distribution of flexible rows (those additional space can be assigned). */
        private boolean[] flexibleRows;

        /** the minimal width required by the whole grid. */
        private float minOverallWidth = 0;

        /** the minimal height required by the whole grid. */
        private float minOverallHeight = 0;

        /** the minimal widths of the particular columns. */
        private float[] columnMaxMinWidth;

        /** the minimal heights of the particular rows. */
        private float[] rowMaxMinHeight;

        /** the finally calculated widths of the particular columns. */
        private float[] calculatedRowHeight;

        /** the finally calculated heights of the particular rows. */
        private float[] calculatedColumnWidth;

        /**
         * Constructor.
         *
         * @param gridPlacement
         *            the grid data
         * @param children
         *            the children to be placed inside the grid with their placementData
         */
        GridPlacer(final KGridPlacement gridPlacement, final List<KRendering> children) {
            this.parent = (KContainerRendering) gridPlacement.eContainer();
            this.children = children;

            if (children.size() == 0) {
                // no elements to place
                return;
            }

            // the following prepares the placer

            // determine the required number of columns and rows
            final int setColumns = gridPlacement.getNumColumns();
            this.topLeft = gridPlacement.getTopLeft();
            this.bottomRight = gridPlacement.getBottomRight();
            this.numColumns = setColumns == -1 ? children.size() : setColumns < 1 ? 1 : gridPlacement
                    .getNumColumns();
            this.numRows = (children.size() - 1) / this.numColumns + 1;


            this.flexibleCols = new boolean[this.numColumns];
            this.flexibleRows = new boolean[this.numRows];

            final GridSizeAssignment spacingProperty = this.parent.getProperty(ESTIMATED_GRID_DATA);

            final boolean gridSizingAvailable = spacingProperty != null;

            final GridSizeAssignment estimatedSpacing =
                    !gridSizingAvailable ? null : new GridSizeAssignment(spacingProperty);

            if (gridSizingAvailable) {
                //take the data already calculated during the size estimation
                this.calculatedColumnWidth = estimatedSpacing.getCalculatedColumnWidths().clone();
                this.calculatedRowHeight = estimatedSpacing.getCalculatedRowHeights().clone();
            }
            if (this.columnMaxMinWidth == null) {
                //start from scratch
                this.columnMaxMinWidth = new float[this.numColumns];
                Arrays.fill(this.columnMaxMinWidth, 0);
            }
            if (this.rowMaxMinHeight == null) {
                this.rowMaxMinHeight = new float[this.numRows];
                Arrays.fill(this.rowMaxMinHeight, 0);
            }

            Arrays.fill(this.flexibleCols, true);
            Arrays.fill(this.flexibleRows, true);

            for (int i = 0; i < children.size(); i++) {

                final KRendering r = children.get(i);
                final int column = i % this.numColumns;
                final int row = i / this.numColumns;

                KGridPlacementData gpd = asGridPlacementData(getPlacementData(r));
                if (gpd == null) {
                    //no grid placement data present, create dummy to prevent null pointer exceptions
                    gpd = KRenderingFactory.eINSTANCE.createKGridPlacementData();
                }

                //determine size of the rendering to be able to size the colum/row accordingly
                final Bounds childMinSize;
                if (gridSizingAvailable) {
                    childMinSize = Bounds.of(estimatedSpacing.calculatedColumnWidths[column],
                            estimatedSpacing.calculatedRowHeights[row]);
                } else {
                    childMinSize = basicEstimateSize(r, new Bounds(0, 0));

                    PlacementUtil.inverselyApplyBoundingBoxKPositions(
                            childMinSize, gpd.getTopLeft(), gpd.getBottomRight());
                }

                this.flexibleCols[column] &= gpd.getFlexibleWidth();
                this.flexibleRows[row] &= gpd.getFlexibleHeight();

                // determine the maximum of the minimum column widths and row heights
                // e.g. how big must a column be to fit all the minSizes

                // size for this element is size of child elements or minSize
                this.columnMaxMinWidth[column] = ElkMath.maxf(
                        gpd.getMinCellWidth(), childMinSize.getWidth(), this.columnMaxMinWidth[column]);
                this.rowMaxMinHeight[row] = ElkMath.maxf(
                        gpd.getMinCellHeight(), childMinSize.getHeight(), this.rowMaxMinHeight[row]);
            }

            // calculate the total width and height
            for (int i = 0; i < this.numColumns; i++) {
                this.minOverallWidth += this.columnMaxMinWidth[i];
                this.numFlexibleCols += this.flexibleCols[i] ? 1 : 0;
            }
            for (int i = 0; i < this.numRows; i++) {
                this.minOverallHeight += this.rowMaxMinHeight[i];
                this.numFlexibleRows += this.flexibleRows[i] ? 1 : 0;
            }
        }

        private static final float TOLERANCE = 0.1f;

        /**
         * Evaluates the grid placement for the given parent bounds.
         *
         * @param parentBounds
         *            the parent bounds
         * @return the bounds for the placed elements
         */
        public Bounds[] evaluate(final Bounds parentBounds) {
            if (children.size() == 0) {
                return new Bounds[0];
            }

            final Bounds[] bounds = new Bounds[children.size()];

            // in case there is actually no space - skip the costly computations
            if (parentBounds.getWidth() == 0f || parentBounds.getHeight() == 0f) {
                Arrays.fill(bounds, new Bounds(0, 0));
                return bounds;
            }

            // if there're no cached estimated grid data attached to the 'parent'
            //  KContainerRendering just do the size estimation of 'parent's children right now
            // this may happen in case a pre-layouted view model is opened in the KGraph editor
            //  with initial diagram layout switched off, for example  
            if (parent.getProperty(ESTIMATED_GRID_DATA) == null) {
                estimateGridSize(parent, parentBounds);
            }

            final GridSizeAssignment estimatedGrid = parent.getProperty(ESTIMATED_GRID_DATA);
            final Pair<Integer, Integer> childAreaPosition = parent.getProperty(CHILD_AREA_POSITION);

            int width = 0, height = 0;
            final float widthScale = 1, heightScale = 1;


            if (estimatedGrid != null) {
                //calculate the width that lead to the distribution before
                for (int i = 0; i < estimatedGrid.getCalculatedColumnWidths().length; i++) {
                    width += estimatedGrid.getCalculatedColumnWidths()[i];
                }
                for (int i = 0; i < estimatedGrid.getCalculatedRowHeights().length; i++) {
                    height += estimatedGrid.getCalculatedRowHeights()[i];
                }
            }

            final Bounds gridBounds = PlacementUtil.evaluateAreaPlacement(topLeft, bottomRight,
                    parentBounds);

            //if the calculated width differs from the width the parent has, we have to put
            //that additional width to use
            if (childAreaPosition != null
                    && childAreaPosition.getFirst() > -1
                    && childAreaPosition.getSecond() > -1) {
                //check if there is a childArea and put that width into it's cell

                if (estimatedGrid != null) {
                    this.calculatedColumnWidth = estimatedGrid.getCalculatedColumnWidths().clone();
                    this.calculatedRowHeight = estimatedGrid.getCalculatedRowHeights().clone();

                    if (gridBounds.getWidth() - width > TOLERANCE
                            && childAreaPosition.getFirst() < numColumns) {
                        calculatedColumnWidth[childAreaPosition.getFirst()]
                                += gridBounds.getWidth() - width;
                    }
                    if (gridBounds.getHeight() - height > TOLERANCE
                            && childAreaPosition.getSecond() < numRows) {
                        calculatedRowHeight[childAreaPosition.getSecond()]
                                += gridBounds.getHeight() - height;
                    }

                } else {
                    final boolean[] tempFlexibleCols = new boolean[flexibleCols.length];
                    tempFlexibleCols[childAreaPosition.getFirst()] = true;

                    final boolean[] tempFlexibleRows = new boolean[flexibleRows.length];
                    tempFlexibleRows[childAreaPosition.getSecond()] = true;

                    this.calculatedColumnWidth = computeCellSizes(gridBounds.getWidth(), minOverallWidth,
                            columnMaxMinWidth, 1, tempFlexibleCols);

                    this.calculatedRowHeight = computeCellSizes(gridBounds.getHeight(), minOverallHeight,
                            rowMaxMinHeight, 1, tempFlexibleRows);
                }

            } else {
                // we have to calculate the data for the single rows / cols

                this.calculatedColumnWidth = computeCellSizes(gridBounds.getWidth(), minOverallWidth,
                        columnMaxMinWidth, numFlexibleCols, flexibleCols);

                this.calculatedRowHeight = computeCellSizes(gridBounds.getHeight(), minOverallHeight,
                        rowMaxMinHeight, numFlexibleRows, flexibleRows);
            }

            // variables that are later on used to define the bounds of single objects
            final float startX = gridBounds.getX();
            final float startY = gridBounds.getY();

            float currentX = startX;
            float currentY = startY;

            for (int i = 0; i < children.size(); i++) {
                final KGridPlacementData gpd = asGridPlacementData(getPlacementData(children.get(i)));
                final int column = i % numColumns;
                final int row = i / numColumns;

                final Bounds cellBounds = Bounds.of(calculatedColumnWidth[column],
                        calculatedRowHeight[row]);

                final Bounds localBounds = gpd == null
                        ? cellBounds : PlacementUtil.evaluateAreaPlacement(
                            gpd.getTopLeft(), gpd.getBottomRight(), cellBounds);

                bounds[i] = localBounds.move(currentX, currentY);


                currentX += cellBounds.getWidth() * widthScale;

                // advance the current y-coordinate if necessary
                if (column == numColumns - 1) {
                    currentY += cellBounds.getHeight() * heightScale;
                    // new row => start from left.
                    currentX = startX;
                }
            }
            return bounds;
        }

        /**
         * Computes the widths (heights) of the cells of a grid based on the provided parameters
         * that characterize the grid wrt. the x (y) dimension.
         *
         * @param availableSize
         *              the current width (height) of the grid's parent figure
         * @param requiredSize
         *              the width (height) required by the grid columns (rows)
         * @param requiredSizes
         *              the widths (heights) of the particular columns (rows)
         * @param countFlexibleSizes
         *              the number of flexible columns (rows)
         * @param flexibleSizes
         *              the particular flexibility flags wrt. the particular columns (rows)
         * @return an array of widths (heights) of the particular columns (rows)
         */
        private float[] computeCellSizes(final float availableSize, final float requiredSize,
                final float[] requiredSizes, final float countFlexibleSizes,
                final boolean[] flexibleSizes) {
            final float[] result = new float[requiredSizes.length];

            //
            final float overSize = availableSize - requiredSize;
            final boolean overSized = overSize > 0;
            final float overSizeShare =
                    !overSized ? 0 : overSize / (countFlexibleSizes == 0 ? 1 : countFlexibleSizes);

            // the running variable
            float remainingSize = availableSize;

            for (int i = 0; i < requiredSizes.length; ++i) {
                if (remainingSize >= 0) {
                    result[i] = requiredSizes[i];
                    if (overSized
                            && (flexibleSizes[i] || countFlexibleSizes == 0
                                    && i + 1 == requiredSizes.length)) {
                        result[i] += overSizeShare;
                    }
                    remainingSize -= result[i];
                } else {
                    break;
                }
            }
            return result;
        }
    }


    /**
     * Returns the minimal size of a {@link KContainerRendering} with
     * <code>childPlacement instanceof KGridPlacement</code> and updates the placement data of
     * internal KRenderings if necessary.<br>
     * <br>
     * {@link de.cau.cs.kieler.klighd.properties.IProperty IProperties} for delivering those data.
     *
     * @param container
     *            the {@link KContainerRendering} to be evaluated
     * @param parentBounds
     *            the size that is currently assigned to 'container'
     * @return the minimal size
     */
    public static Bounds estimateGridSize(final KContainerRendering container,
            final Bounds parentBounds) {
        int numColumns = ((KGridPlacement) container.getChildPlacement()).getNumColumns();
        final List<KRendering> childRenderings = container.getChildren();

        final int numRows;
        if (numColumns == -1) {
            numColumns = childRenderings.size();
            numRows = 1;
        } else if (numColumns < 2) {
            // if the number of columns is not set or is 1 then in each row is one rendering,
            // so numRows = number of elements to place
            numColumns = 1;
            numRows = childRenderings.size();
        } else {
            // else calculate number of rows based on number of child elements and columns
            numRows = (childRenderings.size() + numColumns - 1) / numColumns;
        }

        // initialize the minimal size for each row and column with zero
        final float[] minColumnWidths = new float[numColumns];
        final float[] minRowHeights = new float[numRows];

        // variables to later store the information where an optional childArea might be placed
        int childAreaRowId = -1;
        int childAreaColId = -1;

        // first evaluate the grid to get position and size of each cell
        // to make the layout as compact as possible but have space to place all children,
        // evaluate the space needed for each child
        for (int k = 0; k < childRenderings.size(); k++) {
            final KRendering currentChild = childRenderings.get(k);

            final int row = k / numColumns;
            final int col = k - row * numColumns;

            final LinkedList<KRendering> path = Lists.newLinkedList();
            if (PlacementUtil.findChildArea(currentChild, path)) {
                // if a childArea is contained in the current child (or the child itself is a childarea)
                // remember the position of the area to be able to calculate
                // the size of the parentNode correctly
                // later this is used to calculate insets based on the cellPosition of the childArea
                childAreaColId = col;
                childAreaRowId = row;
            }

            final Bounds cellSize = basicEstimateSize(currentChild, new Bounds(0, 0));

            // evaluate the "winning" KGridPlacementData
            final KGridPlacementData gridData = asGridPlacementData(getPlacementData(currentChild));

            if (gridData != null) {
                final KPosition topLeft = gridData.getTopLeft();
                final KPosition bottomRight = gridData.getBottomRight();

                // apply the cell-local "passe-partout" inversely on the cell size
                PlacementUtil.inverselyApplyBoundingBoxKPositions(cellSize, topLeft, bottomRight);

                // apply the minimal width & definitions
                Bounds.max(cellSize, Bounds.of(gridData.getMinCellWidth(), gridData.getMinCellHeight()));
            }

            // compare the width and height of the current rendering with the biggest width
            // and height of the corresponding row and column and update the values with the maximum
            minRowHeights[row] = Math.max(minRowHeights[row], cellSize.getHeight());
            minColumnWidths[col] = Math.max(minColumnWidths[col], cellSize.getWidth());
        }

        // store the information of the size of the cells in the KRendering containing the grid
        //  in order to be able to re-use that information later
        // the re-usage of already present property objects is done to reduce the notification flood
        //  and the result re-composition of the node figures in the Piccolo binding

        final boolean deliver = container.eDeliver();
        container.eSetDeliver(false);

        final GridSizeAssignment pSpacing = container.getProperty(ESTIMATED_GRID_DATA);
        if (pSpacing != null) {
            pSpacing.setCalculatedSizes(minColumnWidths, minRowHeights);
        } else {
            container.setProperty(ESTIMATED_GRID_DATA, new GridSizeAssignment(minColumnWidths,
                    minRowHeights));
        }

        final Pair<Integer, Integer> pCAPos = container.getProperty(CHILD_AREA_POSITION);
        if (pCAPos != null) {
            pCAPos.setFirst(childAreaColId);
            pCAPos.setSecond(childAreaRowId);
        } else {
            container.setProperty(CHILD_AREA_POSITION, Pair.of(childAreaColId, childAreaRowId));
        }

        container.eSetDeliver(deliver);

        // the minimum total bound is the sum of the single column widths and row heights
        final Bounds childBounds = new Bounds(0, 0);

        for (final float width : minColumnWidths) {
            childBounds.setWidth(childBounds.getWidth() + width);
        }
        for (final float height : minRowHeights) {
            childBounds.setHeight(childBounds.getHeight() + height);
        }

        // take insets of the grid itself into consideration
        final KGridPlacement placement = asGridPlacement(container.getChildPlacement());

        // increase size according to the space needed by the insets
        PlacementUtil.inverselyApplyBoundingBoxKPositions(childBounds, placement.getTopLeft(),
                placement.getBottomRight());

        return childBounds;
    }


    /**
     * A data holder class for the spacing of the grid calculated during the size estimation. this
     * class is intended to be used in the above defined IProperty ESTIMATED_GRID_DATA to be
     * evaluated during the actual evaluation of parentBounds to place childElements
     *
     * @author akoc
     * @author chsch
     */
    private static class GridSizeAssignment {
        private float[] calculatedColumnWidths;
        private float[] calculatedRowHeights;

        /**
         * Copy constructor.
         *
         * @param other
         *            the copied one
         */
        GridSizeAssignment(final GridSizeAssignment other) {
            this.calculatedColumnWidths =
                    other == null ? null : other.calculatedColumnWidths.clone();
            this.calculatedRowHeights =
                    other == null ? null : other.calculatedRowHeights.clone();
        }

        /**
         * Constructor. Sets the attributes according to given parameters
         *
         * @param cols
         *            the column widths calculated during the size estimation
         * @param rows
         *            the row heights calculated during the size estimation
         */
        GridSizeAssignment(final float[] cols, final float[] rows) {
            this.calculatedColumnWidths = cols;
            this.calculatedRowHeights = rows;
        }

        /**
         * Setter.
         *
         * @param cols
         *            the column widths calculated during the size estimation
         * @param rows
         *            the row heights calculated during the size estimation
         */
        public void setCalculatedSizes(final float[] cols, final float[] rows) {
            this.calculatedColumnWidths = cols;
            this.calculatedRowHeights = rows;
        }
        /**
         * Getter.
         *
         * @return the calculatedColumnWidths
         */
        public float[] getCalculatedColumnWidths() {
            return calculatedColumnWidths;
        }

        /**
         * Getter.
         *
         * @return the calculatedRowHeights
         */
        public float[] getCalculatedRowHeights() {
            return calculatedRowHeights;
        }

        @Override
        public String toString() {
            return "(Widths = " + Arrays.toString(this.calculatedColumnWidths) + ", Height = "
                    + Arrays.toString(this.calculatedRowHeights) + ")";
        }
    }
}
