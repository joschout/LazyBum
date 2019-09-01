package learning;

import learning.split.SplitInfo;

import java.util.List;

public class StopCriterion {

    private static final int DEFAULT_MAX_DEPTH = Integer.MAX_VALUE;
    private static final int DEFAULT_MIN_SAMPLES_SPLIT = 3;
    private static final int DEFAULT_MIN_SAMPLES_LEAF = 3; // 1

    private int maxDepth;
    private int minSamplesSplit;
    private int minSamplesLeaf;

    public StopCriterion(int maxDepth, int minSamplesSplit, int minSamplesLeaf){
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.minSamplesLeaf = minSamplesLeaf;
    }

    public StopCriterion() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_MIN_SAMPLES_SPLIT, DEFAULT_MIN_SAMPLES_LEAF);
    }

    /**
     *  If we already know we cannot split without having to calculate possible tests,
     *  report true here.
     * @param instances
     * @param depth
     * @return
     */
    public boolean cannotSplitBeforeTest(List<?> instances, int depth) {
        return depth >= this.maxDepth
                || instances.size() < this.minSamplesSplit;

    }

    public boolean cannotSplitOnTest(SplitInfo splitInfo){
        return splitInfo == null
                || ! splitInfo.hasPassingScore()
                || notEnoughExamplesInLeaves(splitInfo);
    }

    public boolean canSplitOnTest(SplitInfo splitInfo){
        return ! cannotSplitOnTest(splitInfo);
    }


    /**
     * Return true if the smallest of the two subsets has NOT enough examples to be acceptable as a leaf.
     *
     * NOTE: I changed it back to min, this explanation isn't true anymore
     * REASON: setting:
     *     minimal_cases(n).
     *           the minimal nb of examples that a leaf in the tree should cover
     *
     * (De Raedt: a good heuristic:
     * stop expanding nodes
     *   WHEN the number of examples in the nodes falls below a certain (user-defined threshold)
     * NOTE:
     *   the nodes get split into two children
     *   --> possible case:
     *       only for 1 of the children, the nb of examples falls below the threshold
     * IF by splitting,
     *       ONE of the nodes falls below a certain user-defined threshold
     *           (i.e. the MIN of their nbs < threshold)
     *       THEN we don't split this node
     * 
     *
     * @param splitInfo
     * @return
     */
    private boolean notEnoughExamplesInLeaves(SplitInfo splitInfo) {
        return Math.min(
                splitInfo.getLeftExamples().size(),
                splitInfo.getRightExamples().size()
        ) < this.minSamplesLeaf;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
