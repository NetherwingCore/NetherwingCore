package br.net.dd.netherwingcore.common.logging;

/**
 * Represents a marker or contract for logging details within the Netherwing Core framework.
 *
 * <p>This interface can be implemented by classes that contain specific information
 * to be used in logging operations. By defining the structure of the logging details,
 * this interface encourages a standardized implementation across the application.</p>
 *
 * <p>As part of the logging subsystem, implementing this interface ensures that detailed
 * information critical to monitoring and debugging can be encapsulated and passed to
 * loggers effectively.</p>
 *
 * <strong>Usage:</strong>
 * <blockquote><pre>
 * public class CustomDetail implements Detail {
 *     private String key;
 *     private String value;
 *
 *     public CustomDetail(String key, String value) {
 *         this.key = key;
 *         this.value = value;
 *     }
 *
 *     // Add appropriate getters, setters, and other methods as needed
 * }
 * </pre></blockquote>
 *
 * Note: This interface is currently a marker and does not enforce method definitions.
 * This design provides flexibility, allowing implementers to define their log detail
 * structures as per their needs.
 *
 */
public interface Detail {
}
