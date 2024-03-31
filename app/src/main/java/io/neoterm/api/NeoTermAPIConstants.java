package io.neoterm.api;

import io.neoterm.shared.neoterm.NeoTermConstants;
import static io.neoterm.shared.neoterm.NeoTermConstants.NEOTERM_API_PACKAGE_NAME;
import static io.neoterm.shared.neoterm.NeoTermConstants.NEOTERM_PACKAGE_NAME;

public class NeoTermAPIConstants {

    /**
     * NeoTerm:API Receiver name.
     */
    public static final String NEOTERM_API_RECEIVER_NAME = NEOTERM_API_PACKAGE_NAME + ".NeoTermApiReceiver"; // Default to "io.neoterm.api.NeoTermApiReceiver"

    /** The Uri authority for NeoTerm:API app file shares */
    public static final String NEOTERM_API_FILE_SHARE_URI_AUTHORITY = NEOTERM_PACKAGE_NAME + ".sharedfiles"; // Default: "io.neoterm.sharedfiles"

}
