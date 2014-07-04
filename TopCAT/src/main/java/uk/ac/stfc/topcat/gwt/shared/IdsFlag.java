package uk.ac.stfc.topcat.gwt.shared;

public enum IdsFlag {
    /**
     * Apply compression if the file or files are zipped.
     */
    COMPRESS,

    /**
     * No zipping when a single data file is requested and no compression.
     */
    NONE,

    /**
     * Also zip when a single data file is requested.
     */
    ZIP,

    /**
     * Compress and also zip when a single data file is requested.
     */
    ZIP_AND_COMPRESS
}