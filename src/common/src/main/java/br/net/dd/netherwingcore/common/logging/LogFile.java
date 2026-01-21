package br.net.dd.netherwingcore.common.logging;

import java.nio.file.Path;

/**
 * Represents a record that encapsulates one or more file paths related to logging.
 * <p>
 * This class is implemented as a Java record, providing a compact and immutable
 * way to store and operate on a collection of paths. It can be used to aggregate
 * logging-related file paths in a structured manner.
 * </p>
 *
 * @param paths an array of {@link Path} objects representing the file paths related to logs.
 *              Multiple paths may be provided as part of the logging operations.
 */
public record LogFile(Path... paths) implements Detail {
}
