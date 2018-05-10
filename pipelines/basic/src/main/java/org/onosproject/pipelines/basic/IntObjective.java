package org.onosproject.pipelines.basic;

import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;

import static com.google.common.base.Preconditions.checkArgument;

public final class IntObjective {

    private static final int DEFAULT_PRIORITY = 10;

    // TrafficSelector to describe target flows to monitor
    private final TrafficSelector selector;
    // instruction bitmap
    private final int instBitmap;
    // hop-by-hop (1)  or destination (2)
    private final int headerType;

    /**
     * Creates an IntObjective.
     *
     * @param selector   the traffic selector that identifies traffic to enable INT
     * @param instBitmap the types of metadata to collect
     * @param headerType the type of INT header
     */
    private IntObjective(TrafficSelector selector, int instBitmap, int headerType) {
        this.selector = selector;
        this.instBitmap = instBitmap;
        this.headerType = headerType;
    }

    /**
     * Returns traffic selector of this objective.
     *
     * @return traffic selector
     */
    public TrafficSelector selector() {
        return selector;
    }

    /**
     * Returns an instruction bitmap specified in this objective.
     *
     * @return instruction bitmap
     */
    public int instructionBitmap() {
        return instBitmap;
    }

    /**
     * Returns a INT header type specified in this objective.
     *
     * @return INT header type
     */
    public int headerType() {
        return headerType;
    }

    /**
     * An IntObjective builder.
     */
    public static final class Builder {
        private TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        private int instBitmap = 0;
        private int headerType = 1; // Hop-by-hop

        /**
         * Assigns a selector to the IntObjective.
         *
         * @param selector a traffic selector
         * @return an IntObjective builder
         */
        public IntObjective.Builder withSelector(TrafficSelector selector) {
            this.selector = selector;
            return this;
        }

        /**
         * Add a metadata type to the IntObjective.
         *
         * @param instBitmap instruction bitmap
         * @return an IntObjective builder
         */
        public IntObjective.Builder withInstructionBitmap(int instBitmap) {
            this.instBitmap = instBitmap;
            return this;
        }

        /**
         * Assigns a header type to the IntObjective.
         *
         * @param headerType a header type
         * @return an IntObjective builder
         */
        public IntObjective.Builder withHeaderType(int headerType) {
            this.headerType = headerType;
            return this;
        }

        /**
         * Builds the IntObjective.
         *
         * @return an IntObjective
         */
        public IntObjective build() {
            checkArgument(!selector.criteria().isEmpty(), "Empty selector cannot match any flow.");
            checkArgument(instBitmap > 0 && instBitmap <= 0xffff,
                          "Instruction bitmap should be in between 0x1 and 0xffff.");
            checkArgument(headerType == 1 || headerType == 2, "Header type should be either 1 or 2");

            return new IntObjective(selector, instBitmap, headerType);
        }
    }
}
